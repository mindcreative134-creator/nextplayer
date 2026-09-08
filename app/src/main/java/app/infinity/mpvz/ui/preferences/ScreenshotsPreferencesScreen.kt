/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package app.infinity.mpvz.ui.preferences

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.infinity.mpvz.R
import app.infinity.mpvz.preferences.PlayerPreferences
import app.infinity.mpvz.preferences.preference.collectAsState
import app.infinity.mpvz.presentation.Screen
import app.infinity.mpvz.ui.icons.Icon
import app.infinity.mpvz.ui.icons.Icons
import app.infinity.mpvz.ui.player.screenshot.ScreenshotFormat
import app.infinity.mpvz.ui.preferences.components.SwitchPreference
import app.infinity.mpvz.ui.utils.LocalBackStack
import app.infinity.mpvz.ui.utils.LocalShowSettingsBackArrow
import app.infinity.mpvz.ui.utils.popSafely
import kotlinx.serialization.Serializable
import me.zhanghai.compose.preference.ListPreference
import me.zhanghai.compose.preference.Preference
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import me.zhanghai.compose.preference.SliderPreference
import org.koin.compose.koinInject
import kotlin.math.roundToInt

@Serializable
object ScreenshotsPreferencesScreen : Screen {
  @OptIn(ExperimentalMaterial3Api::class)
  @Composable
  override fun Content() {
    val backstack = LocalBackStack.current
    val showBackArrow = LocalShowSettingsBackArrow.current
    val preferences = koinInject<PlayerPreferences>()

    var showTemplateDialog by remember { mutableStateOf(false) }
    var templateDraft by remember { mutableStateOf("") }

    Scaffold(
      topBar = {
        TopAppBar(
          title = {
            Text(
              text = stringResource(R.string.pref_screenshots_section),
              style = MaterialTheme.typography.headlineSmall,
              fontWeight = FontWeight.ExtraBold,
              color = MaterialTheme.colorScheme.primary,
            )
          },
          navigationIcon = {
            if (showBackArrow) {
              IconButton(onClick = { backstack.popSafely() }) {
                Icon(
                  Icons.RoundedFilled.ArrowBack,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.secondary,
                )
              }
            }
          },
        )
      },
    ) { padding ->
      ProvidePreferenceLocals {
        LazyColumn(
          modifier =
            Modifier
              .fillMaxSize()
              .padding(padding)
              .padding(horizontal = 16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          item {
            PreferenceCard {
              val screenshotFormat by preferences.screenshotFormat.collectAsState()
              ListPreference(
                value = screenshotFormat,
                onValueChange = preferences.screenshotFormat::set,
                values = ScreenshotFormat.entries,
                valueToText = { AnnotatedString(it.title) },
                title = { Text(stringResource(R.string.ui_image_format)) },
                summary = {
                  Text(
                    "${screenshotFormat.title} (.${screenshotFormat.extension})",
                    color = MaterialTheme.colorScheme.outline,
                  )
                },
              )

              PreferenceDivider()

              val pngCompression by preferences.screenshotPngCompression.collectAsState()
              SliderPreference(
                value = pngCompression.toFloat(),
                onValueChange = { preferences.screenshotPngCompression.set(it.roundToInt().coerceIn(0, 9)) },
                title = { Text(stringResource(R.string.ui_png_compression)) },
                valueRange = 0f..9f,
                summary = {
                  Text(
                    if (pngCompression == 0) {
                      "0 (None / Lossless uncompressed)"
                    } else if (pngCompression == 9) {
                      "9 (Maximum compression)"
                    } else {
                      "$pngCompression (Level $pngCompression/9)"
                    },
                    color = MaterialTheme.colorScheme.outline,
                  )
                },
                onSliderValueChange = { preferences.screenshotPngCompression.set(it.roundToInt().coerceIn(0, 9)) },
                sliderValue = pngCompression.toFloat(),
              )

              if (screenshotFormat == ScreenshotFormat.JPG ||
                screenshotFormat == ScreenshotFormat.JPEG ||
                screenshotFormat == ScreenshotFormat.WEBP
              ) {
                PreferenceDivider()

                val screenshotQuality by preferences.screenshotQuality.collectAsState()
                SliderPreference(
                  value = screenshotQuality.toFloat(),
                  onValueChange = { preferences.screenshotQuality.set(it.roundToInt().coerceIn(1, 100)) },
                  title = { Text(stringResource(R.string.ui_jpeg_webp_quality)) },
                  valueRange = 1f..100f,
                  summary = { Text("$screenshotQuality%", color = MaterialTheme.colorScheme.outline) },
                  onSliderValueChange = { preferences.screenshotQuality.set(it.roundToInt().coerceIn(1, 100)) },
                  sliderValue = screenshotQuality.toFloat(),
                )
              }

              if (screenshotFormat == ScreenshotFormat.WEBP) {
                PreferenceDivider()

                val webpLossless by preferences.screenshotWebpLossless.collectAsState()
                SwitchPreference(
                  value = webpLossless,
                  onValueChange = preferences.screenshotWebpLossless::set,
                  title = { Text(stringResource(R.string.ui_webp_lossless)) },
                  summary = {
                    Text(
                      stringResource(R.string.ui_uses_mpv_native_lossless_output_android_fallback_uses_lossless_o),
                      color = MaterialTheme.colorScheme.outline,
                    )
                  },
                )
              }

              PreferenceDivider()

              val includeSubtitles by preferences.includeSubtitlesInSnapshot.collectAsState()
              SwitchPreference(
                value = includeSubtitles,
                onValueChange = preferences.includeSubtitlesInSnapshot::set,
                title = { Text(stringResource(R.string.ui_include_subtitles_in_screenshots)) },
              )

              PreferenceDivider()

              val screenshotTemplate by preferences.screenshotTemplate.collectAsState()
              Preference(
                title = { Text(stringResource(R.string.ui_filename_template)) },
                summary = { Text(screenshotTemplate, color = MaterialTheme.colorScheme.outline) },
                onClick = {
                  templateDraft = screenshotTemplate
                  showTemplateDialog = true
                },
              )
            }
          }
        }
      }
    }

    if (showTemplateDialog) {
      AlertDialog(
        onDismissRequest = { showTemplateDialog = false },
        title = { Text(stringResource(R.string.ui_filename_template)) },
        text = {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
              value = templateDraft,
              onValueChange = { templateDraft = it },
              label = { Text(stringResource(R.string.ui_template)) },
              modifier = Modifier.fillMaxWidth(),
              singleLine = true,
            )
            Text(
              text =
                stringResource(R.string.ui_use_placeholders_to_customize_the_screenshot_filename_n) +
                  "• %f — Video title or filename\n" +
                  "• %p — Playback position (seconds)\n" +
                  "• %Y, %m, %d — Year, Month, Day\n" +
                  "• %H, %M, %S — Hour, Minute, Second\n" +
                  "• %wH, %wM, %wS, %wT — Wall-clock time (hour, min, sec, ms)",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        },
        confirmButton = {
          TextButton(
            onClick = {
              preferences.screenshotTemplate.set(templateDraft)
              showTemplateDialog = false
            },
          ) {
            Text(stringResource(R.string.ui_save))
          }
        },
        dismissButton = {
          TextButton(onClick = { showTemplateDialog = false }) {
            Text(stringResource(R.string.generic_cancel))
          }
        },
      )
    }
  }
}
