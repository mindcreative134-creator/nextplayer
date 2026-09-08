/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package app.infinity.mpvz.network

import android.content.Context
import android.os.Build
import android.os.Looper
import android.webkit.WebSettings
import app.infinity.mpvz.BuildConfig

/** Uses the installed Android WebView identity when available, with a truthful app fallback. */
object NetworkUserAgent {
  @Volatile private var cachedWebViewUserAgent: String? = null

  fun resolve(
    context: Context,
    customUserAgent: String? = null,
  ): String {
    customUserAgent?.trim()?.takeIf(String::isNotEmpty)?.let { return it }
    cachedWebViewUserAgent?.let { return it }

    // WebSettings.getDefaultUserAgent() can synchronously initialize the WebView, which is slow
    // enough to trigger an "App Not Responding" dialog if called on the main thread. Only use it
    // off the main thread; otherwise fall straight back to the lightweight app identity.
    if (Looper.myLooper() != Looper.getMainLooper()) {
      runCatching { WebSettings.getDefaultUserAgent(context.applicationContext) }
        .getOrNull()
        ?.takeIf(String::isNotBlank)
        ?.let { userAgent ->
          cachedWebViewUserAgent = userAgent
          return userAgent
        }
    }

    return "NextPlayer/${BuildConfig.VERSION_NAME} (Android ${Build.VERSION.RELEASE}; ${Build.MANUFACTURER} ${Build.MODEL})"
  }
}
