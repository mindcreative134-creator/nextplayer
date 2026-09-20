/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package app.infinity.mpvz.preferences

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import app.infinity.mpvz.preferences.preference.PreferenceStore
import app.infinity.mpvz.preferences.preference.getEnum
import app.infinity.mpvz.ui.theme.AppTheme
import app.infinity.mpvz.ui.theme.DarkMode
import app.infinity.mpvz.ui.theme.spacing
import kotlinx.collections.immutable.ImmutableList

class AppearancePreferences(
  preferenceStore: PreferenceStore,
) {
  val darkMode = preferenceStore.getEnum("dark_mode", DarkMode.System)
  val appTheme = preferenceStore.getEnum("app_theme", AppTheme.Dynamic)
  val customThemes = preferenceStore.getObject(
    key = "custom_themes",
    defaultValue = emptyList<CustomThemeData>(),
    serializer = CustomThemeCodec::encode,
    deserializer = CustomThemeCodec::decode,
  )
  val activeCustomThemeId = preferenceStore.getString("active_custom_theme_id", "")
  val amoledMode = preferenceStore.getBoolean("amoled_mode", false)
  val liquidGlassSurfaces = preferenceStore.getBoolean("liquid_glass_surfaces", false)
  val useSystemFont = preferenceStore.getBoolean("use_system_font", false)
  val unlimitedNameLines = preferenceStore.getBoolean("unlimited_name_lines", false)
  val hidePlayerButtonsBackground = preferenceStore.getBoolean("hide_player_buttons_background", false)
  val forceDarkPlayerButtonsBackground = preferenceStore.getBoolean("force_dark_player_buttons_background", false)
  val showUnplayedOldVideoLabel = preferenceStore.getBoolean("show_unplayed_old_video_label", true)
  val unplayedOldVideoDays = preferenceStore.getInt("unplayed_old_video_days", 7)
  val showNetworkThumbnails = preferenceStore.getBoolean("show_network_thumbnails", false)
  val seekbarStyle = preferenceStore.getEnum("seekbar_style", SeekbarStyle.Thick)
  val playerControlsTheme = preferenceStore.getEnum("player_controls_theme", PlayerControlsTheme.Classic)
  val showSeekbarOuterContainer = preferenceStore.getBoolean("show_seekbar_outer_container", false)
  val portraitPlaybackControlsPosition =
    preferenceStore.getEnum("portrait_playback_controls_position", PortraitPlaybackControlsPosition.Center)
  val showHomeTab = preferenceStore.getBoolean("show_home_tab", true)
  val showMusicTab = preferenceStore.getBoolean("show_music_tab", true)
  val showRecentsTab = preferenceStore.getBoolean("show_recents_tab", true)
  val showPlaylistsTab = preferenceStore.getBoolean("show_playlists_tab", true)
  val showNetworkTab = preferenceStore.getBoolean("show_network_tab", false)
  val showJellyfinTab = preferenceStore.getBoolean("show_jellyfin_tab", false)
  val showQuickPlayFab = preferenceStore.getBoolean("show_quick_play_fab", true)
  val quickPlayFabDirect = preferenceStore.getBoolean("quick_play_fab_direct", false)

  val topLeftControls =
    preferenceStore.getString(
      "top_left_controls",
      "BACK_ARROW,VIDEO_TITLE",
    )

  val topRightControls =
    preferenceStore.getString(
      "top_right_controls",
      "SUBTITLES,PLAYBACK_SPEED,DECODER,MORE_OPTIONS",
    )

  val bottomRightControls =
    preferenceStore.getString(
      "bottom_right_controls",
      "PICTURE_IN_PICTURE,ASPECT_RATIO,VIDEO_ZOOM",
    )

  val bottomLeftControls =
    preferenceStore.getString(
      "bottom_left_controls",
      "LOCK_CONTROLS,SCREEN_ROTATION,BACKGROUND_PLAYBACK",
    )

  val portraitBottomControls =
    preferenceStore.getString(
      "portrait_bottom_controls",
      "LOCK_CONTROLS,SCREEN_ROTATION,ASPECT_RATIO,AUDIO_TRACK,SUBTITLES,PLAYBACK_SPEED,MORE_OPTIONS",
    )

  private val simplifiedControlsMigrationComplete =
    preferenceStore.getBoolean("simplified_controls_v3_migration_complete", false)

  init {
    if (!simplifiedControlsMigrationComplete.get()) {
      topRightControls.set("SUBTITLES,PLAYBACK_SPEED,DECODER,MORE_OPTIONS")
      bottomLeftControls.set("LOCK_CONTROLS,SCREEN_ROTATION,BACKGROUND_PLAYBACK")
      bottomRightControls.set("PICTURE_IN_PICTURE,ASPECT_RATIO,VIDEO_ZOOM")
      portraitBottomControls.set("LOCK_CONTROLS,SCREEN_ROTATION,ASPECT_RATIO,AUDIO_TRACK,SUBTITLES,PLAYBACK_SPEED,MORE_OPTIONS")
      simplifiedControlsMigrationComplete.set(true)
    }
  }

  fun parseButtons(
    csv: String,
    usedButtons: MutableSet<PlayerButton>,
  ): List<PlayerButton> =
    csv
      .splitToSequence(',')
      .map { it.trim().uppercase() }
      .mapNotNull { name ->
        try {
          PlayerButton.valueOf(name)
        } catch (_: IllegalArgumentException) {
          null
        }
      }.filter { it != PlayerButton.NONE }
      .filter { usedButtons.add(it) }
      .toList()
}

enum class PortraitPlaybackControlsPosition(
  val displayName: String,
) {
  Center("Center of screen"),
  BelowSeekbar("Between seekbar and controls"),
}

@Composable
fun MultiChoiceSegmentedButton(
  choices: ImmutableList<String>,
  selectedIndices: ImmutableList<Int>,
  onClick: (Int, Offset) -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .padding(MaterialTheme.spacing.medium),
    horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
  ) {
    choices.forEachIndexed { index, choice ->
      var buttonCenter by remember(choice) { mutableStateOf(Offset.Zero) }
      ToggleButton(
        checked = selectedIndices.contains(index),
        onCheckedChange = { onClick(index, buttonCenter) },
        modifier =
          Modifier
            .weight(1f)
            .defaultMinSize(minHeight = MaterialTheme.spacing.extraLarge)
            .onGloballyPositioned { buttonCenter = it.boundsInWindow().center }
            .semantics { role = Role.RadioButton },
        colors =
          ToggleButtonDefaults.colors(
            checkedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            checkedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
            contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
          ),
        shapes =
          when (index) {
            0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
            choices.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
            else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
          },
      ) {
        Text(text = choice)
      }
    }
  }
}


enum class PlayerControlsTheme(val displayName: String) {
  Classic("Classic"),
  Glass("Glass"),
  Glossy("Glossy"),
}
