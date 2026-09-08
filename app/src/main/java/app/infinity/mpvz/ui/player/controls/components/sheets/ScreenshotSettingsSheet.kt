/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package app.infinity.mpvz.ui.player.controls.components.sheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.infinity.mpvz.R
import app.infinity.mpvz.preferences.PlayerPreferences
import app.infinity.mpvz.preferences.preference.collectAsState
import app.infinity.mpvz.presentation.components.PlayerSheet
import app.infinity.mpvz.presentation.components.SliderItem
import app.infinity.mpvz.ui.components.IconSwitch
import app.infinity.mpvz.ui.icons.Icon
import app.infinity.mpvz.ui.icons.Icons
import app.infinity.mpvz.ui.player.screenshot.ScreenshotFormat
import app.infinity.mpvz.ui.player.screenshot.ScreenshotSaver
import app.infinity.mpvz.ui.player.screenshot.ScreenshotSettings
import app.infinity.mpvz.ui.theme.spacing
import org.koin.compose.koinInject

@Composable
fun ScreenshotSettingsSheet(
  onDismissRequest: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val playerPreferences = koinInject<PlayerPreferences>()
  val screenshotFormat by playerPreferences.screenshotFormat.collectAsState()
  val pngCompression by playerPreferences.screenshotPngCompression.collectAsState()
  val screenshotQuality by playerPreferences.screenshotQuality.collectAsState()
  val webpLossless by playerPreferences.screenshotWebpLossless.collectAsState()
  val includeSubtitles by playerPreferences.includeSubtitlesInSnapshot.collectAsState()

  PlayerSheet(onDismissRequest = onDismissRequest, modifier = modifier) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())
        .padding(bottom = MaterialTheme.spacing.medium),
    ) {
      // Header
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(
            horizontal = MaterialTheme.spacing.medium,
            vertical = MaterialTheme.spacing.smaller,
          ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
      ) {
        Icon(
          imageVector = Icons.RoundedFilled.Aperture,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(24.dp),
        )
        Text(
          text = stringResource(R.string.pref_screenshots_section),
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface,
        )
      }

      HorizontalDivider(modifier = Modifier.padding(vertical = MaterialTheme.spacing.extraSmall))

      // Format Selection
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(
            horizontal = MaterialTheme.spacing.medium,
            vertical = MaterialTheme.spacing.extraSmall,
          ),
      ) {
        Text(
          text = stringResource(R.string.ui_image_format),
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          listOf(ScreenshotFormat.PNG, ScreenshotFormat.JPG, ScreenshotFormat.WEBP).forEach { format ->
            val isSelected = screenshotFormat == format ||
              (format == ScreenshotFormat.JPG && screenshotFormat == ScreenshotFormat.JPEG)
            FilterChip(
              selected = isSelected,
              onClick = {
                playerPreferences.screenshotFormat.set(format)
                ScreenshotSaver.applyMpvScreenshotOptions(
                  ScreenshotSettings.fromPreferences(playerPreferences).copy(format = format),
                )
              },
              label = {
                Text(
                  text = when (format) {
                    ScreenshotFormat.PNG -> "PNG"
                    ScreenshotFormat.JPG, ScreenshotFormat.JPEG -> "JPG"
                    ScreenshotFormat.WEBP -> "WebP"
                  },
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                )
              },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
              ),
            )
          }
        }
      }

      // PNG Compression Controls
      if (screenshotFormat == ScreenshotFormat.PNG) {
        Spacer(modifier = Modifier.height(4.dp))
        SliderItem(
          label = stringResource(R.string.ui_png_compression),
          value = pngCompression,
          valueText = when (pngCompression) {
            0 -> "0 (Lossless uncompressed)"
            9 -> "9 (Maximum)"
            else -> "$pngCompression/9"
          },
          onChange = { level ->
            val clamped = level.coerceIn(0, 9)
            playerPreferences.screenshotPngCompression.set(clamped)
            ScreenshotSaver.applyMpvScreenshotOptions(
              ScreenshotSettings.fromPreferences(playerPreferences).copy(pngCompression = clamped),
            )
          },
          min = 0,
          max = 9,
        )

        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.spacing.medium, vertical = 2.dp),
          shape = RoundedCornerShape(8.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ) {
          Text(
            text = if (pngCompression == 0) {
              "✓ Lossless uncompressed: Preserves full original video frame quality with zero compression artifacts."
            } else {
              "PNG compression level $pngCompression/9 (0 is uncompressed lossless)."
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
          )
        }
      }

      // Quality Controls for JPG and WEBP
      if (screenshotFormat == ScreenshotFormat.JPG ||
        screenshotFormat == ScreenshotFormat.JPEG ||
        screenshotFormat == ScreenshotFormat.WEBP
      ) {
        Spacer(modifier = Modifier.height(4.dp))
        SliderItem(
          label = stringResource(R.string.ui_jpeg_webp_quality),
          value = screenshotQuality,
          valueText = "$screenshotQuality%",
          onChange = { quality ->
            val clamped = quality.coerceIn(1, 100)
            playerPreferences.screenshotQuality.set(clamped)
            ScreenshotSaver.applyMpvScreenshotOptions(
              ScreenshotSettings.fromPreferences(playerPreferences).copy(quality = clamped),
            )
          },
          min = 1,
          max = 100,
        )
      }

      // WebP Lossless Toggle
      if (screenshotFormat == ScreenshotFormat.WEBP) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(
              horizontal = MaterialTheme.spacing.medium,
              vertical = MaterialTheme.spacing.extraSmall,
            ),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = stringResource(R.string.ui_webp_lossless),
              style = MaterialTheme.typography.bodyMedium,
            )
          }
          IconSwitch(
            checked = webpLossless,
            onCheckedChange = { checked ->
              playerPreferences.screenshotWebpLossless.set(checked)
              ScreenshotSaver.applyMpvScreenshotOptions(
                ScreenshotSettings.fromPreferences(playerPreferences).copy(webpLossless = checked),
              )
            },
            modifier = Modifier.scale(0.8f),
          )
        }
      }

      HorizontalDivider(modifier = Modifier.padding(vertical = MaterialTheme.spacing.smaller))

      // Include Subtitles Toggle
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(
            horizontal = MaterialTheme.spacing.medium,
            vertical = MaterialTheme.spacing.extraSmall,
          ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Text(
          text = stringResource(R.string.ui_include_subtitles_in_screenshots),
          style = MaterialTheme.typography.bodyMedium,
        )
        IconSwitch(
          checked = includeSubtitles,
          onCheckedChange = playerPreferences.includeSubtitlesInSnapshot::set,
          modifier = Modifier.scale(0.8f),
        )
      }
    }
  }
}
