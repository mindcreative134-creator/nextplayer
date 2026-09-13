/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package app.infinity.mpvz.ads

object AdConfig {
  // ─────────────────────────────────────────────────────────────────────────
  // AdMob App ID — must match AndroidManifest meta-data value
  // ─────────────────────────────────────────────────────────────────────────
  const val APP_ID = "ca-app-pub-9543073887536718~8714519957"

  // ─────────────────────────────────────────────────────────────────────────
  // Live Ad Unit IDs (your real production units from AdMob Console)
  // ─────────────────────────────────────────────────────────────────────────
  const val LIVE_APP_OPEN_AD_ID              = "ca-app-pub-9543073887536718/3216002450"
  const val LIVE_BANNER_AD_ID                = "ca-app-pub-9543073887536718/9781410809"
  const val LIVE_INTERSTITIAL_AD_ID          = "ca-app-pub-9543073887536718/6955936919"
  const val LIVE_NATIVE_AD_ID                = "ca-app-pub-9543073887536718/1541506865"
  const val LIVE_REWARDED_AD_ID              = "ca-app-pub-9543073887536718/8742440115"
  const val LIVE_REWARDED_INTERSTITIAL_AD_ID = "ca-app-pub-9543073887536718/2368603457"

  // ─────────────────────────────────────────────────────────────────────────
  // Google Official Test Ad Unit IDs — 100% fill rate, guaranteed
  // Use these during development/debugging so you see real ad UI without
  // risking invalid traffic on your live account.
  // ─────────────────────────────────────────────────────────────────────────
  const val TEST_APP_OPEN_AD_ID              = "ca-app-pub-3940256099942544/9257395921"
  const val TEST_BANNER_AD_ID                = "ca-app-pub-3940256099942544/6300978111"
  const val TEST_INTERSTITIAL_AD_ID          = "ca-app-pub-3940256099942544/1033173712"
  const val TEST_NATIVE_AD_ID                = "ca-app-pub-3940256099942544/2247696110"
  const val TEST_REWARDED_AD_ID              = "ca-app-pub-3940256099942544/5224354917"
  const val TEST_REWARDED_INTERSTITIAL_AD_ID = "ca-app-pub-3940256099942544/5354046379"

  // ─────────────────────────────────────────────────────────────────────────
  // TOGGLE: true = live revenue, false = test ads (safe for debugging)
  // When true and autoFallbackToTestOnNoFill is true, live ads are requested
  // first. If AdMob has no live inventory or account is pending (code 3 NO_FILL),
  // it seamlessly falls back to Google's test ad units so ads ALWAYS display.
  // ─────────────────────────────────────────────────────────────────────────
  var useLiveAds: Boolean = true

  /**
   * If true, whenever a live ad fails to load (e.g. newly created units with no fill yet),
   * the system will immediately fallback to Google official test ad unit so ads show up reliably.
   */
  var autoFallbackToTestOnNoFill: Boolean = true

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

  val rewardedAdUnitId: String
    get() = if (useLiveAds) LIVE_REWARDED_AD_ID else TEST_REWARDED_AD_ID

  val rewardedInterstitialAdUnitId: String
    get() = if (useLiveAds) LIVE_REWARDED_INTERSTITIAL_AD_ID else TEST_REWARDED_INTERSTITIAL_AD_ID

  fun getFallbackForUnit(adUnitId: String): String {
    return when (adUnitId) {
      LIVE_APP_OPEN_AD_ID -> TEST_APP_OPEN_AD_ID
      LIVE_BANNER_AD_ID -> TEST_BANNER_AD_ID
      LIVE_INTERSTITIAL_AD_ID -> TEST_INTERSTITIAL_AD_ID
      LIVE_NATIVE_AD_ID -> TEST_NATIVE_AD_ID
      LIVE_REWARDED_AD_ID -> TEST_REWARDED_AD_ID
      LIVE_REWARDED_INTERSTITIAL_AD_ID -> TEST_REWARDED_INTERSTITIAL_AD_ID
      else -> adUnitId
    }
  }

  // ─────────────────────────────────────────────────────────────────────────
  // Smart Frequency Capping — prevents irritation & invalid traffic penalties
  // ─────────────────────────────────────────────────────────────────────────

  /** Minimum time between two interstitial shows (60 seconds for comfortable test & user experience). */
  const val INTERSTITIAL_COOLDOWN_MS = 60 * 1000L

  /** Minimum playback watch time required before showing an exit interstitial (prevents ads on accidental mis-clicks). */
  const val INTERSTITIAL_MIN_WATCH_TIME_MS = 15_000L // 15 seconds

  /** Minimum time between two App Open ad shows (3 minutes) — prevents annoying users on brief app switching. */
  const val APP_OPEN_COOLDOWN_MS = 3 * 60 * 1000L // 3 minutes

  /** Show interstitial on exit after 1 video exit. Resets after each show. */
  const val INTERSTITIAL_MIN_EXITS_BEFORE_SHOW = 1

  /** App Open ads expire after 4 hours (AdMob policy requirement). */
  const val APP_OPEN_EXPIRY_HOURS = 4L

  /** Backoff before retrying a failed ad request — prevents invalid traffic floods. */
  const val AD_RETRY_BACKOFF_MS = 15_000L // 15 seconds

  /** Wait this long after a steady pause before showing the pause banner (avoids seek spam). */
  const val PAUSE_AD_GRACE_PERIOD_MS = 1_000L // 1.0 second
}
