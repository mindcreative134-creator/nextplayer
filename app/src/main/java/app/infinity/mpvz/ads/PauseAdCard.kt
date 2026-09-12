/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package app.infinity.mpvz.ads

import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import app.infinity.mpvz.ui.icons.Icon
import app.infinity.mpvz.ui.icons.Icons
import kotlinx.coroutines.delay

@Composable
fun PauseAdCard(
  isPaused: Boolean,
  modifier: Modifier = Modifier,
  onDismiss: () -> Unit = {},
) {
  var isDismissedByUser by remember { mutableStateOf(false) }
  var isGracePeriodElapsed by remember { mutableStateOf(false) }
  val context = LocalContext.current

  // Preload pause ad when player is active
  LaunchedEffect(Unit) {
    AdmobManager.preloadPauseAd(context)
  }

  // Wait for a steady pause (1.2s) before showing an ad to avoid spamming on quick seeks/taps
  LaunchedEffect(isPaused) {
    if (isPaused) {
      delay(AdConfig.PAUSE_AD_GRACE_PERIOD_MS)
      isGracePeriodElapsed = true
      AdmobManager.preloadPauseAd(context)
    } else {
      isGracePeriodElapsed = false
    }
  }

  // If user unpauses, manually closed this session, or grace period hasn't elapsed
  if (!isPaused || isDismissedByUser || !isGracePeriodElapsed) {
    return
  }

  val isAdReady = AdmobManager.isPauseAdReadyState.value || AdmobManager.isPauseAdReady()
  if (!isAdReady) {
    return
  }

  val adView = remember(context) {
    AdmobManager.getOrCreatePauseAdView(context)
  }

  DisposableEffect(adView) {
    onDispose {
      // Detach from parent view so it can be re-attached without re-loading
      (adView.parent as? ViewGroup)?.removeView(adView)
    }
  }

  Surface(
    modifier = modifier
      .wrapContentSize()
      .clip(RoundedCornerShape(12.dp)),
    color = Color(0xCC1A1A1A),
    tonalElevation = 6.dp,
    shadowElevation = 8.dp,
  ) {
    Column(
      modifier = Modifier.padding(6.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Row(
        modifier = Modifier
          .width(320.dp)
          .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Box(
          modifier = Modifier
            .background(Color(0xFFFBC02D), RoundedCornerShape(4.dp))
            .padding(horizontal = 4.dp, vertical = 1.dp),
        ) {
          Text(
            text = "Ad",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
          )
        }

        Spacer(modifier = Modifier.weight(1f))

        Icon(
          imageVector = Icons.RoundedFilled.Close,
          contentDescription = "Close Ad",
          tint = Color.White.copy(alpha = 0.7f),
          modifier = Modifier
            .size(18.dp)
            .clickable {
              isDismissedByUser = true
              onDismiss()
            },
        )
      }

      AndroidView(
        modifier = Modifier.wrapContentSize(),
        factory = {
          (adView.parent as? ViewGroup)?.removeView(adView)
          adView
        },
      )
    }
  }
}
