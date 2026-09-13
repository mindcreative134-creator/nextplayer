/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package app.infinity.mpvz.ads

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
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
 * - FrameLayout container hosting AdView instances.
 * - Ad unit ID is set strictly ONCE per AdView (preventing IllegalStateException).
 * - Automatic Live-to-Test cascading fallback with fresh AdView instantiation.
 * - Proper Android lifecycle awareness (pause/resume/destroy).
 * - Zero height when ad is not loaded, smooth expand on load.
 */
@Composable
fun RealAdmobBanner(
  modifier: Modifier = Modifier,
  adUnitId: String = AdConfig.bannerAdUnitId,
  onAdLoadedStateChanged: ((Boolean) -> Unit)? = null,
) {
  val lifecycleOwner = LocalLifecycleOwner.current
  var isAdLoaded by remember { mutableStateOf(false) }
  var activeAdView by remember { mutableStateOf<AdView?>(null) }

  DisposableEffect(lifecycleOwner, activeAdView) {
    val adView = activeAdView
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
        AndroidView<FrameLayout>(
          modifier = Modifier.wrapContentSize(),
          factory = { context ->
            createBannerContainer(
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
              onActiveAdViewChanged = { adView ->
                activeAdView = adView
              },
            )
          },
          onRelease = { container: FrameLayout ->
            for (i in 0 until container.childCount) {
              (container.getChildAt(i) as? AdView)?.destroy()
            }
            container.removeAllViews()
            activeAdView = null
          },
        )
      }
    }
  }
}

private fun createBannerContainer(
  context: Context,
  initialAdUnitId: String,
  onLoaded: () -> Unit,
  onFailed: () -> Unit,
  onActiveAdViewChanged: (AdView) -> Unit,
): FrameLayout {
  val container = FrameLayout(context).apply {
    layoutParams = ViewGroup.LayoutParams(
      ViewGroup.LayoutParams.WRAP_CONTENT,
      ViewGroup.LayoutParams.WRAP_CONTENT,
    )
  }

  val mainHandler = Handler(Looper.getMainLooper())
  var hasFallenBackToTest = false
  var retryCount = 0

  fun loadBanner(unitId: String, isFallback: Boolean) {
    // 1. Destroy and remove existing AdView from container
    for (i in 0 until container.childCount) {
      (container.getChildAt(i) as? AdView)?.destroy()
    }
    container.removeAllViews()

    // 2. Create fresh AdView instance (Ad unit ID can only be set ONCE per AdView)
    val adView = AdView(context).apply {
      layoutParams = FrameLayout.LayoutParams(
        FrameLayout.LayoutParams.WRAP_CONTENT,
        FrameLayout.LayoutParams.WRAP_CONTENT,
        Gravity.CENTER,
      )
      setAdSize(AdSize.BANNER)
      this.adUnitId = unitId // Set strictly once on this fresh AdView instance!
    }

    onActiveAdViewChanged(adView)
    container.addView(adView)

    adView.adListener = object : AdListener() {
      override fun onAdLoaded() {
        Log.d(TAG, "Banner ad loaded successfully with unit: $unitId")
        retryCount = 0
        onLoaded()
      }

      override fun onAdFailedToLoad(error: LoadAdError) {
        Log.w(
          TAG,
          "Banner ad failed to load ($unitId): ${error.message} (code ${error.code})",
        )
        if (
          !hasFallenBackToTest &&
          !isFallback &&
          AdConfig.autoFallbackToTestOnNoFill &&
          unitId != AdConfig.TEST_BANNER_AD_ID
        ) {
          hasFallenBackToTest = true
          Log.i(
            TAG,
            "Cascading to Google Test Banner unit: ${AdConfig.TEST_BANNER_AD_ID}",
          )
          mainHandler.post {
            loadBanner(AdConfig.TEST_BANNER_AD_ID, isFallback = true)
          }
        } else {
          onFailed()
          retryCount++
          val delayMs = (15_000L * retryCount).coerceAtMost(120_000L)
          mainHandler.postDelayed({
            loadBanner(unitId, isFallback)
          }, delayMs)
        }
      }
    }

    Log.d(TAG, "Requesting banner ad for unit: $unitId")
    adView.loadAd(AdRequest.Builder().build())
  }

  loadBanner(initialAdUnitId, isFallback = false)
  return container
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
