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
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.infinity.mpvz.domain.media.model.Video
import app.infinity.mpvz.domain.thumbnail.ThumbnailRepository
import app.infinity.mpvz.presentation.components.pullrefresh.PullRefreshBox
import app.infinity.mpvz.ui.browser.LocalNavigationBarHeight
import app.infinity.mpvz.ui.browser.components.BrowserTopBar
import app.infinity.mpvz.ui.icons.Icon
import app.infinity.mpvz.ui.icons.Icons
import app.infinity.mpvz.ui.preferences.PreferencesScreen
import app.infinity.mpvz.ui.utils.LocalBackStack
import app.infinity.mpvz.utils.media.MediaUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject

@Composable
fun ShortsContent(
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val backstack = LocalBackStack.current
  val navigationBarHeight = LocalNavigationBarHeight.current

  val viewModel: ShortsViewModel =
    viewModel(
      factory = ShortsViewModel.factory(context.applicationContext as Application),
    )

  val shortsVideos by viewModel.shortsVideos.collectAsState()
  val isLoading by viewModel.isLoading.collectAsState()
  val gridState = rememberSaveable(saver = LazyGridState.Saver) { LazyGridState() }

  val isRefreshing = remember { mutableStateOf(false) }
  LaunchedEffect(isLoading) {
    isRefreshing.value = isLoading
  }

  Scaffold(
    topBar = {
      BrowserTopBar(
        title = "Shorts",
        isInSelectionMode = false,
        selectedCount = 0,
        totalCount = shortsVideos.size,
        onCancelSelection = {},
        onSettingsClick = { backstack.add(PreferencesScreen) },
      )
    },
    modifier = modifier.fillMaxSize(),
  ) { paddingValues ->
    PullRefreshBox(
      isRefreshing = isRefreshing,
      onRefresh = { viewModel.refresh() },
      modifier = Modifier.fillMaxSize().padding(top = paddingValues.calculateTopPadding()),
    ) {
      Column(modifier = Modifier.fillMaxSize()) {
        app.infinity.mpvz.ads.HomeBannerAdCard()

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
          if (isLoading && shortsVideos.isEmpty()) {
            Box(
              modifier = Modifier.fillMaxSize(),
              contentAlignment = Alignment.Center,
            ) {
              CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
              )
            }
          } else if (shortsVideos.isEmpty()) {
            ShortsEmptyState(
              onRefresh = { viewModel.refresh() },
              modifier = Modifier.fillMaxSize(),
            )
          } else {
            LazyVerticalGrid(
              columns = GridCells.Fixed(2),
              state = gridState,
              contentPadding =
                PaddingValues(
                  start = 12.dp,
                  end = 12.dp,
                  top = 8.dp,
                  bottom = navigationBarHeight + 24.dp,
                ),
              horizontalArrangement = Arrangement.spacedBy(10.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp),
              modifier = Modifier.fillMaxSize(),
            ) {
              items(shortsVideos, key = { it.id }) { video ->
                ShortsCard(
                  video = video,
                  onPlayClick = {
                    MediaUtils.playFile(video, context, "shorts")
                  },
                  onRemoveFromShorts = {
                    viewModel.removeShort(video)
                  },
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun ShortsCard(
  video: Video,
  onPlayClick: () -> Unit,
  onRemoveFromShorts: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val thumbnailRepository = koinInject<ThumbnailRepository>()
  var thumbnailBitmap by remember(video.id) { mutableStateOf<Bitmap?>(null) }
  var menuExpanded by remember { mutableStateOf(false) }

  LaunchedEffect(video.id, video.dateModified) {
    withContext(Dispatchers.IO) {
      thumbnailRepository.getThumbnail(video, 405, 720)?.let {
        thumbnailBitmap = it
      }
    }
  }

  val cardShape = RoundedCornerShape(18.dp)

  Card(
    modifier =
      modifier
        .fillMaxWidth()
        .aspectRatio(9f / 16f)
        .clip(cardShape)
        .clickable(onClick = onPlayClick),
    shape = cardShape,
    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
    colors =
      CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
      ),
  ) {
    Box(modifier = Modifier.fillMaxSize()) {
      // Thumbnail
      if (thumbnailBitmap != null) {
        Image(
          bitmap = thumbnailBitmap!!.asImageBitmap(),
          contentDescription = video.displayName,
          modifier = Modifier.fillMaxSize(),
          contentScale = ContentScale.Crop,
        )
      } else {
        Box(
          modifier =
            Modifier
              .fillMaxSize()
              .background(MaterialTheme.colorScheme.surfaceContainerHighest),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            Icons.RoundedFilled.PlayCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.size(48.dp),
          )
        }
      }

      // Dark gradient overlay on bottom for readability
      Box(
        modifier =
          Modifier
            .fillMaxWidth()
            .height(110.dp)
            .align(Alignment.BottomCenter)
            .background(
              Brush.verticalGradient(
                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f)),
              ),
            ),
      )

      // Top-right 3-dot overflow menu
      Box(modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)) {
        Surface(
          shape = CircleShape,
          color = Color.Black.copy(alpha = 0.45f),
          modifier = Modifier.size(32.dp),
        ) {
          IconButton(
            onClick = { menuExpanded = true },
            modifier = Modifier.size(32.dp),
          ) {
            Icon(
              Icons.RoundedFilled.MoreVert,
              contentDescription = "Options",
              tint = Color.White,
              modifier = Modifier.size(18.dp),
            )
          }
        }

        DropdownMenu(
          expanded = menuExpanded,
          onDismissRequest = { menuExpanded = false },
        ) {
          DropdownMenuItem(
            text = { Text("Play") },
            onClick = {
              menuExpanded = false
              onPlayClick()
            },
            leadingIcon = {
              Icon(Icons.RoundedFilled.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
            },
          )
          DropdownMenuItem(
            text = { Text("Share") },
            onClick = {
              menuExpanded = false
              MediaUtils.shareVideos(context, listOf(video))
            },
            leadingIcon = {
              Icon(Icons.RoundedFilled.Share, contentDescription = null, modifier = Modifier.size(20.dp))
            },
          )
          DropdownMenuItem(
            text = { Text("Video Details") },
            onClick = {
              menuExpanded = false
              val intent = Intent(context, app.infinity.mpvz.ui.mediainfo.MediaInfoActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                data = video.uri
              }
              context.startActivity(intent)
            },
            leadingIcon = {
              Icon(Icons.RoundedFilled.Info, contentDescription = null, modifier = Modifier.size(20.dp))
            },
          )
          DropdownMenuItem(
            text = { Text("Remove from Shorts") },
            onClick = {
              menuExpanded = false
              onRemoveFromShorts()
            },
            leadingIcon = {
              Icon(Icons.RoundedFilled.Close, contentDescription = null, modifier = Modifier.size(20.dp))
            },
          )
        }
      }

      // Center Frosted Play Pill
      Surface(
        modifier = Modifier.align(Alignment.Center).size(46.dp),
        shape = CircleShape,
        color = Color.Black.copy(alpha = 0.42f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
        shadowElevation = 6.dp,
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            Icons.RoundedFilled.PlayArrow,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(26.dp),
          )
        }
      }

      // Bottom info (Title, Duration badge)
      Column(
        modifier =
          Modifier
            .align(Alignment.BottomStart)
            .fillMaxWidth()
            .padding(10.dp),
      ) {
        Text(
          text = video.displayName,
          style = MaterialTheme.typography.bodySmall,
          fontWeight = FontWeight.Bold,
          color = Color.White,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          if (video.durationFormatted.isNotBlank()) {
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = Color.Black.copy(alpha = 0.65f),
              border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.White.copy(alpha = 0.2f)),
              contentColor = Color.White,
            ) {
              Text(
                text = video.durationFormatted,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
              )
            }
          }

          if (video.resolution.isNotBlank()) {
            Text(
              text = video.resolution,
              style = MaterialTheme.typography.labelSmall,
              color = Color.White.copy(alpha = 0.75f),
            )
          }
        }
      }
    }
  }
}

@Composable
private fun ShortsEmptyState(
  onRefresh: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier =
      modifier
        .fillMaxWidth()
        .padding(horizontal = 32.dp, vertical = 64.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Surface(
      shape = CircleShape,
      color = MaterialTheme.colorScheme.surfaceContainerHighest,
      modifier = Modifier.size(80.dp),
    ) {
      Box(contentAlignment = Alignment.Center) {
        Icon(
          Icons.RoundedFilled.PlayCircle,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(42.dp),
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(
      text = "No Shorts Yet",
      style = MaterialTheme.typography.titleLarge,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onBackground,
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
      text = "Portrait and vertical videos (under 3 minutes) from your device will automatically appear here.",
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      textAlign = TextAlign.Center,
    )

    Spacer(modifier = Modifier.height(24.dp))

    Surface(
      shape = CircleShape,
      color = MaterialTheme.colorScheme.primary,
      contentColor = MaterialTheme.colorScheme.onPrimary,
      modifier = Modifier.clip(CircleShape).clickable(onClick = onRefresh),
    ) {
      Text(
        text = "Scan Media",
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
      )
    }
  }
}
