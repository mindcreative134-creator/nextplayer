/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package app.infinity.mpvz.presentation.crash

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import java.io.PrintWriter
import java.io.StringWriter
import java.io.File
import kotlin.system.exitProcess

class GlobalExceptionHandler(
  private val context: Context,
  private val activity: Class<*>,
) : Thread.UncaughtExceptionHandler {
  override fun uncaughtException(
    t: Thread,
    e: Throwable,
  ) {
    try {
      // Persist a crash log file so the crash screen and debug-log viewer can surface it.
      persistCrashLog(e)

      val intent = Intent(context, activity)
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
      val stackTrace = e.stackTraceToString()
      val trimmedTrace =
        if (stackTrace.length > 50_000) {
          stackTrace.take(50_000) + "\n...[truncated]"
        } else {
          stackTrace
        }
      intent.putExtra("exception", trimmedTrace)
      context.startActivity(intent)
    } catch (_: Throwable) {
      // Prevent secondary crash loops or system ANR stalls if CrashActivity fails to start
    }

    // Give the CrashActivity a short grace window to launch and render before the process exit.
    // Calling exitProcess() immediately can cut off the just-started activity, leaving the user
    // staring at a dead app and the OS reporting "App Not Responding".
    val crashStartUptime = SystemClock.uptimeMillis()
    Handler(Looper.getMainLooper()).postDelayed({
      if (SystemClock.uptimeMillis() - crashStartUptime >= 300L) {
        exitProcess(0)
      }
    }, 1500L)
  }

  private fun persistCrashLog(e: Throwable) {
    try {
      val dir = File(context.filesDir, "crashlogs")
      if (!dir.exists()) dir.mkdirs()
      val sw = StringWriter()
      e.printStackTrace(PrintWriter(sw))
      val target = File(dir, "last_crash.txt")
      target.writeText(
        "---- Crash at ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US).format(java.util.Date())} ----\n" +
          sw.toString(),
      )
    } catch (_: Throwable) {
      // Best-effort only
    }
  }
}
