/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package app.infinity.mpvz.ads

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.ViewGroup
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

private const val TAG = "BannerAdView"

/**
 * Robust Google AdMob Banner Ad Composable.
 * Features:
 * - Direct AndroidView instantiation (no leaked/destroyed references).
 * - Automatic Live-to-Test cascading fallback if live ad returns NO_FILL or error.
 * - Proper Android lifecycle awareness (pause/resume/destroy).
 * - Clean UI presentation.
 */
@Composable
fun RealAdmobBanner(
  modifier: Modifier = Modifier,
  adUnitId: String = AdConfig.bannerAdUnitId,
  onAdLoadedStateChanged: ((Boolean) -> Unit)? = null,
) {
  val lifecycleOwner = LocalLifecycleOwner.current
  var isAdLoaded by remember { mutableStateOf(false) }
  var adViewInstance by remember { mutableStateOf<AdView?>(null) }

  DisposableEffect(lifecycleOwner, adViewInstance) {
    val adView = adViewInstance
    if (adView == null) return@DisposableEffect onDispose {}

    val observer = LifecycleEventObserver { _, event ->
      when (event) {
        Lifecycle.Event.ON_RESUME -> adView.resume()
        Lifecycle.Event.ON_PAUSE -> adView.pause()
        Lifecycle.Event.ON_DESTROY -> adView.destroy()
        else -> Unit
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose {
      lifecycleOwner.lifecycle.removeObserver(observer)
      adView.destroy()
    }
  }

  Box(
    modifier = if (isAdLoaded) {
      modifier
        .fillMaxWidth()
        .wrapContentHeight()
        .padding(vertical = 4.dp, horizontal = 8.dp)
    } else {
      Modifier
        .fillMaxWidth()
        .height(0.dp)
    },
    contentAlignment = Alignment.Center,
  ) {
    AnimatedVisibility(
      visible = isAdLoaded,
      enter = fadeIn(),
      exit = fadeOut(),
    ) {
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .wrapContentHeight()
          .clip(RoundedCornerShape(8.dp)),
        color = Color(0x0DFFFFFF),
        tonalElevation = 1.dp,
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
          contentAlignment = Alignment.Center,
        ) {
          AndroidView(
            modifier = Modifier.wrapContentSize(),
            factory = { context ->
              createBannerAdView(
                context = context,
                initialAdUnitId = adUnitId,
                onLoaded = {
                  isAdLoaded = true
                  onAdLoadedStateChanged?.invoke(true)
                },
                onFailed = {
                  isAdLoaded = false
                  onAdLoadedStateChanged?.invoke(false)
                },
                onAdViewCreated = { adView ->
                  adViewInstance = adView
                },
              )
            },
            onRelease = { adView ->
              adView.destroy()
            },
          )
        }
      }
    }
  }
}

private fun createBannerAdView(
  context: Context,
  initialAdUnitId: String,
  onLoaded: () -> Unit,
  onFailed: () -> Unit,
  onAdViewCreated: (AdView) -> Unit,
): AdView {
  val mainHandler = Handler(Looper.getMainLooper())
  var activeAdUnit = initialAdUnitId
  var hasFallenBackToTest = false
  var retryCount = 0

  return AdView(context).apply {
    layoutParams = ViewGroup.LayoutParams(
      ViewGroup.LayoutParams.WRAP_CONTENT,
      ViewGroup.LayoutParams.WRAP_CONTENT,
    )
    setAdSize(AdSize.BANNER)
    this.adUnitId = initialAdUnitId
    onAdViewCreated(this)

    fun loadAdForUnit(unitId: String) {
      activeAdUnit = unitId
      this.adUnitId = unitId
      Log.d(TAG, "Requesting banner ad for unit: $unitId")
      val request = AdRequest.Builder().build()
      this.loadAd(request)
    }

    this.adListener = object : AdListener() {
      override fun onAdLoaded() {
        Log.d(TAG, "Banner ad loaded successfully with unit: $activeAdUnit")
        retryCount = 0
        onLoaded()
      }

      override fun onAdFailedToLoad(error: LoadAdError) {
        Log.w(
          TAG,
          "Banner ad failed to load ($activeAdUnit): ${error.message} (code ${error.code})",
        )
        // Fallback to official Google Test ad unit if live unit has no fill or error
        if (
          !hasFallenBackToTest &&
          AdConfig.autoFallbackToTestOnNoFill &&
          activeAdUnit != AdConfig.TEST_BANNER_AD_ID
        ) {
          hasFallenBackToTest = true
          Log.i(
            TAG,
            "Cascading to Google Test Banner unit: ${AdConfig.TEST_BANNER_AD_ID}",
          )
          mainHandler.post {
            loadAdForUnit(AdConfig.TEST_BANNER_AD_ID)
          }
        } else {
          onFailed()
          retryCount++
          val delayMs = (15_000L * retryCount).coerceAtMost(120_000L)
          mainHandler.postDelayed({
            loadAdForUnit(activeAdUnit)
          }, delayMs)
        }
      }
    }

    loadAdForUnit(initialAdUnitId)
  }
}

@Composable
fun BannerAdView(
  modifier: Modifier = Modifier,
  adUnitId: String = AdConfig.bannerAdUnitId,
  onAdLoadedStateChanged: ((Boolean) -> Unit)? = null,
) {
  RealAdmobBanner(
    modifier = modifier,
    adUnitId = adUnitId,
    onAdLoadedStateChanged = onAdLoadedStateChanged,
  )
}

@Composable
fun HomeBannerAdCard(
  modifier: Modifier = Modifier,
) {
  RealAdmobBanner(
    modifier = modifier,
    adUnitId = AdConfig.bannerAdUnitId,
  )
}
