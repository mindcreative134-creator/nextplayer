/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package app.infinity.mpvz.ui.player

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import app.infinity.mpvz.domain.network.NetworkPlaybackUri
import app.infinity.mpvz.ui.player.PlayerActivity.Companion.TAG
import `is`.xyz.mpv.MPVNode
import `is`.xyz.mpv.Utils
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Storage path constants for Android's various storage locations.
 */
private object StoragePaths {
  const val PRIMARY_PREFIX = "primary:"
  const val RAW_PREFIX = "raw:"
  const val PRIMARY_STORAGE = "/storage/emulated/0"
  const val EXTERNAL_STORAGE = "/storage"
  const val MEDIA_RW = "/mnt/media_rw"
}

/**
 * Resolves content:// URIs to paths MPV can play.
 *
 * Tries multiple resolution strategies because Android's storage system varies by:
 * - Android version (pre-10, 10+, 11+ with scoped storage)
 * - Storage type (internal, external SD, SAF documents)
 * - Content provider implementation
 *
 * Falls back to file descriptor if real path cannot be determined.
 */

/**
 * Extracts a direct local filesystem path from a content:// URI if it exists.
 * This is useful to bypass scoped storage / document provider permissions when we have MANAGE_EXTERNAL_STORAGE.
 */
internal fun Uri.extractLocalPath(): String? {
  val decoded = runCatching { Uri.decode(this.toString()) }.getOrNull() ?: return null
  val candidates = listOf("/storage/emulated/", "/storage/", "/sdcard/")
  for (candidate in candidates) {
    val index = decoded.indexOf(candidate)
    if (index != -1) {
      val rawPath = decoded.substring(index)
      val path = rawPath.substringBefore('?').substringBefore('#')
      if (File(path).exists()) {
        return path
      }
    }
  }
  return null
}

internal fun Uri.openContentFd(
  context: Context,
  allowFdFallback: Boolean = true,
): String? {
  val targetUri = resolveDownloadsUri(context) ?: this
  return targetUri.extractLocalPath()
    ?: targetUri.tryFileDescriptorPath(context, allowFdFallback)
    ?: targetUri.tryDownloadsToMediaFallback(context, allowFdFallback)
    ?: targetUri.tryMediaStoreQuery(context)
    ?: targetUri.tryDocumentUriParsing(context)
    ?: (if (allowFdFallback) targetUri.tryFileDescriptorFallback(context) else null)
}

/** Resolves identity only; unlike [openContentFd], this never detaches a file descriptor. */
internal fun Uri.resolveLocalPath(context: Context): String? {
  val targetUri = resolveDownloadsUri(context) ?: this
  return when (targetUri.scheme?.lowercase()) {
    "file" -> targetUri.path
    "content" ->
      targetUri.extractLocalPath()
        ?: targetUri.tryFileDescriptorPath(context, allowFdFallback = false)
        ?: targetUri.tryDownloadsToMediaFallback(context, allowFdFallback = false)
        ?: targetUri.tryMediaStoreQuery(context)
        ?: targetUri.tryDocumentUriParsing(context)
    else -> null
  }
}

/**
 * Resolves downloads URIs (e.g. content://media/external/downloads/<id>,
 * content://downloads/public_downloads/<id>, or document URIs) to MediaStore.Video.Media
 * or MediaStore.Audio.Media URIs where the app has full READ_MEDIA_VIDEO / READ_MEDIA_AUDIO permissions.
 *
 * IMPORTANT: This function ONLY applies to genuine downloads/document URIs.
 * It must NOT activate for normal MediaStore Video/Audio URIs — doing so would try
 * to open a random ID from the wrong collection and cause SecurityExceptions.
 */
