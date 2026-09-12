/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package app.infinity.mpvz.ads

import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView

/**
 * Renders an AdMob Native Advanced Ad.
 * Automatically loads the ad (with Live-to-Test cascading fallback)
 * and displays it cleanly inside a Compose card.
 */
@Composable
fun NativeAdCard(
  modifier: Modifier = Modifier,
  adUnitId: String = AdConfig.nativeAdUnitId,
) {
  val context = LocalContext.current
  var nativeAdInstance by remember { mutableStateOf<NativeAd?>(null) }

  LaunchedEffect(adUnitId) {
    AdmobManager.loadNativeAd(
      context = context,
      adUnitId = adUnitId,
      onLoaded = { ad ->
        nativeAdInstance?.destroy()
        nativeAdInstance = ad
      },
    )
  }

  DisposableEffect(Unit) {
    onDispose {
      nativeAdInstance?.destroy()
      nativeAdInstance = null
    }
  }

  val ad = nativeAdInstance
  Box(
    modifier = if (ad != null) {
      modifier
        .fillMaxWidth()
        .wrapContentHeight()
        .padding(horizontal = 12.dp, vertical = 6.dp)
    } else {
      Modifier
        .fillMaxWidth()
        .height(0.dp)
    },
  ) {
    AnimatedVisibility(
      visible = ad != null,
      enter = fadeIn(),
      exit = fadeOut(),
    ) {
      if (ad != null) {
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clip(RoundedCornerShape(12.dp)),
          color = Color(0x14FFFFFF),
          tonalElevation = 2.dp,
        ) {
          AndroidView(
            modifier = Modifier
              .fillMaxWidth()
              .wrapContentHeight()
              .padding(10.dp),
            factory = { ctx ->
              val nativeAdView = NativeAdView(ctx)
              populateNativeAdView(ad, nativeAdView)
              nativeAdView
            },
            update = { view ->
              populateNativeAdView(ad, view)
            },
          )
        }
      }
    }
  }
}

private fun populateNativeAdView(nativeAd: NativeAd, adView: NativeAdView) {
  adView.removeAllViews()

  val context = adView.context
  val rootLayout = android.widget.LinearLayout(context).apply {
    orientation = android.widget.LinearLayout.VERTICAL
    layoutParams = android.view.ViewGroup.LayoutParams(
      android.view.ViewGroup.LayoutParams.MATCH_PARENT,
      android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
    )
  }

  // Header row: "Ad" badge + Headline + Advertiser
  val topRow = android.widget.LinearLayout(context).apply {
    orientation = android.widget.LinearLayout.HORIZONTAL
    gravity = android.view.Gravity.CENTER_VERTICAL
    layoutParams = android.widget.LinearLayout.LayoutParams(
      android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
      android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
    )
  }

  // App / Ad Icon
  val iconView = ImageView(context).apply {
    layoutParams = android.widget.LinearLayout.LayoutParams(40.dpToPx(context), 40.dpToPx(context)).apply {
      marginEnd = 10.dpToPx(context)
    }
    scaleType = ImageView.ScaleType.FIT_CENTER
  }
  adView.iconView = iconView

  nativeAd.icon?.drawable?.let {
    iconView.setImageDrawable(it)
    topRow.addView(iconView)
  }

  val textColumn = android.widget.LinearLayout(context).apply {
    orientation = android.widget.LinearLayout.VERTICAL
    layoutParams = android.widget.LinearLayout.LayoutParams(
      0,
      android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
      1f,
    )
  }

  val badgeAndHeadRow = android.widget.LinearLayout(context).apply {
    orientation = android.widget.LinearLayout.HORIZONTAL
    gravity = android.view.Gravity.CENTER_VERTICAL
  }

  val badge = TextView(context).apply {
    text = "Ad"
    textSize = 10f
    setTextColor(android.graphics.Color.BLACK)
    setBackgroundColor(android.graphics.Color.parseColor("#FBC02D"))
    setPadding(6, 2, 6, 2)
  }
  badgeAndHeadRow.addView(badge)

  val headlineView = TextView(context).apply {
    text = nativeAd.headline ?: ""
    textSize = 14f
    setTextColor(android.graphics.Color.WHITE)
    typeface = android.graphics.Typeface.DEFAULT_BOLD
    maxLines = 1
    ellipsize = android.text.TextUtils.TruncateAt.END
    setPadding(8, 0, 0, 0)
  }
  adView.headlineView = headlineView
  badgeAndHeadRow.addView(headlineView)
  textColumn.addView(badgeAndHeadRow)

  nativeAd.advertiser?.let { adv ->
    val advertiserView = TextView(context).apply {
      text = adv
      textSize = 11f
      setTextColor(android.graphics.Color.LTGRAY)
      maxLines = 1
    }
    adView.advertiserView = advertiserView
    textColumn.addView(advertiserView)
  }

  topRow.addView(textColumn)

  // Call to action button
  val ctaText = nativeAd.callToAction
  if (!ctaText.isNullOrBlank()) {
    val callToActionView = Button(context).apply {
      text = ctaText
      textSize = 12f
      setTextColor(android.graphics.Color.WHITE)
      setBackgroundColor(android.graphics.Color.parseColor("#2979FF"))
      layoutParams = android.widget.LinearLayout.LayoutParams(
        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
        36.dpToPx(context),
      ).apply {
        marginStart = 8.dpToPx(context)
      }
    }
    adView.callToActionView = callToActionView
    topRow.addView(callToActionView)
  }

  rootLayout.addView(topRow)

  // Body text if available
  nativeAd.body?.let { body ->
    val bodyView = TextView(context).apply {
      text = body
      textSize = 12f
      setTextColor(android.graphics.Color.argb(200, 255, 255, 255))
      maxLines = 2
      ellipsize = android.text.TextUtils.TruncateAt.END
      setPadding(0, 6, 0, 0)
    }
    adView.bodyView = bodyView
    rootLayout.addView(bodyView)
  }

  adView.addView(rootLayout)
  adView.setNativeAd(nativeAd)
}

private fun Int.dpToPx(context: android.content.Context): Int {
  return (this * context.resources.displayMetrics.density).toInt()
}
