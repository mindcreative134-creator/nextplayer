/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package app.infinity.mpvz.utils.permission

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import app.infinity.mpvz.BuildConfig
import app.infinity.mpvz.domain.media.model.Video
import app.infinity.mpvz.utils.history.RecentlyPlayedOps
import app.infinity.mpvz.utils.media.MediaLibraryEvents
import app.infinity.mpvz.utils.media.PlaybackStateOps
import app.infinity.mpvz.utils.storage.FileTypeUtils
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Simplified storage permission utilities with MANAGE_EXTERNAL_STORAGE support.
 */
object PermissionUtils {
  private const val FILE_ACCESS_TAG = "FileAccessRequest"

  private var mediaRequestLauncher: ActivityResultLauncher<IntentSenderRequest>? = null
  private var resultOkCallback: () -> Unit = {}
  private var resultCancelledCallback: () -> Unit = {}

  /**
   * Set the media access launcher from MainActivity.
   */
  fun setMediaAccessLauncher(launcher: ActivityResultLauncher<IntentSenderRequest>) {
    mediaRequestLauncher = launcher
  }

  /**
   * Handle result from MainActivity's launcher callback.
   */
  fun handleMediaAccessResult(resultCode: Int) {
    if (resultCode == Activity.RESULT_OK) {
      resultOkCallback()
    } else {
      resultCancelledCallback()
    }
  }

  suspend fun requestScopedWriteAccess(
    context: Context,
    uris: List<Uri>,
  ): Boolean = requestWriteAccess(context, uris)

  suspend fun requestScopedDeleteAccess(
    context: Context,
    uris: List<Uri>,
  ): Boolean = requestDeleteAccess(context, uris)