internal fun Uri.resolveDownloadsUri(context: Context): Uri? {
  if (!scheme.equals("content", ignoreCase = true)) return null
  val uriString = toString()

  val isDownloadsUri = uriString.contains("/downloads/", ignoreCase = true) ||
    uriString.contains("/download/", ignoreCase = true) ||
    authority?.contains("downloads", ignoreCase = true) == true

  // Fast-path: standard MediaStore URIs (video, audio, images) — do NOT touch.
  // Downloads URIs must proceed to resolution below.
  if (!isDownloadsUri && (
    uriString.startsWith(MediaStore.Video.Media.EXTERNAL_CONTENT_URI.toString(), ignoreCase = true) ||
    uriString.startsWith(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.toString(), ignoreCase = true) ||
    uriString.startsWith(MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString(), ignoreCase = true) ||
    (uriString.startsWith("content://media/", ignoreCase = true) && !uriString.contains("download", ignoreCase = true))
  )) {
    return null // callers use the original URI as-is
  }

  // Strategy 1: SAF/Downloads document provider URIs
  val isDocumentUri = runCatching { DocumentsContract.isDocumentUri(context, this) }.getOrDefault(false)
  if (isDocumentUri) {
    val docId = runCatching { DocumentsContract.getDocumentId(this) }.getOrNull()
    if (!docId.isNullOrBlank()) {
      if (docId.startsWith("raw:")) {
        val rawPath = docId.removePrefix("raw:")
        if (File(rawPath).exists()) return Uri.fromFile(File(rawPath))
      }
      val numericId = when {
        docId.startsWith("msf:") -> docId.removePrefix("msf:").toLongOrNull()
        docId.all { it.isDigit() } -> docId.toLongOrNull()
        else -> null
      }
      if (numericId != null) {
        val mediaUri = resolveMediaStoreId(context, numericId)
          ?: resolveFromDownloadManager(context, numericId)
        if (mediaUri != null) return mediaUri
      }
    }
  }

  // Strategy 2: MediaStore Downloads URI (content://media/external/downloads/<id>)
  // Only activate for URIs with "downloads" explicitly in path or authority.
  if (isDownloadsUri) {
    // 2a. Primary: Try direct file descriptor real path resolution (fast and matches mpvRx)
    runCatching {
      context.contentResolver.openFileDescriptor(this, "r")?.use { pfd ->
        val path = Utils.findRealPath(pfd.fd)
        if (!path.isNullOrBlank() && File(path).exists()) {
          Log.d(TAG, "resolveDownloadsUri via file descriptor: $path")
          return Uri.fromFile(File(path))
        }
      }
    }

    // 2b. Query MediaStore for direct DATA path
    runCatching {
      context.contentResolver.query(
        this,
        arrayOf(MediaStore.MediaColumns.DATA),
        null,
        null,
        null,
      )?.use { cursor ->
        if (cursor.moveToFirst()) {
          val dataIdx = cursor.getColumnIndex(MediaStore.MediaColumns.DATA)
          if (dataIdx >= 0) {
            val dataPath = cursor.getString(dataIdx)
            if (!dataPath.isNullOrBlank() && File(dataPath).exists()) {
              Log.d(TAG, "resolveDownloadsUri via MediaStore DATA: $dataPath")
              return Uri.fromFile(File(dataPath))
            }
          }
        }
      }
    }

    // 2c. Fallback to numeric ID mapping in Video/Audio collections or DownloadManager
    val numericId = lastPathSegment?.toLongOrNull()
    if (numericId != null) {
      val mediaUri = resolveMediaStoreId(context, numericId)
        ?: resolveFromDownloadManager(context, numericId)
      if (mediaUri != null) return mediaUri
    }
  }

  // Strategy 3: Full DATA/DISPLAY_NAME query — only for non-standard content providers.
  // Skip for content://media/ URIs since those are already correctly addressed.
  if (!uriString.contains("content://media/", ignoreCase = true)) {
    return runCatching {
      var displayName: String? = null
      var dataPath: String? = null
      var size: Long? = null
      context.contentResolver.query(
        this,
        arrayOf(
          MediaStore.MediaColumns.DISPLAY_NAME,
          MediaStore.MediaColumns.DATA,
          MediaStore.MediaColumns.SIZE,
        ),
        null,
        null,
        null,
      )?.use { cursor ->
        if (cursor.moveToFirst()) {
          val nameIdx = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
          if (nameIdx >= 0) displayName = cursor.getString(nameIdx)
          val dataIdx = cursor.getColumnIndex(MediaStore.MediaColumns.DATA)
          if (dataIdx >= 0) dataPath = cursor.getString(dataIdx)
          val sizeIdx = cursor.getColumnIndex(MediaStore.MediaColumns.SIZE)
          if (sizeIdx >= 0) size = cursor.getLong(sizeIdx)
        }
      }
      if (dataPath != null && File(dataPath).exists()) {
        Uri.fromFile(File(dataPath))
      } else {
        findInMediaCollection(context, MediaStore.Video.Media.EXTERNAL_CONTENT_URI, dataPath, displayName, size)
          ?: findInMediaCollection(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, dataPath, displayName, size)
      }
    }.getOrNull()
  }

  return null
}


