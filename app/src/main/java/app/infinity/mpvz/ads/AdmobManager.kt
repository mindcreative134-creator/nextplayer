/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package app.infinity.mpvz.ads

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.view.ViewGroup
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import java.lang.ref.WeakReference
import java.util.Date
import java.util.concurrent.atomic.AtomicBoolean

object AdmobManager {
  private const val TAG = "AdmobManager"

  private val isInitialized = AtomicBoolean(false)

  // App Open Ad State
  private var appOpenAd: AppOpenAd? = null
  private var isAppOpenAdLoading = false
  var isShowingAppOpenAd = false
    private set
  private var appOpenLoadTime: Long = 0
  private var lastAppOpenFailTime: Long = 0
  private var coldStartActivityRef: WeakReference<Activity>? = null
  private var appLaunchTimestamp: Long = 0

  // Interstitial Ad State
  private var interstitialAd: InterstitialAd? = null
  private var isInterstitialLoading = false
  private var lastInterstitialShowTime: Long = 0L
  private var lastInterstitialFailTime: Long = 0L
  private var videoExitsCounter = 0

  // Pause Banner Ad State (preloaded and cached to prevent request storms)
  private var cachedPauseAdView: AdView? = null
  private var isPauseAdLoaded = false
  val isPauseAdReadyState = androidx.compose.runtime.mutableStateOf(false)
  private var isPauseAdLoading = false
  private var lastPauseAdLoadTime = 0L
  private var lastPauseAdFailTime = 0L
  private const val PAUSE_AD_RETRY_COOLDOWN_MS = 30_000L
  private const val PAUSE_AD_REFRESH_MIN_MS = 120_000L

  // Rewarded Ad State
  private var rewardedAd: RewardedAd? = null
  private var isRewardedLoading = false
  private var lastRewardedFailTime = 0L

  // Rewarded Interstitial Ad State
  private var rewardedInterstitialAd: RewardedInterstitialAd? = null
  private var isRewardedInterstitialLoading = false
  private var lastRewardedInterstitialFailTime = 0L

  /**
   * Initializes the Google Mobile Ads SDK asynchronously.
   */
  fun initialize(context: Context) {
    if (isInitialized.compareAndSet(false, true)) {
      appLaunchTimestamp = SystemClock.elapsedRealtime()
      lastInterstitialShowTime = 0L // Allow first exit to show interstitial
      try {
        if (AdConfig.testDeviceHashedIds.isNotEmpty()) {
          val reqConfig = RequestConfiguration.Builder()
            .setTestDeviceIds(AdConfig.testDeviceHashedIds)
            .build()
          MobileAds.setRequestConfiguration(reqConfig)
        }
        MobileAds.initialize(context) { initializationStatus ->
          Log.d(TAG, "AdMob initialized successfully: ${initializationStatus.adapterStatusMap.keys}")
          loadAppOpenAd(context.applicationContext)
          loadInterstitialAd(context.applicationContext)
          loadRewardedAd(context.applicationContext)
          preloadPauseAd(context.applicationContext)
        }
      } catch (e: Exception) {
        Log.e(TAG, "Failed to initialize AdMob", e)
      }
    }
  }

  // ==========================================
  // App Open Ad Management
  // ==========================================

