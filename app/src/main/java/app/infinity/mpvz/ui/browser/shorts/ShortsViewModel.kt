/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package app.infinity.mpvz.ui.browser.shorts

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.infinity.mpvz.domain.media.model.Video
import app.infinity.mpvz.preferences.FoldersPreferences
import app.infinity.mpvz.repository.MediaFileRepository
import app.infinity.mpvz.utils.media.ShortVideoClassifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ShortsViewModel(
  application: Application,
) : AndroidViewModel(application), KoinComponent {

  private val foldersPreferences: FoldersPreferences by inject()

  private val _shortsVideos = MutableStateFlow<List<Video>>(emptyList())
  val shortsVideos: StateFlow<List<Video>> = _shortsVideos.asStateFlow()

  private val _isLoading = MutableStateFlow(true)
  val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

  init {
    loadShorts()
  }

  fun refresh() {
    loadShorts()
  }

  private fun loadShorts() {
    viewModelScope.launch(Dispatchers.IO) {
      _isLoading.value = true
      try {
        val allVideos = MediaFileRepository.getAllVideos(
          context = getApplication(),
          includeAudioOverride = false,
        )
        val included = foldersPreferences.manuallyIncludedShorts.get()
        val excluded = foldersPreferences.manuallyExcludedShorts.get()

        val classified = allVideos.filter { video ->
          ShortVideoClassifier.isShort(
            video = video,
            manuallyIncluded = included,
            manuallyExcluded = excluded,
          )
        }
        _shortsVideos.value = classified
      } catch (_: Exception) {
      } finally {
        _isLoading.value = false
      }
    }
  }

  fun removeShort(video: Video) {
    foldersPreferences.removeManualShort(video.path)
    refresh()
  }

  fun addShort(video: Video) {
    foldersPreferences.addManualShort(video.path)
    refresh()
  }

  companion object {
    fun factory(application: Application): ViewModelProvider.Factory =
      viewModelFactory {
        initializer {
          ShortsViewModel(application)
        }
      }
  }
}