private fun resolveMediaStoreId(context: Context, id: Long): Uri? {
  // 1. Try MediaStore.Video.Media by descriptor open
  val videoUri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)
  val canOpenVideo = runCatching {
    context.contentResolver.openFileDescriptor(videoUri, "r")?.use { true } ?: false
  }.getOrDefault(false)
  if (canOpenVideo) return videoUri

  // 2. Try MediaStore.Audio.Media by descriptor open
  val audioUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
  val canOpenAudio = runCatching {
    context.contentResolver.openFileDescriptor(audioUri, "r")?.use { true } ?: false
  }.getOrDefault(false)
  if (canOpenAudio) return audioUri

  // 3. Try query on Video collection
  val videoExists = runCatching {
    context.contentResolver.query(
      videoUri,
      arrayOf(MediaStore.MediaColumns._ID),
      null,
      null,
      null,
    )?.use { it.moveToFirst() } ?: false
  }.getOrDefault(false)
  if (videoExists) return videoUri

  // 4. Try query on Audio collection
  val audioExists = runCatching {
    context.contentResolver.query(
      audioUri,
      arrayOf(MediaStore.MediaColumns._ID),
      null,
      null,
      null,
    )?.use { it.moveToFirst() } ?: false
  }.getOrDefault(false)
  if (audioExists) return audioUri

  return null
}

private fun resolveFromDownloadManager(context: Context, downloadId: Long): Uri? =
  runCatching {
    val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager ?: return@runCatching null
    dm.query(DownloadManager.Query().setFilterById(downloadId))?.use { cursor ->
      if (cursor.moveToFirst()) {
        val mediaUriIdx = cursor.getColumnIndex(DownloadManager.COLUMN_MEDIAPROVIDER_URI)
        if (mediaUriIdx >= 0) {
          val mediaUriStr = cursor.getString(mediaUriIdx)
          if (!mediaUriStr.isNullOrBlank()) {
            val parsed = Uri.parse(mediaUriStr)
            val resolved = parsed.resolveDownloadsUri(context) ?: parsed
            return@runCatching resolved
          }
        }
        val localUriIdx = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
        if (localUriIdx >= 0) {
          val localUriStr = cursor.getString(localUriIdx)
          if (!localUriStr.isNullOrBlank()) {
            val parsed = Uri.parse(localUriStr)
            val resolved = parsed.resolveDownloadsUri(context) ?: parsed
            return@runCatching resolved
          }
        }
      }
      null
    }
  }.getOrNull()

/**
 * Method 1: Extract real filesystem path from file descriptor.
 * Works best for most content URIs on modern Android.
 * If the resolved path is not readable directly (e.g. 0 permissions granted),
 * returns the detached file descriptor so MPV can play via fd:// directly.
 * Callers that persist the result must pass [allowFdFallback] = false: a detached
 * descriptor is single-use and cannot be replayed later.
 */
private fun Uri.tryFileDescriptorPath(
  context: Context,
  allowFdFallback: Boolean = true,
): String? =
  runCatching {
    val pfd = context.contentResolver.openFileDescriptor(this, "r") ?: return null
    val path = Utils.findRealPath(pfd.fd)
    if (!path.isNullOrBlank()) {
      // Path resolved successfully — use it directly (matches mpvRx behavior).
      // Do NOT check canRead() here: scoped storage may deny POSIX read even when
      // the ContentResolver grants access. MPV's native layer handles the actual open.
      pfd.close()
      Log.d(TAG, "Resolved via file descriptor: $path")
      path
    } else if (allowFdFallback) {
      val fd = pfd.detachFd()
      Log.d(TAG, "Using file descriptor fallback (fd://$fd) for $this")
      "fd://$fd"
    } else {
      pfd.close()
      null
    }
  }.getOrNull()

/**
 * Method 2: Query MediaStore for direct file path.
 * Works for media files indexed by MediaStore (videos, music, images).
 */
private fun Uri.tryMediaStoreQuery(context: Context): String? =
  runCatching {
    context.contentResolver
      .query(this, arrayOf(MediaStore.MediaColumns.DATA), null, null, null)
      ?.use { cursor ->
        if (cursor.moveToFirst()) {
          val columnIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DATA)
          if (columnIndex != -1) {
            cursor
              .getString(columnIndex)
              ?.takeIf { path ->
                // Use exists() not canRead(): scoped storage can deny POSIX read
                // even for valid, accessible files. MPV opens the path natively.
                path.isNotBlank() && File(path).exists()
              }?.also {
                Log.d(TAG, "Resolved via MediaStore: $it")
              }
          } else {
            null
          }
        } else {
          null
        }
      }
  }.onFailure { e ->
    Log.d(TAG, "MediaStore query failed: ${e.message}")
  }.getOrNull()

