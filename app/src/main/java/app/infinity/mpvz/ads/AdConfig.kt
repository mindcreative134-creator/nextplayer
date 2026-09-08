/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package app.infinity.mpvz.ads

object AdConfig {
  // ─────────────────────────────────────────────────────────────────────────
  // AdMob App ID — must match AndroidManifest meta-data value
  // ─────────────────────────────────────────────────────────────────────────
  const val APP_ID = "ca-app-pub-8615789090438802~6576899360"

  // ─────────────────────────────────────────────────────────────────────────
  // Live Ad Unit IDs (your real production units from AdMob Console)
  // ─────────────────────────────────────────────────────────────────────────
  const val LIVE_APP_OPEN_AD_ID     = "ca-app-pub-8615789090438802/5156746147"
  const val LIVE_BANNER_AD_ID       = "ca-app-pub-8615789090438802/1492093645"
  const val LIVE_INTERSTITIAL_AD_ID = "ca-app-pub-8615789090438802/3903868099"
  const val LIVE_NATIVE_AD_ID       = "ca-app-pub-8615789090438802/1712158259"

  // ─────────────────────────────────────────────────────────────────────────
  // Google Official Test Ad Unit IDs — 100% fill rate, guaranteed
  // Use these during development/debugging so you see real ad UI without
  // risking invalid traffic on your live account.
  // ─────────────────────────────────────────────────────────────────────────
  const val TEST_APP_OPEN_AD_ID     = "ca-app-pub-3940256099942544/9257395921"
  const val TEST_BANNER_AD_ID       = "ca-app-pub-3940256099942544/6300978111"
  const val TEST_INTERSTITIAL_AD_ID = "ca-app-pub-3940256099942544/1033173712"
  const val TEST_NATIVE_AD_ID       = "ca-app-pub-3940256099942544/2247696110"

  // ─────────────────────────────────────────────────────────────────────────
  // TOGGLE: true = live revenue, false = test ads (safe for debugging)
  // Set to FALSE before installing on your own device for testing!
  // Set to TRUE only for production/Play Store signed builds.
  // ─────────────────────────────────────────────────────────────────────────
  var useLiveAds: Boolean = true

  // Optional: add your device's hashed ID here to force test mode on that device
  // even when useLiveAds = true. Get your hash from logcat: "Use RequestConfiguration..."
  val testDeviceHashedIds: List<String> = listOf()

  val isTestMode: Boolean
    get() = !useLiveAds

  // ─────────────────────────────────────────────────────────────────────────
  // Active Ad Unit IDs (auto-selects live vs test based on useLiveAds flag)
  // ─────────────────────────────────────────────────────────────────────────
  val appOpenAdUnitId: String
    get() = if (useLiveAds) LIVE_APP_OPEN_AD_ID else TEST_APP_OPEN_AD_ID

  val bannerAdUnitId: String
    get() = if (useLiveAds) LIVE_BANNER_AD_ID else TEST_BANNER_AD_ID

  val interstitialAdUnitId: String
    get() = if (useLiveAds) LIVE_INTERSTITIAL_AD_ID else TEST_INTERSTITIAL_AD_ID

  val nativeAdUnitId: String
    get() = if (useLiveAds) LIVE_NATIVE_AD_ID else TEST_NATIVE_AD_ID

  // ─────────────────────────────────────────────────────────────────────────
  // Smart Frequency Capping — prevents irritation & invalid traffic penalties
  //
  // Strategy for a VIDEO PLAYER app:
  //  • Interstitial: NOT on player open (too aggressive for video apps).
  //                  ONLY on exit, every 3rd video, min 4 minutes apart.
  //  • App Open:     Cold start only (not on resume/background restore).
  //  • Pause Banner: Natural moment, user already stopped — high CTR, low annoyance.
  //  • Banner:       Always visible in browse screens — steady passive income.
  // ─────────────────────────────────────────────────────────────────────────

  /** Minimum time between two interstitial shows (4 minutes). AdMob recommends 3-5 min for video apps. */
  const val INTERSTITIAL_COOLDOWN_MS = 4 * 60 * 1000L // 4 minutes

  /** Show interstitial on exit only after this many video exits. Resets after each show. */
  const val INTERSTITIAL_MIN_EXITS_BEFORE_SHOW = 3

  /** App Open ads expire after 4 hours (AdMob policy requirement). */
  const val APP_OPEN_EXPIRY_HOURS = 4L

  /** Backoff before retrying a failed ad request — prevents invalid traffic floods. */
  const val AD_RETRY_BACKOFF_MS = 30_000L // 30 seconds

  /** Wait this long after a steady pause before showing the pause banner (avoids seek spam). */
  const val PAUSE_AD_GRACE_PERIOD_MS = 1_200L // 1.2 seconds
}