  private suspend fun requestWriteAccess(
    context: Context,
    uris: List<Uri>,
  ): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R || uris.isEmpty()) return true
    return withContext(Dispatchers.Main) {
      suspendCancellableCoroutine { continuation ->
        val launcher =
          mediaRequestLauncher ?: run {
            continuation.resumeWith(Result.success(false))
            return@suspendCancellableCoroutine
          }

        resultOkCallback = { continuation.resumeWith(Result.success(true)) }
        resultCancelledCallback = { continuation.resumeWith(Result.success(false)) }

        val pendingIntent = MediaStore.createWriteRequest(context.contentResolver, uris)
        launcher.launch(IntentSenderRequest.Builder(pendingIntent).build())
      }
    }
  }

  private suspend fun requestDeleteAccess(
    context: Context,
    uris: List<Uri>,
  ): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R || uris.isEmpty()) return true
    return withContext(Dispatchers.Main) {
      suspendCancellableCoroutine { continuation ->
        val launcher =
          mediaRequestLauncher ?: run {
            continuation.resumeWith(Result.success(false))
            return@suspendCancellableCoroutine
          }

        resultOkCallback = { continuation.resumeWith(Result.success(true)) }
        resultCancelledCallback = { continuation.resumeWith(Result.success(false)) }

        val pendingIntent = MediaStore.createDeleteRequest(context.contentResolver, uris)
        launcher.launch(IntentSenderRequest.Builder(pendingIntent).build())
      }
    }
  }

  /**
   * Returns storage permission for the current Android version.
   * On Android 13+, requests media-specific permissions.
   * On Android 12 and below (including Android 9 / API 28), requests READ_EXTERNAL_STORAGE.
   */
  fun getStoragePermission(audioOnly: Boolean = false): String =
    when {
      Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
        if (audioOnly) {
          android.Manifest.permission.READ_MEDIA_AUDIO
        } else {
          android.Manifest.permission.READ_MEDIA_VIDEO
        }
      }

      else -> android.Manifest.permission.READ_EXTERNAL_STORAGE
    }

  /**
   * Checks if storage read permission is granted across all Android versions.
   * On Android <= 32 (including Android 9 / Xiaomi Redmi 6 Pro), having EITHER
   * READ_EXTERNAL_STORAGE or WRITE_EXTERNAL_STORAGE grants full media access.
   */
  fun hasStoragePermission(context: Context, audioOnly: Boolean = false): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      val perm = if (audioOnly) android.Manifest.permission.READ_MEDIA_AUDIO else android.Manifest.permission.READ_MEDIA_VIDEO
      return androidx.core.content.ContextCompat.checkSelfPermission(context, perm) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && android.os.Environment.isExternalStorageManager()) {
      return true
    }
    return androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_EXTERNAL_STORAGE) == android.content.pm.PackageManager.PERMISSION_GRANTED ||
      androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == android.content.pm.PackageManager.PERMISSION_GRANTED
  }

  /**
   * Creates a permission state for storage access.
   */
  @OptIn(ExperimentalPermissionsApi::class)
  @Composable
  fun rememberStoragePermissionState(audioOnly: Boolean = false): PermissionState =
    rememberPermissionState(getStoragePermission(audioOnly))

  /**
   * Opens application details settings in the Android system.
   * Essential when permissions are permanently denied or suppressed by OEM managers like Xiaomi MIUI.
   */
  fun openAppSettings(context: Context) {
    try {
      val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = android.net.Uri.parse("package:${context.packageName}")
        addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(intent)
    } catch (e: Exception) {
      Log.e(FILE_ACCESS_TAG, "Failed to open app details settings", e)
      try {
        val fallback = android.content.Intent(android.provider.Settings.ACTION_SETTINGS).apply {
          addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(fallback)
      } catch (_: Exception) {}
    }
  }

  /**
   * Handles storage permission and invokes [onPermissionGranted] when granted.
   * On Android 11+, also checks MANAGE_EXTERNAL_STORAGE permission.
   * On Android <= 29 (e.g. Xiaomi Redmi 6 Pro on Android 9), requests both READ and WRITE.
   *
   * @param audioOnly When true, requests READ_MEDIA_AUDIO instead of READ_MEDIA_VIDEO on Android 13+,
   * for screens that only browse the audio library (e.g. Music > Folders).
   */
  @OptIn(ExperimentalPermissionsApi::class)
  @Composable
  fun handleStoragePermission(
    audioOnly: Boolean = false,
    onPermissionGranted: () -> Unit,
  ): PermissionState {
    val permissionState = rememberStoragePermissionState(audioOnly)
    val context = LocalContext.current
    var lifecycleTrigger by remember { mutableIntStateOf(0) }

    // Re-check permission when app resumes from Settings
    DisposableEffect(Unit) {
      val lifecycleOwner = context as? LifecycleOwner
      val observer =
        LifecycleEventObserver { _, event ->
          if (event == Lifecycle.Event.ON_RESUME) {
            lifecycleTrigger++
          }
        }
      lifecycleOwner?.lifecycle?.addObserver(observer)
      onDispose {
        lifecycleOwner?.lifecycle?.removeObserver(observer)
      }
    }

    // Wrap permission state to consider MANAGE_EXTERNAL_STORAGE on Android 11+ and
    // existing READ/WRITE storage permissions on Android <= 32 (e.g. Android 9 on Xiaomi)
    val effectivePermissionState =
      remember(permissionState.status, lifecycleTrigger) {
        if (hasStoragePermission(context, audioOnly)) {
          object : PermissionState {
            override val permission = permissionState.permission
            override val status = PermissionStatus.Granted

            override fun launchPermissionRequest() = permissionState.launchPermissionRequest()
          }
        } else if (!BuildConfig.SCOPED_STORAGE_ONLY && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
          if (android.os.Environment.isExternalStorageManager()) {
            object : PermissionState {
              override val permission = permissionState.permission
              override val status = PermissionStatus.Granted

              override fun launchPermissionRequest() = permissionState.launchPermissionRequest()
            }
          } else {
            object : PermissionState {
              override val permission = permissionState.permission
              override val status = PermissionStatus.Denied(false)

              override fun launchPermissionRequest() = permissionState.launchPermissionRequest()
            }
          }
        } else {
          object : PermissionState {
            override val permission = permissionState.permission
            override val status = permissionState.status

            override fun launchPermissionRequest() {
              val activity = context as? Activity
              if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q && activity != null) {
                // Android 9 & 10 (e.g. Xiaomi Redmi 6 Pro / MIUI): request both READ and WRITE
                androidx.core.app.ActivityCompat.requestPermissions(
                  activity,
                  arrayOf(
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                  ),
                  1001,
                )
              } else {
                permissionState.launchPermissionRequest()
              }
            }
          }
        }
      }

    LaunchedEffect(effectivePermissionState.status) {
      if (effectivePermissionState.status == PermissionStatus.Granted) {
        onPermissionGranted()
      }
    }

    return effectivePermissionState
  }

  // --------------------------------------------------------------------------
  // Storage operations (automatically uses scoped storage or direct file access)
  // --------------------------------------------------------------------------

  object StorageOps {
    private const val TAG = "StorageOps"

    /**
     * Check if MANAGE_EXTERNAL_STORAGE permission is available and granted
     */
    private fun hasManageStoragePermission(): Boolean =
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        android.os.Environment.isExternalStorageManager()
      } else {
        true // Pre-Android 11 devices don't need this permission
      }

    /**
     * Delete videos using direct file operations (requires MANAGE_EXTERNAL_STORAGE on Android 11+)
     */
    suspend fun deleteVideos(
      context: Context,
      videos: List<Video>,
    ): Pair<Int, Int> =
      withContext(Dispatchers.IO) {
        if (!BuildConfig.SCOPED_STORAGE_ONLY || hasManageStoragePermission()) {
          deleteVideosDirectly(context, videos)
        } else {
          deleteVideosScoped(context, videos)
        }
      }

    /**
     * Delete videos using direct file operations (requires MANAGE_EXTERNAL_STORAGE on Android 11+)
     */
    private suspend fun deleteVideosDirectly(
      context: Context,
      videos: List<Video>,
    ): Pair<Int, Int> =
      withContext(Dispatchers.IO) {
        var deleted = 0
        var failed = 0
        val deletedPaths = mutableListOf<String>()

        for (video in videos) {
          try {
            val file = File(video.path)
            if (file.exists() && file.delete()) {
              deleted++
              deletedPaths += video.path
              RecentlyPlayedOps.onVideoDeleted(video.path)
              PlaybackStateOps.onVideoDeleted(video.path)
              Log.d(TAG, "✓ Deleted: ${video.displayName}")
            } else {
              failed++
              Log.w(TAG, "✗ Failed to delete: ${video.displayName}")
            }
          } catch (e: Exception) {
            failed++
            Log.e(TAG, "✗ Error deleting ${video.displayName}", e)
          }
        }

        // Notify that media library has changed
        if (deleted > 0) {
          scanChangedPaths(context, deletedPaths)
          MediaLibraryEvents.notifyChanged()
        }

        Pair(deleted, failed)
      }

    /**
     * Delete videos via scoped storage (Play Store flavor / no MANAGE_EXTERNAL_STORAGE)
     */
    private suspend fun deleteVideosScoped(
      context: Context,
      videos: List<Video>,
    ): Pair<Int, Int> =
      withContext(Dispatchers.IO) {
        var deleted = 0
        var failed = 0
        val deletedFilePaths = mutableListOf<String>()

        val contentVideos = videos.filter { it.uri.scheme == "content" }
        val fileVideos = videos.filter { it.uri.scheme != "content" }

        if (contentVideos.isNotEmpty() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
          val granted = requestDeleteAccess(context, contentVideos.map { it.uri })
          if (granted) {
            contentVideos.forEach { video ->
              deleted++
              RecentlyPlayedOps.onVideoDeleted(video.path)
              PlaybackStateOps.onVideoDeleted(video.path)
              Log.d(TAG, "✓ Deleted (scoped request): ${video.displayName}")
            }
          } else {
            failed += contentVideos.size
            Log.w(TAG, "✗ Delete request denied/cancelled for ${contentVideos.size} item(s)")
          }
        } else {
          for (video in contentVideos) {
            try {
              val rows = context.contentResolver.delete(video.uri, null, null)
              if (rows > 0) {
                deleted++
                RecentlyPlayedOps.onVideoDeleted(video.path)
                PlaybackStateOps.onVideoDeleted(video.path)
                Log.d(TAG, "✓ Deleted (scoped): ${video.displayName}")
              } else {
                failed++
                Log.w(TAG, "✗ Failed to delete (scoped): ${video.displayName}")
              }
            } catch (e: Exception) {
              failed++
              Log.e(TAG, "✗ Error deleting (scoped) ${video.displayName}", e)
            }
          }
        }

        for (video in fileVideos) {
          try {
            val file = File(video.path)
            if (!file.exists() || file.delete()) {
              deleted++
              deletedFilePaths += video.path
              RecentlyPlayedOps.onVideoDeleted(video.path)
              PlaybackStateOps.onVideoDeleted(video.path)
              Log.d(TAG, "✓ Deleted (file fallback): ${video.displayName}")
            } else {
              failed++
              Log.w(TAG, "✗ Failed to delete (file fallback): ${video.displayName}")
            }
          } catch (e: Exception) {
            failed++
            Log.e(TAG, "✗ Error deleting (file fallback) ${video.displayName}", e)
          }
        }

        if (deleted > 0) {
          scanChangedPaths(context, deletedFilePaths)
          MediaLibraryEvents.notifyChanged()
        }

        Pair(deleted, failed)
      }

    /**
     * Rename video using direct file operations (requires MANAGE_EXTERNAL_STORAGE on Android 11+)
     */
    suspend fun renameVideo(
      context: Context,
      video: Video,
      newDisplayName: String,
    ): Result<Unit> =
      withContext(Dispatchers.IO) {
        if (!isValidFileName(newDisplayName)) {
          return@withContext Result.failure(IllegalArgumentException("Invalid file name"))
        }
        if (!BuildConfig.SCOPED_STORAGE_ONLY || hasManageStoragePermission()) {
          renameVideoDirectly(context, video, newDisplayName)
        } else {
          renameVideoScoped(context, video, newDisplayName)
        }
      }

    /**
     * Rename video using direct file operations (requires MANAGE_EXTERNAL_STORAGE on Android 11+)
     */
    private suspend fun renameVideoDirectly(
      context: Context,
      video: Video,
      newDisplayName: String,
    ): Result<Unit> =
      withContext(Dispatchers.IO) {
        try {
          val oldFile = File(video.path)
          val newFile = File(oldFile.parentFile, newDisplayName)

          if (oldFile.absolutePath == newFile.absolutePath) {
            return@withContext Result.success(Unit)
          }

          if (newFile.exists()) {
            return@withContext Result.failure(FileAlreadyExistsException(newFile, null, "Target file already exists"))
          }

          if (oldFile.exists() && oldFile.renameTo(newFile)) {
            // Update history
            RecentlyPlayedOps.onVideoRenamed(oldFile.absolutePath, newFile.absolutePath)
            PlaybackStateOps.onVideoRenamed(oldFile.absolutePath, newFile.absolutePath)

            // Notify that media library has changed
            MediaLibraryEvents.notifyChanged()

            Log.d(TAG, "✓ Renamed: ${video.displayName} -> $newDisplayName")
            try {
              android.media.MediaScannerConnection.scanFile(
                context,
                arrayOf(oldFile.absolutePath, newFile.absolutePath),
                null,
                null,
              )
            } catch (e: Exception) {
              Log.w(TAG, "Media scan failed after rename: ${e.message}")
            }
            Result.success(Unit)
          } else {
            Log.w(TAG, "✗ Rename failed: ${video.displayName}")
            Result.failure(IllegalStateException("Rename operation failed"))
          }
        } catch (e: Exception) {
          Log.e(TAG, "✗ Error renaming ${video.displayName}", e)
          Result.failure(e)
        }
      }

    suspend fun renameFolder(
      context: Context,
      sourcePath: String,
      newName: String,
    ): Boolean =
      withContext(Dispatchers.IO) {
        val source = File(sourcePath)
        val parent = source.parentFile ?: return@withContext false
        if (!isValidFileName(newName)) return@withContext false
        val destination = File(parent, newName)
        if (!source.isDirectory) return@withContext false
        if (source.absolutePath == destination.absolutePath) return@withContext true
        if (destination.exists()) return@withContext false

        val mediaPaths =
          source
            .walkTopDown()
            .filter { file ->
              file.isFile &&
                (file.extension.lowercase() in FileTypeUtils.VIDEO_EXTENSIONS ||
                  file.extension.lowercase() in FileTypeUtils.AUDIO_EXTENSIONS)
            }.map { file ->
              file.absolutePath to File(destination, file.relativeTo(source).path).absolutePath
            }.toList()

        if (!source.renameTo(destination)) return@withContext false

        mediaPaths.forEach { (oldPath, newPath) ->
          RecentlyPlayedOps.onVideoRenamed(oldPath, newPath)
          PlaybackStateOps.onVideoRenamed(oldPath, newPath)
        }
        scanChangedPaths(context, mediaPaths.flatMap { (oldPath, newPath) -> listOf(oldPath, newPath) })
        MediaLibraryEvents.notifyChanged()
        true
      }

    private fun scanChangedPaths(
      context: Context,
      paths: Collection<String>,
    ) {
      if (paths.isEmpty()) return
      runCatching {
        android.media.MediaScannerConnection.scanFile(context, paths.distinct().toTypedArray(), null, null)
      }.onFailure { error ->
        Log.w(TAG, "Media scan failed after storage operation", error)
      }
    }

    private fun isValidFileName(name: String): Boolean =
      name.isNotBlank() &&
        name != "." &&
        name != ".." &&
        '/' !in name &&
        '\\' !in name &&
        '\u0000' !in name

    /**
     * Rename video via MediaStore update (scoped storage path)
     */
    private suspend fun renameVideoScoped(
      context: Context,
      video: Video,
      newDisplayName: String,
    ): Result<Unit> =
      withContext(Dispatchers.IO) {
        try {
          if (video.uri.scheme != "content") {
            return@withContext renameVideoDirectly(context, video, newDisplayName)
          }

          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val granted = requestWriteAccess(context, listOf(video.uri))
            if (!granted) {
              Log.w(TAG, "✗ Rename request denied/cancelled: ${video.displayName}")
              return@withContext Result.failure(SecurityException("Rename permission denied"))
            }
          }

          val values =
            ContentValues().apply {
              put(MediaStore.MediaColumns.DISPLAY_NAME, newDisplayName)
            }

          val updated = context.contentResolver.update(video.uri, values, null, null)
          if (updated > 0) {
            val oldPath = video.path
            val parent = File(oldPath).parentFile
            val newPath = parent?.let { File(it, newDisplayName).absolutePath } ?: oldPath

            RecentlyPlayedOps.onVideoRenamed(oldPath, newPath)
            PlaybackStateOps.onVideoRenamed(oldPath, newPath)
            MediaLibraryEvents.notifyChanged()

            Log.d(TAG, "✓ Renamed (scoped): ${video.displayName} -> $newDisplayName")
            Result.success(Unit)
          } else {
            Log.w(TAG, "✗ Rename failed (scoped): ${video.displayName}")
            Result.failure(IllegalStateException("Rename operation failed"))
          }
        } catch (e: Exception) {
          Log.e(TAG, "✗ Error renaming (scoped) ${video.displayName}", e)
          Result.failure(e)
        }
      }
  }
}