/**
 * Method 2b: Fallback for MediaStore Downloads URIs (content://media/external/downloads/...).
 * Resolves to MediaStore.Video.Media or MediaStore.Audio.Media.
 */
private fun Uri.tryDownloadsToMediaFallback(
  context: Context,
  allowFdFallback: Boolean = true,
): String? {
  val resolvedUri = resolveDownloadsUri(context)
  if (resolvedUri != null && resolvedUri != this) {
    return resolvedUri.tryFileDescriptorPath(context, allowFdFallback)
      ?: resolvedUri.tryMediaStoreQuery(context)
  }
  return null
}

private fun findInMediaCollection(
  context: Context,
  collection: Uri,
  dataPath: String?,
  displayName: String?,
  size: Long?,
): Uri? =
  runCatching {
    val selection = StringBuilder()
    val selectionArgs = mutableListOf<String>()

    if (!dataPath.isNullOrBlank()) {
      selection.append("${MediaStore.MediaColumns.DATA} = ?")
      selectionArgs.add(dataPath)
    } else if (!displayName.isNullOrBlank()) {
      selection.append("${MediaStore.MediaColumns.DISPLAY_NAME} = ?")
      selectionArgs.add(displayName)
      if (size != null && size > 0) {
        selection.append(" AND ${MediaStore.MediaColumns.SIZE} = ?")
        selectionArgs.add(size.toString())
      }
    } else {
      return@runCatching null
    }

    context.contentResolver.query(
      collection,
      arrayOf(MediaStore.MediaColumns._ID),
      selection.toString(),
      selectionArgs.toTypedArray(),
      null,
    )?.use { cursor ->
      if (cursor.moveToFirst()) {
        val idCol = cursor.getColumnIndex(MediaStore.MediaColumns._ID)
        if (idCol >= 0) {
          ContentUris.withAppendedId(collection, cursor.getLong(idCol))
        } else {
          null
        }
      } else {
        null
      }
    }
  }.getOrNull()

/**
 * Method 3: Parse DocumentsContract URIs manually.
 *
 * Document IDs have format: "storageType:path"
 * Examples:
 * - "primary:DCIM/video.mp4" → /storage/emulated/0/DCIM/video.mp4
 * - "raw:/storage/1234-5678/Movies/file.mp4" → /storage/1234-5678/Movies/file.mp4
 * - "1234-5678:Movies/video.mp4" → External SD card path
 */
private fun Uri.tryDocumentUriParsing(context: Context): String? {
  if (!DocumentsContract.isDocumentUri(context, this)) return null

  return runCatching {
    val docId = DocumentsContract.getDocumentId(this)
    Log.d(TAG, "Parsing document ID: $docId")

    when {
      docId.startsWith(StoragePaths.PRIMARY_PREFIX) -> {
        tryPrimaryStoragePath(docId)
      }
      docId.startsWith(StoragePaths.RAW_PREFIX) -> {
        tryRawPath(docId)
      }

      docId.contains(":") -> {
        tryExternalStoragePaths(docId)
      }

      else -> null
    }
  }.onFailure { e ->
    Log.d(TAG, "Document URI parsing failed: ${e.message}")
  }.getOrNull()
}

private fun tryPrimaryStoragePath(docId: String): String? {
  val path = docId.substringAfter(StoragePaths.PRIMARY_PREFIX)
  val fullPath = "${StoragePaths.PRIMARY_STORAGE}/$path"
  return fullPath.takeIf { File(it).exists() }?.also {
    Log.d(TAG, "Resolved document URI to primary storage: $it")
  }
}

private fun tryRawPath(docId: String): String? {
  val rawPath = docId.substringAfter(StoragePaths.RAW_PREFIX)
  return rawPath.takeIf { File(it).exists() }?.also {
    Log.d(TAG, "Resolved document URI from raw path: $it")
  }
}

/**
 * Tries multiple common mount points for external storage.
 * External SD cards can be mounted at different locations depending on manufacturer.
 */
