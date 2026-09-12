/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package app.infinity.mpvz.ads

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

/**
 * Real Google AdMob Banner Ad Composable.
 * Renders an active AdView for adUnitId "ca-app-pub-8615789090438802/1492093645".
 * Reloads the banner after a failure with exponential backoff so ads show reliably.
 */
@Composable
fun RealAdmobBanner(
  modifier: Modifier = Modifier,
  adUnitId: String = AdConfig.bannerAdUnitId,
  onAdLoadedStateChanged: ((Boolean) -> Unit)? = null,
) {
  val context = LocalContext.current
  val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
  var isAdReady by remember { mutableStateOf(false) }

  val adView = rememberAdView(context, adUnitId, onAdLoadedStateChanged, onAdReady = { isAdReady = it })

  // Pause/resume the AdView with the host lifecycle to avoid loading ads while
  // the screen is not visible and to avoid leaking the ad request.
  DisposableEffect(lifecycleOwner, adView) {
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

  if (!isAdReady) return

  Box(
    modifier = modifier
      .fillMaxWidth()
      .wrapContentHeight()
      .padding(vertical = 4.dp),
    contentAlignment = Alignment.Center,
  ) {
    AndroidView(
      modifier = Modifier
        .fillMaxWidth()
        .height(50.dp),
      factory = {
        (adView.parent as? ViewGroup)?.removeView(adView)
        adView
      },
    )
  }
}

private val bannerReloadHandler = Handler(Looper.getMainLooper())

@Composable
private fun rememberAdView(
  context: android.content.Context,
  adUnitId: String,
  onAdLoadedStateChanged: ((Boolean) -> Unit)?,
  onAdReady: (Boolean) -> Unit,
): AdView {
  return remember(adUnitId) {
    var retryAttempt = 0
    AdView(context).apply {
      this.adUnitId = adUnitId
      setAdSize(AdSize.BANNER)
      layoutParams = ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT,
      )
      adListener = object : AdListener() {
        override fun onAdLoaded() {
          Log.d("BannerAdView", "Banner ad loaded successfully ($adUnitId)")
          retryAttempt = 0
          onAdReady(true)
          onAdLoadedStateChanged?.invoke(true)
        }

        override fun onAdFailedToLoad(error: LoadAdError) {
          Log.w("BannerAdView", "Banner ad failed to load ($adUnitId): ${error.message} (code ${error.code})")
          onAdReady(false)
          onAdLoadedStateChanged?.invoke(false)
          // Retry with exponential backoff: 20s, 40s, 80s...
          retryAttempt++
          val delayMs = (20_000L * retryAttempt).coerceAtMost(300_000L)
          bannerReloadHandler.postDelayed({
            if (Looper.myLooper() == Looper.getMainLooper()) {
              this@apply.loadAd(AdRequest.Builder().build())
            }
          }, delayMs)
        }
      }
      loadAd(AdRequest.Builder().build())
    }
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