  fun loadAppOpenAd(context: Context, adUnitId: String = AdConfig.appOpenAdUnitId) {
    if (isAppOpenAdLoading || isAppOpenAdAvailable()) return

    // Don't flood requests if recently failed (e.g. ad serving limit or NO_FILL)
    val timeSinceFail = SystemClock.elapsedRealtime() - lastAppOpenFailTime
    if (lastAppOpenFailTime > 0 && timeSinceFail < AdConfig.AD_RETRY_BACKOFF_MS) {
      return
    }

    isAppOpenAdLoading = true
    val request = AdRequest.Builder().build()
    Log.d(TAG, "Loading App Open Ad with unit: $adUnitId")
    AppOpenAd.load(
      context,
      adUnitId,
      request,
      object : AppOpenAd.AppOpenAdLoadCallback() {
        override fun onAdLoaded(ad: AppOpenAd) {
          Log.d(TAG, "App Open Ad loaded successfully ($adUnitId)")
          appOpenAd = ad
          appOpenLoadTime = Date().time
          isAppOpenAdLoading = false
          lastAppOpenFailTime = 0

          // If a pending cold-start activity was waiting on launch, show it immediately
          val coldAct = coldStartActivityRef?.get()
          val elapsedSinceLaunch = SystemClock.elapsedRealtime() - appLaunchTimestamp
          if (coldAct != null && !coldAct.isFinishing && !coldAct.isDestroyed && elapsedSinceLaunch < 10_000L) {
            coldStartActivityRef = null
            showAppOpenAdIfAvailable(coldAct)
          }
        }

        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
          Log.w(TAG, "App Open Ad failed to load ($adUnitId): ${loadAdError.message} (code ${loadAdError.code})")
          appOpenAd = null
          isAppOpenAdLoading = false
          lastAppOpenFailTime = SystemClock.elapsedRealtime()

          // Cascading fallback to test ad if live ad unit returns no-fill or error
          if (AdConfig.autoFallbackToTestOnNoFill && adUnitId != AdConfig.TEST_APP_OPEN_AD_ID) {
            Log.i(TAG, "Cascading to Google Test App Open ad unit (${AdConfig.TEST_APP_OPEN_AD_ID})")
            loadAppOpenAd(context, AdConfig.TEST_APP_OPEN_AD_ID)
          } else {
            coldStartActivityRef = null
          }
        }
      },
    )
  }

  private fun isAppOpenAdAvailable(): Boolean {
    val ad = appOpenAd ?: return false
    val numHoursPassed = (Date().time - appOpenLoadTime) / (1000 * 60 * 60)
    return numHoursPassed < AdConfig.APP_OPEN_EXPIRY_HOURS
  }

  fun showAppOpenAdIfAvailable(activity: Activity, onComplete: () -> Unit = {}) {
    // Never show App Open Ad during video playback or if already showing
    if (isShowingAppOpenAd) {
      onComplete()
      return
    }

    val ad = appOpenAd
    if (ad == null || !isAppOpenAdAvailable()) {
      val elapsedSinceLaunch = SystemClock.elapsedRealtime() - appLaunchTimestamp
      if (elapsedSinceLaunch < 10_000L) {
        coldStartActivityRef = WeakReference(activity)
      }
      loadAppOpenAd(activity.applicationContext)
      onComplete()
      return
    }

    if (activity.isFinishing || activity.isDestroyed) {
      onComplete()
      return
    }

    // Defer so the activity is started/resumed before the full-screen ad appears.
    Handler(Looper.getMainLooper()).post {
      if (activity.isFinishing || activity.isDestroyed) {
        onComplete()
        return@post
      }
      ad.fullScreenContentCallback = object : FullScreenContentCallback() {
        override fun onAdDismissedFullScreenContent() {
          Log.d(TAG, "App Open Ad dismissed")
          appOpenAd = null
          isShowingAppOpenAd = false
          loadAppOpenAd(activity.applicationContext)
          onComplete()
        }

        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
          Log.w(TAG, "App Open Ad failed to show: ${adError.message}")
          appOpenAd = null
          isShowingAppOpenAd = false
          loadAppOpenAd(activity.applicationContext)
          onComplete()
        }

        override fun onAdShowedFullScreenContent() {
          Log.d(TAG, "App Open Ad showing")
          isShowingAppOpenAd = true
        }
      }

      try {
        isShowingAppOpenAd = true
        ad.show(activity)
      } catch (e: Exception) {
        Log.w(TAG, "App Open Ad could not show: ${e.message}")
        isShowingAppOpenAd = false
        appOpenAd = null
        loadAppOpenAd(activity.applicationContext)
        onComplete()
      }
    }
  }

  // ==========================================
  // Interstitial Ad Management
  // ==========================================

  fun loadInterstitialAd(context: Context, adUnitId: String = AdConfig.interstitialAdUnitId) {
    if (isInterstitialLoading || interstitialAd != null) return

    // Respect backoff to prevent invalid traffic penalties
    val timeSinceFail = SystemClock.elapsedRealtime() - lastInterstitialFailTime
    if (lastInterstitialFailTime > 0 && timeSinceFail < AdConfig.AD_RETRY_BACKOFF_MS) {
      return
    }

    isInterstitialLoading = true
    val request = AdRequest.Builder().build()
    Log.d(TAG, "Loading Interstitial Ad with unit: $adUnitId")
    InterstitialAd.load(
      context,
      adUnitId,
      request,
      object : InterstitialAdLoadCallback() {
        override fun onAdLoaded(ad: InterstitialAd) {
          Log.d(TAG, "Interstitial Ad loaded successfully ($adUnitId)")
          interstitialAd = ad
          isInterstitialLoading = false
          lastInterstitialFailTime = 0
        }

        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
          Log.w(TAG, "Interstitial Ad failed to load ($adUnitId): ${loadAdError.message} (code ${loadAdError.code})")
          interstitialAd = null
          isInterstitialLoading = false
          lastInterstitialFailTime = SystemClock.elapsedRealtime()

          // Cascading fallback to test ad if live ad unit returns no-fill or error
          if (AdConfig.autoFallbackToTestOnNoFill && adUnitId != AdConfig.TEST_INTERSTITIAL_AD_ID) {
            Log.i(TAG, "Cascading to Google Test Interstitial ad unit (${AdConfig.TEST_INTERSTITIAL_AD_ID})")
            loadInterstitialAd(context, AdConfig.TEST_INTERSTITIAL_AD_ID)
          }
        }
      },
    )
  }

  /**
   * Shows an interstitial ad when opening the player / starting video playback.
   */
  fun showInterstitialOnPlayerStart(activity: Activity, onFinished: () -> Unit = {}) {
    val ad = interstitialAd
    if (ad != null) {
      if (activity.isFinishing || activity.isDestroyed) {
        onFinished()
        return
      }
      val showWhenReady = {
        if (!activity.isFinishing && !activity.isDestroyed) {
          try {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
              override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Player Start Interstitial dismissed")
                interstitialAd = null
                lastInterstitialShowTime = SystemClock.elapsedRealtime()
                loadInterstitialAd(activity.applicationContext)
                onFinished()
              }

              override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.w(TAG, "Player Start Interstitial failed to show: ${adError.message}")
                interstitialAd = null
                loadInterstitialAd(activity.applicationContext)
                onFinished()
              }

              override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Player Start Interstitial showing")
              }
            }
            ad.show(activity)
          } catch (e: Exception) {
            Log.w(TAG, "Player Start Interstitial could not show: ${e.message}")
            interstitialAd = null
            loadInterstitialAd(activity.applicationContext)
            onFinished()
          }
        } else {
          onFinished()
        }
      }
      Handler(Looper.getMainLooper()).post {
        showWhenReady()
      }
    } else {
      loadInterstitialAd(activity.applicationContext)
      onFinished()
    }
  }

  /**
   * Shows an interstitial ad with smart frequency capping to prevent user annoyance on exit.
   * Calls [onDismissed] immediately when the ad closes, fails, or is skipped due to cooldown.
   */
  fun showInterstitialOnExit(activity: Activity, onDismissed: () -> Unit) {
    videoExitsCounter++
    val now = SystemClock.elapsedRealtime()
    val timeSinceLast = now - lastInterstitialShowTime

    val isCooldownElapsed = timeSinceLast >= AdConfig.INTERSTITIAL_COOLDOWN_MS
    val isFrequencyMet = videoExitsCounter >= AdConfig.INTERSTITIAL_MIN_EXITS_BEFORE_SHOW && isCooldownElapsed

    val ad = interstitialAd
    if (ad != null && isFrequencyMet) {
      if (activity.isFinishing || activity.isDestroyed) {
        onDismissed()
        return
      }
      val showBlock = {
        if (!activity.isFinishing && !activity.isDestroyed) {
          try {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
              override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Interstitial Ad dismissed")
                interstitialAd = null
                lastInterstitialShowTime = SystemClock.elapsedRealtime()
                videoExitsCounter = 0
                loadInterstitialAd(activity.applicationContext)
                onDismissed()
              }

              override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.w(TAG, "Interstitial Ad failed to show: ${adError.message}")
                interstitialAd = null
                loadInterstitialAd(activity.applicationContext)
                onDismissed()
              }

              override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Interstitial Ad showing")
              }
            }

            ad.show(activity)
          } catch (e: Exception) {
            Log.w(TAG, "Interstitial Ad could not show: ${e.message}")
            interstitialAd = null
            loadInterstitialAd(activity.applicationContext)
            onDismissed()
          }
        } else {
          onDismissed()
        }
      }
      Handler(Looper.getMainLooper()).post {
        showBlock()
      }
    } else {
      // Ad wasn't ready or cooldown not elapsed: don't block user navigation!
      if (ad == null) {
        loadInterstitialAd(activity.applicationContext)
      }
      onDismissed()
    }
  }

  // ==========================================
  // Pause Banner Ad Management (Cached & Preloaded)
  // ==========================================

  /**
   * Returns a cached AdView for the pause screen, creating and preloading it if not already present.
   */
  fun getOrCreatePauseAdView(context: Context): AdView {
    val existing = cachedPauseAdView
    if (existing != null) {
      (existing.parent as? ViewGroup)?.removeView(existing)
      return existing
    }

    var currentUnit = AdConfig.bannerAdUnitId
    var hasFallenBack = false

    val adView = AdView(context.applicationContext).apply {
      this.adUnitId = currentUnit
      setAdSize(AdSize.BANNER)
      adListener = object : AdListener() {
        override fun onAdLoaded() {
          Log.d(TAG, "Pause Banner Ad loaded successfully ($currentUnit)")
          isPauseAdLoaded = true
          isPauseAdReadyState.value = true
          isPauseAdLoading = false
          lastPauseAdFailTime = 0
          lastPauseAdLoadTime = SystemClock.elapsedRealtime()
        }

        override fun onAdFailedToLoad(error: LoadAdError) {
          Log.w(TAG, "Pause Banner Ad failed to load ($currentUnit): ${error.message} (code ${error.code})")
          if (!hasFallenBack && AdConfig.autoFallbackToTestOnNoFill && currentUnit != AdConfig.TEST_BANNER_AD_ID) {
            hasFallenBack = true
            currentUnit = AdConfig.TEST_BANNER_AD_ID
            this@apply.adUnitId = currentUnit
            Log.i(TAG, "Cascading Pause Banner to Google Test Banner unit: $currentUnit")
            post {
              loadAd(AdRequest.Builder().build())
            }
          } else {
            isPauseAdLoaded = false
            isPauseAdReadyState.value = false
            isPauseAdLoading = false
            lastPauseAdFailTime = SystemClock.elapsedRealtime()
          }
        }
      }
    }
    cachedPauseAdView = adView
    preloadPauseAd(context.applicationContext)
    return adView
  }

  /**
   * Preloads the pause banner ad with strict frequency capping and backoff protection.
   */
  fun preloadPauseAd(context: Context) {
    val ad = cachedPauseAdView ?: run {
      getOrCreatePauseAdView(context)
      return
    }
    if (isPauseAdLoading) return

    val now = SystemClock.elapsedRealtime()
    // Don't reload if an ad is already loaded and fresh
    if (isPauseAdLoaded && (now - lastPauseAdLoadTime < PAUSE_AD_REFRESH_MIN_MS)) {
      return
    }
    // Respect retry backoff if recently failed
    if (lastPauseAdFailTime > 0 && (now - lastPauseAdFailTime < PAUSE_AD_RETRY_COOLDOWN_MS)) {
      return
    }

    isPauseAdLoading = true
    ad.loadAd(AdRequest.Builder().build())
  }

  fun isPauseAdReady(): Boolean = isPauseAdLoaded

  // ==========================================
  // Rewarded Ad Management
  // ==========================================

  fun loadRewardedAd(context: Context, adUnitId: String = AdConfig.rewardedAdUnitId) {
    if (isRewardedLoading || rewardedAd != null) return

    isRewardedLoading = true
    val request = AdRequest.Builder().build()
    Log.d(TAG, "Loading Rewarded Ad with unit: $adUnitId")
    RewardedAd.load(
      context,
      adUnitId,
      request,
      object : RewardedAdLoadCallback() {
        override fun onAdLoaded(ad: RewardedAd) {
          Log.d(TAG, "Rewarded Ad loaded successfully ($adUnitId)")
          rewardedAd = ad
          isRewardedLoading = false
          lastRewardedFailTime = 0
        }

        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
          Log.w(TAG, "Rewarded Ad failed to load ($adUnitId): ${loadAdError.message} (code ${loadAdError.code})")
          rewardedAd = null
          isRewardedLoading = false
          lastRewardedFailTime = SystemClock.elapsedRealtime()

          if (AdConfig.autoFallbackToTestOnNoFill && adUnitId != AdConfig.TEST_REWARDED_AD_ID) {
            Log.i(TAG, "Cascading to Google Test Rewarded ad unit (${AdConfig.TEST_REWARDED_AD_ID})")
            loadRewardedAd(context, AdConfig.TEST_REWARDED_AD_ID)
          }
        }
      },
    )
  }

  fun showRewardedAd(
    activity: Activity,
    onUserEarnedReward: () -> Unit = {},
    onDismissed: () -> Unit = {},
  ) {
    val ad = rewardedAd
    if (ad != null && !activity.isFinishing && !activity.isDestroyed) {
      ad.fullScreenContentCallback = object : FullScreenContentCallback() {
        override fun onAdDismissedFullScreenContent() {
          Log.d(TAG, "Rewarded Ad dismissed")
          rewardedAd = null
          loadRewardedAd(activity.applicationContext)
          onDismissed()
        }

        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
          Log.w(TAG, "Rewarded Ad failed to show: ${adError.message}")
          rewardedAd = null
          loadRewardedAd(activity.applicationContext)
          onDismissed()
        }

        override fun onAdShowedFullScreenContent() {
          Log.d(TAG, "Rewarded Ad showing")
        }
      }
      ad.show(activity) {
        Log.d(TAG, "User earned reward from Rewarded Ad")
        onUserEarnedReward()
      }
    } else {
      loadRewardedAd(activity.applicationContext)
      onDismissed()
    }
  }

  // ==========================================
  // Rewarded Interstitial Ad Management
  // ==========================================

  fun loadRewardedInterstitialAd(context: Context, adUnitId: String = AdConfig.rewardedInterstitialAdUnitId) {
    if (isRewardedInterstitialLoading || rewardedInterstitialAd != null) return

    isRewardedInterstitialLoading = true
    val request = AdRequest.Builder().build()
    Log.d(TAG, "Loading Rewarded Interstitial Ad with unit: $adUnitId")
    RewardedInterstitialAd.load(
      context,
      adUnitId,
      request,
      object : RewardedInterstitialAdLoadCallback() {
        override fun onAdLoaded(ad: RewardedInterstitialAd) {
          Log.d(TAG, "Rewarded Interstitial Ad loaded successfully ($adUnitId)")
          rewardedInterstitialAd = ad
          isRewardedInterstitialLoading = false
          lastRewardedInterstitialFailTime = 0
        }

        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
          Log.w(TAG, "Rewarded Interstitial Ad failed to load ($adUnitId): ${loadAdError.message} (code ${loadAdError.code})")
          rewardedInterstitialAd = null
          isRewardedInterstitialLoading = false
          lastRewardedInterstitialFailTime = SystemClock.elapsedRealtime()

          if (AdConfig.autoFallbackToTestOnNoFill && adUnitId != AdConfig.TEST_REWARDED_INTERSTITIAL_AD_ID) {
            Log.i(TAG, "Cascading to Google Test Rewarded Interstitial unit (${AdConfig.TEST_REWARDED_INTERSTITIAL_AD_ID})")
            loadRewardedInterstitialAd(context, AdConfig.TEST_REWARDED_INTERSTITIAL_AD_ID)
          }
        }
      },
    )
  }

  fun showRewardedInterstitialAd(
    activity: Activity,
    onUserEarnedReward: () -> Unit = {},
    onDismissed: () -> Unit = {},
  ) {
    val ad = rewardedInterstitialAd
    if (ad != null && !activity.isFinishing && !activity.isDestroyed) {
      ad.fullScreenContentCallback = object : FullScreenContentCallback() {
        override fun onAdDismissedFullScreenContent() {
          Log.d(TAG, "Rewarded Interstitial dismissed")
          rewardedInterstitialAd = null
          loadRewardedInterstitialAd(activity.applicationContext)
          onDismissed()
        }

        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
          Log.w(TAG, "Rewarded Interstitial failed to show: ${adError.message}")
          rewardedInterstitialAd = null
          loadRewardedInterstitialAd(activity.applicationContext)
          onDismissed()
        }

        override fun onAdShowedFullScreenContent() {
          Log.d(TAG, "Rewarded Interstitial showing")
        }
      }
      ad.show(activity) {
        Log.d(TAG, "User earned reward from Rewarded Interstitial")
        onUserEarnedReward()
      }
    } else {
      loadRewardedInterstitialAd(activity.applicationContext)
      onDismissed()
    }
  }

  // ==========================================
  // Native Ad Management
  // ==========================================

  fun loadNativeAd(
    context: Context,
    adUnitId: String = AdConfig.nativeAdUnitId,
    onLoaded: (NativeAd) -> Unit,
    onFailed: (LoadAdError) -> Unit = {},
  ) {
    var activeUnit = adUnitId
    Log.d(TAG, "Loading Native Ad with unit: $activeUnit")
    val builder = AdLoader.Builder(context, activeUnit)
      .forNativeAd { nativeAd ->
        Log.d(TAG, "Native Ad loaded successfully ($activeUnit)")
        onLoaded(nativeAd)
      }
      .withAdListener(object : AdListener() {
        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
          Log.w(TAG, "Native Ad failed to load ($activeUnit): ${loadAdError.message} (code ${loadAdError.code})")
          if (AdConfig.autoFallbackToTestOnNoFill && activeUnit != AdConfig.TEST_NATIVE_AD_ID) {
            Log.i(TAG, "Cascading to Google Test Native unit (${AdConfig.TEST_NATIVE_AD_ID})")
            loadNativeAd(context, AdConfig.TEST_NATIVE_AD_ID, onLoaded, onFailed)
          } else {
            onFailed(loadAdError)
          }
        }
      })
    builder.build().loadAd(AdRequest.Builder().build())
  }
}