private fun tryExternalStoragePaths(docId: String): String? {
  val volumeId = docId.substringBefore(":")
  val path = docId.substringAfter(":")
  val possiblePaths =
    listOf(
      "${StoragePaths.EXTERNAL_STORAGE}/$volumeId/$path",
      "${StoragePaths.MEDIA_RW}/$volumeId/$path",
      "${StoragePaths.PRIMARY_STORAGE}/$path",
      "${StoragePaths.EXTERNAL_STORAGE}/$path",
      "${StoragePaths.MEDIA_RW}/$path",
    )

  return possiblePaths.firstOrNull { File(it).exists() }?.also {
    Log.d(TAG, "Resolved document URI to: $it")
  }
}

/**
 * Fallback: Return file descriptor URI.
 * MPV can play directly from fd:// when filesystem path is unavailable.
 * Common with scoped storage on Android 11+.
 */
@SuppressLint("Recycle")
private fun Uri.tryFileDescriptorFallback(context: Context): String? =
  runCatching {
    context.contentResolver.openFileDescriptor(this, "r")?.detachFd()?.let { fd ->
      "fd://$fd".also {
        Log.d(TAG, "Using file descriptor fallback: $it")
      }
    }
  }.getOrNull()

/**
 * Resolves any URI to a format MPV can play.
 *
 * Returns null if URI scheme is null or unsupported.
 */
internal fun Uri.resolveUri(
  context: Context,
  allowFdFallback: Boolean = true,
): String? {
  val resolved = resolveDownloadsUri(context) ?: this
  val currentScheme = resolved.scheme?.lowercase()
  if (currentScheme == null) {
    Log.e(TAG, "URI has null scheme: $resolved")
    return null
  }

  return when (currentScheme) {
    "file" -> resolved.path
    "content" ->
      resolved.openContentFd(context, allowFdFallback = allowFdFallback)
        ?: if (allowFdFallback) null else resolved.toString()
    "data" -> "data://${resolved.schemeSpecificPart}"
    "magnet", "torrent" -> resolved.toString()
    NetworkPlaybackUri.SCHEME -> resolved.toString()
    "http", "https", "rtmp", "rtmps", "rtsp", "rtsps", "mms", "mmsh", "udp", "tcp", "hls", "dash", "ftp", "ftps" -> resolved.toString()
    in Utils.PROTOCOLS.map { it.lowercase() } -> resolved.toString()
    else -> {
      if (resolved.isHierarchical && !resolved.isRelative) {
        resolved.toString()
      } else {
        Log.e(TAG, "Unsupported URI scheme: ${resolved.scheme}")
        null
      }
    }
  }
}

/**
 * Sanitizes JSON strings from MPV by fixing invalid escape sequences.
 *
 * MPV's C library may generate JSON with unescaped backslashes (e.g., in file paths
 * like "Signs\Songs"). This function fixes invalid escape sequences by properly
 * escaping backslashes that aren't part of valid JSON escape sequences.
 *
 * Valid JSON escape sequences: \" \\ \/ \b \f \n \r \t \uXXXX
 */
fun sanitizeJsonString(jsonString: String): String {
  val result = StringBuilder(jsonString.length)
  var i = 0
  var inString = false

  while (i < jsonString.length) {
    val char = jsonString[i]

    when {
      // Track if we're inside a string literal
      char == '"' && (i == 0 || jsonString[i - 1] != '\\') -> {
        inString = !inString
        result.append(char)
        i++
      }
      // Handle backslashes inside string literals
      char == '\\' && inString && i + 1 < jsonString.length -> {
        val nextChar = jsonString[i + 1]

        // Check if this is a valid escape sequence
        val isValidEscape =
          when (nextChar) {
            '"', '\\', '/', 'b', 'f', 'n', 'r', 't' -> true
            'u' -> i + 5 < jsonString.length // \uXXXX format
            else -> false
          }

        if (isValidEscape) {
          // Valid escape sequence, keep as-is
          result.append(char)
          i++
        } else {
          // Invalid escape sequence, escape the backslash
          result.append("\\\\")
          i++
        }
      }
      else -> {
        result.append(char)
        i++
      }
    }
  }

  return result.toString()
}

/**
 * Deserializes MPV's native node structure to Kotlin data classes.
 * MPV uses C-style tree structures (MPVNode) which we convert to typed objects.
 *
 * Sanitizes the JSON before parsing to handle invalid escape sequences from MPV.
 */
inline fun <reified T> MPVNode.toObject(json: Json): T {
  val jsonString = toJson()
  val sanitizedJson = sanitizeJsonString(jsonString)
  return json.decodeFromString<T>(sanitizedJson)
}
