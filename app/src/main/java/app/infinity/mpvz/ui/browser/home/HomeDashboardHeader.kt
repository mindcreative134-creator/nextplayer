/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package app.infinity.mpvz.ui.browser.home

import android.graphics.Bitmap
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.net.Uri
import app.infinity.mpvz.database.entities.RecentlyPlayedEntity
import app.infinity.mpvz.domain.media.model.Video
import app.infinity.mpvz.domain.media.model.VideoFolder
import app.infinity.mpvz.domain.playbackstate.repository.PlaybackStateRepository
import app.infinity.mpvz.domain.recentlyplayed.repository.RecentlyPlayedRepository
import app.infinity.mpvz.domain.thumbnail.ThumbnailRepository
import app.infinity.mpvz.preferences.FolderSortType
import app.infinity.mpvz.preferences.MediaLayoutMode
import app.infinity.mpvz.preferences.SortOrder
import app.infinity.mpvz.ui.icons.Icon
import app.infinity.mpvz.ui.icons.Icons
import app.infinity.mpvz.ui.player.PlaybackIdentity
import app.infinity.mpvz.utils.media.MediaUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject

@Composable
fun HomeDashboardHeader(
  folders: List<VideoFolder>,
  pinnedCount: Int,
  mediaLayoutMode: MediaLayoutMode,
  sortType: FolderSortType,
  sortOrder: SortOrder,
  onSortClick: () -> Unit,
  onSearchClick: () -> Unit,
  onRefreshClick: () -> Unit,
  onToggleLayoutMode: () -> Unit,
  onNavigateToFavorites: () -> Unit,
  onNavigateToPlaylists: () -> Unit,
  onStreamClick: () -> Unit = {},
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val recentlyPlayedRepo = koinInject<RecentlyPlayedRepository>()
  val playbackStateRepo = koinInject<PlaybackStateRepository>()
  val thumbnailRepo = koinInject<ThumbnailRepository>()

  val recentVideos by recentlyPlayedRepo
    .observeRecentlyPlayed(limit = 10)
    .collectAsState(initial = emptyList())

  val heroVideo = recentVideos.firstOrNull()

  // Calculate aggregate metrics
  val totalVideosCount = remember(folders) {
    folders.sumOf { it.videoCount }
  }
  val totalFoldersCount = folders.size

  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 12.dp, vertical = 6.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    // 1. Top Decoder & Utility Status Bar
    HomeDecoderStatusBar(
      onSearchClick = onSearchClick,
      onRefreshClick = onRefreshClick,
      onSortClick = onSortClick,
    )

    // 2. Continue Watching Hero Banner (if recent video available)
    if (heroVideo != null) {
      ContinueWatchingHeroCard(
        video = heroVideo,
        playbackStateRepo = playbackStateRepo,
        thumbnailRepo = thumbnailRepo,
        onPlay = { MediaUtils.playFile(heroVideo.filePath, context, "home_continue_watching") },
      )
    }

    // 3. Quick Access 2x2 Pills
    QuickAccessGrid(
      totalVideosCount = totalVideosCount,
      totalFoldersCount = totalFoldersCount,
      pinnedCount = pinnedCount,
      onFavoritesClick = onNavigateToFavorites,
      onPlaylistsClick = onNavigateToPlaylists,
      onStreamClick = onStreamClick,
    )

    // 4. Recently Played Carousel (if more than 1 recent item)
    if (recentVideos.size > 1) {
      RecentlyPlayedCarousel(
        videos = recentVideos.drop(1),
        thumbnailRepo = thumbnailRepo,
        playbackStateRepo = playbackStateRepo,
        onVideoClick = { entity ->
          MediaUtils.playFile(entity.filePath, context, "home_recent_carousel")
        },
      )
    }

    // 5. Storage Hierarchy & Folders Section Header with Sort Ribbon
    StorageSectionRibbon(
      folderCount = totalFoldersCount,
      sortType = sortType,
      sortOrder = sortOrder,
      mediaLayoutMode = mediaLayoutMode,
      onSortClick = onSortClick,
      onToggleLayout = onToggleLayoutMode,
    )
  }
}

@Composable
private fun HomeDecoderStatusBar(
  onSearchClick: () -> Unit,
  onRefreshClick: () -> Unit,
  onSortClick: () -> Unit,
) {
  val infiniteTransition = rememberInfiniteTransition(label = "decoder_pulse")
  val pulseAlpha by infiniteTransition.animateFloat(
    initialValue = 0.4f,
    targetValue = 1.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(1200, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse,
    ),
    label = "pulseAlpha",
  )

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(top = 2.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    // Status Indicator
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp),
      modifier = Modifier
        .clip(RoundedCornerShape(8.dp))
        .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f))
        .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
      Box(
        modifier = Modifier
          .size(7.dp)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.secondary.copy(alpha = pulseAlpha)),
      )
      Text(
        text = "DECODER: HW+",
        style = MaterialTheme.typography.labelSmall.copy(
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.SemiBold,
          letterSpacing = 0.5.sp,
          fontSize = 11.sp,
        ),
        color = MaterialTheme.colorScheme.secondary,
      )
    }

    // Utility Actions
    Row(
      horizontalArrangement = Arrangement.spacedBy(4.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      IconButton(
        onClick = onSearchClick,
        modifier = Modifier
          .size(34.dp)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.surfaceContainerHigh),
      ) {
        Icon(
          Icons.RoundedFilled.Search,
          contentDescription = "Search",
          tint = MaterialTheme.colorScheme.onSurface,
          modifier = Modifier.size(18.dp),
        )
      }
      IconButton(
        onClick = onRefreshClick,
        modifier = Modifier
          .size(34.dp)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.surfaceContainerHigh),
      ) {
        Icon(
          Icons.RoundedFilled.Refresh,
          contentDescription = "Sync",
          tint = MaterialTheme.colorScheme.secondary,
          modifier = Modifier.size(18.dp),
        )
      }
      IconButton(
        onClick = onSortClick,
        modifier = Modifier
          .size(34.dp)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.surfaceContainerHigh),
      ) {
        Icon(
          Icons.RoundedFilled.SortByAlpha,
          contentDescription = "Sort & Filter",
          tint = MaterialTheme.colorScheme.tertiary,
          modifier = Modifier.size(18.dp),
        )
      }
    }
  }
}

@Composable
private fun ContinueWatchingHeroCard(
  video: RecentlyPlayedEntity,
  playbackStateRepo: PlaybackStateRepository,
  thumbnailRepo: ThumbnailRepository,
  onPlay: () -> Unit,
) {
  var thumbnailBitmap by remember(video.filePath) { mutableStateOf<Bitmap?>(null) }
  var progressFraction by remember(video.filePath) { mutableStateOf(0f) }

  LaunchedEffect(video.filePath, video.duration) {
    withContext(Dispatchers.IO) {
      val state = playbackStateRepo.getVideoDataByTitle(PlaybackIdentity.forLocalPath(video.filePath))
        ?: playbackStateRepo.getVideoDataByTitle(PlaybackIdentity.forUri(video.filePath))
      val durSec = video.duration / 1000L
      if (state != null && durSec > 0L) {
        val remaining = state.timeRemaining.toLong()
        val watched = durSec - remaining
        progressFraction = (watched.toFloat() / durSec.toFloat()).coerceIn(0.01f, 0.99f)
      }
      val bmp = thumbnailRepo.getThumbnail(video.toVideo(), widthPx = 360, heightPx = 200)
      withContext(Dispatchers.Main) {
        thumbnailBitmap = bmp
      }
    }
  }

  Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(6.dp),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 2.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        Icon(
          Icons.RoundedFilled.PlayCircle,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(16.dp),
        )
        Text(
          text = "CONTINUE WATCHING",
          style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp,
          ),
          color = MaterialTheme.colorScheme.onSurface,
        )
      }
      if (progressFraction > 0f) {
        Text(
          text = "${(progressFraction * 100).toInt()}% ELAPSED",
          style = MaterialTheme.typography.labelSmall.copy(
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold,
          ),
          color = MaterialTheme.colorScheme.tertiary,
        )
      }
    }

    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
      ),
      modifier = Modifier
        .fillMaxWidth()
        .clickable(onClick = onPlay),
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          // 16:9 Thumbnail Box
          Box(
            modifier = Modifier
              .width(112.dp)
              .aspectRatio(16f / 10f)
              .clip(RoundedCornerShape(10.dp))
              .background(MaterialTheme.colorScheme.surfaceContainerHighest),
            contentAlignment = Alignment.Center,
          ) {
            if (thumbnailBitmap != null) {
              Image(
                bitmap = thumbnailBitmap!!.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
              )
            } else {
              Icon(
                Icons.RoundedFilled.Movie,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                modifier = Modifier.size(28.dp),
              )
            }
            if (video.duration > 0) {
              Surface(
                shape = RoundedCornerShape(4.dp),
                color = Color.Black.copy(alpha = 0.75f),
                modifier = Modifier
                  .align(Alignment.BottomEnd)
                  .padding(4.dp),
              ) {
                Text(
                  text = formatDuration(video.duration),
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                  ),
                  color = Color.White,
                  modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                )
              }
            }
          }

          // Details
          Column(
            modifier = Modifier
              .weight(1f)
              .padding(vertical = 2.dp),
            verticalArrangement = Arrangement.SpaceBetween,
          ) {
            Row(
              horizontalArrangement = Arrangement.spacedBy(4.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Surface(
                shape = RoundedCornerShape(4.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f),
              ) {
                Text(
                  text = "HW+",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                  ),
                  color = MaterialTheme.colorScheme.secondary,
                  modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                )
              }
              val ext = video.fileName.substringAfterLast('.', "").uppercase()
              if (ext.isNotBlank()) {
                Surface(
                  shape = RoundedCornerShape(4.dp),
                  color = MaterialTheme.colorScheme.surfaceContainerHigh,
                ) {
                  Text(
                    text = ext,
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontFamily = FontFamily.Monospace,
                      fontSize = 9.sp,
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                  )
                }
              }
            }

            Text(
              text = video.videoTitle ?: video.fileName,
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
              color = MaterialTheme.colorScheme.onSurface,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.padding(top = 4.dp),
            )

            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              val sizeStr = if (video.fileSize > 0) formatFileSize(video.fileSize) else ""
              Text(
                text = sizeStr,
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )

              Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable(onClick = onPlay),
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                  Icon(
                    Icons.RoundedFilled.PlayArrow,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(14.dp),
                  )
                  Text(
                    text = "Resume",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimary,
                  )
                }
              }
            }
          }
        }

        // Progress Bar
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(5.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
        ) {
          if (progressFraction > 0f) {
            Box(
              modifier = Modifier
                .fillMaxWidth(progressFraction)
                .height(5.dp)
                .clip(CircleShape)
                .background(
                  Brush.horizontalGradient(
                    listOf(
                      MaterialTheme.colorScheme.primary,
                      MaterialTheme.colorScheme.secondary,
                    ),
                  ),
                ),
            )
          }
        }
      }
    }
  }
}

@Composable
private fun QuickAccessGrid(
  totalVideosCount: Int,
  totalFoldersCount: Int,
  pinnedCount: Int,
  onFavoritesClick: () -> Unit,
  onPlaylistsClick: () -> Unit,
  onStreamClick: () -> Unit = {},
) {
  Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(6.dp),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      QuickPillCard(
        title = "All Videos",
        subtitle = "$totalVideosCount files",
        icon = Icons.RoundedFilled.VideoLibrary,
        iconTint = MaterialTheme.colorScheme.primary,
        containerTint = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
        onClick = {},
        modifier = Modifier.weight(1f),
      )
      QuickPillCard(
        title = "Folders",
        subtitle = "$totalFoldersCount dirs",
        icon = Icons.RoundedFilled.Folder,
        iconTint = MaterialTheme.colorScheme.secondary,
        containerTint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
        onClick = {},
        modifier = Modifier.weight(1f),
      )
    }
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      QuickPillCard(
        title = "Favorites",
        subtitle = "$pinnedCount pinned",
        icon = Icons.RoundedFilled.Star,
        iconTint = MaterialTheme.colorScheme.tertiary,
        containerTint = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f),
        onClick = onFavoritesClick,
        modifier = Modifier.weight(1f),
      )
      QuickPillCard(
        title = "Playlists",
        subtitle = "Custom queues",
        icon = Icons.RoundedFilled.QueueMusic,
        iconTint = MaterialTheme.colorScheme.primary,
        containerTint = MaterialTheme.colorScheme.surfaceContainerHigh,
        onClick = onPlaylistsClick,
        modifier = Modifier.weight(1f),
      )
    }
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      QuickPillCard(
        title = "Online Stream",
        subtitle = "Movies, Series & Resolver",
        icon = Icons.RoundedFilled.PlayCircle,
        iconTint = MaterialTheme.colorScheme.secondary,
        containerTint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.14f),
        onClick = onStreamClick,
        modifier = Modifier.fillMaxWidth(),
      )
    }
  }
}

@Composable
private fun QuickPillCard(
  title: String,
  subtitle: String,
  icon: app.infinity.mpvz.ui.icons.AppIcon,
  iconTint: Color,
  containerTint: Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ),
    modifier = modifier
      .clip(RoundedCornerShape(14.dp))
      .clickable(onClick = onClick),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 10.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.weight(1f),
      ) {
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(containerTint),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(20.dp),
          )
        }
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          Text(
            text = subtitle,
            style = MaterialTheme.typography.labelSmall.copy(
              fontFamily = FontFamily.Monospace,
              fontSize = 10.sp,
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
          )
        }
      }
      Icon(
        Icons.RoundedFilled.ChevronRight,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.outlineVariant,
        modifier = Modifier.size(16.dp),
      )
    }
  }
}

@Composable
private fun RecentlyPlayedCarousel(
  videos: List<RecentlyPlayedEntity>,
  thumbnailRepo: ThumbnailRepository,
  playbackStateRepo: PlaybackStateRepository,
  onVideoClick: (RecentlyPlayedEntity) -> Unit,
) {
  Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 2.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        Icon(
          Icons.RoundedFilled.History,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.secondary,
          modifier = Modifier.size(16.dp),
        )
        Text(
          text = "RECENTLY PLAYED",
          style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp,
          ),
          color = MaterialTheme.colorScheme.onSurface,
        )
      }
      Text(
        text = "${videos.size} ITEMS",
        style = MaterialTheme.typography.labelSmall.copy(
          fontFamily = FontFamily.Monospace,
          fontSize = 10.sp,
        ),
        color = MaterialTheme.colorScheme.outline,
      )
    }

    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      contentPadding = PaddingValues(horizontal = 2.dp),
      modifier = Modifier.fillMaxWidth(),
    ) {
      items(videos, key = { it.filePath }) { video ->
        RecentVideoCarouselCard(
          video = video,
          thumbnailRepo = thumbnailRepo,
          playbackStateRepo = playbackStateRepo,
          onClick = { onVideoClick(video) },
        )
      }
    }
  }
}

@Composable
private fun RecentVideoCarouselCard(
  video: RecentlyPlayedEntity,
  thumbnailRepo: ThumbnailRepository,
  playbackStateRepo: PlaybackStateRepository,
  onClick: () -> Unit,
) {
  var thumbnailBitmap by remember(video.filePath) { mutableStateOf<Bitmap?>(null) }
  var progressFraction by remember(video.filePath) { mutableStateOf(0f) }

  LaunchedEffect(video.filePath, video.duration) {
    withContext(Dispatchers.IO) {
      val state = playbackStateRepo.getVideoDataByTitle(PlaybackIdentity.forLocalPath(video.filePath))
        ?: playbackStateRepo.getVideoDataByTitle(PlaybackIdentity.forUri(video.filePath))
      val durSec = video.duration / 1000L
      if (state != null && durSec > 0L) {
        val remaining = state.timeRemaining.toLong()
        val watched = durSec - remaining
        progressFraction = (watched.toFloat() / durSec.toFloat()).coerceIn(0.01f, 0.99f)
      }
      val bmp = thumbnailRepo.getThumbnail(video.toVideo(), widthPx = 300, heightPx = 170)
      withContext(Dispatchers.Main) {
        thumbnailBitmap = bmp
      }
    }
  }

  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ),
    modifier = Modifier
      .width(170.dp)
      .clip(RoundedCornerShape(12.dp))
      .clickable(onClick = onClick),
  ) {
    Column(modifier = Modifier.padding(8.dp)) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .aspectRatio(16f / 9.5f)
          .clip(RoundedCornerShape(8.dp))
          .background(MaterialTheme.colorScheme.surfaceContainerHighest),
        contentAlignment = Alignment.Center,
      ) {
        if (thumbnailBitmap != null) {
          Image(
            bitmap = thumbnailBitmap!!.asImageBitmap(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
          )
        } else {
          Icon(
            Icons.RoundedFilled.Movie,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            modifier = Modifier.size(24.dp),
          )
        }

        // 10-BIT / 4K / HW+ chip at top-left
        Surface(
          shape = RoundedCornerShape(3.dp),
          color = Color.Black.copy(alpha = 0.75f),
          modifier = Modifier
            .align(Alignment.TopStart)
            .padding(4.dp),
        ) {
          Text(
            text = "HW+",
            style = MaterialTheme.typography.labelSmall.copy(
              fontFamily = FontFamily.Monospace,
              fontSize = 8.sp,
              fontWeight = FontWeight.Bold,
            ),
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
          )
        }

        // Timecode badge
        if (video.duration > 0) {
          Surface(
            shape = RoundedCornerShape(3.dp),
            color = Color.Black.copy(alpha = 0.75f),
            modifier = Modifier
              .align(Alignment.BottomEnd)
              .padding(4.dp),
          ) {
            Text(
              text = formatDuration(video.duration),
              style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 8.sp,
                fontWeight = FontWeight.SemiBold,
              ),
              color = Color.White,
              modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
            )
          }
        }

        // Bottom progress bar
        if (progressFraction > 0f) {
          Box(
            modifier = Modifier
              .align(Alignment.BottomStart)
              .fillMaxWidth(progressFraction)
              .height(3.dp)
              .background(MaterialTheme.colorScheme.primary),
          )
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = video.videoTitle ?: video.fileName,
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )

      val ext = video.fileName.substringAfterLast('.', "").uppercase()
      Text(
        text = ext.ifBlank { "VIDEO" },
        style = MaterialTheme.typography.labelSmall.copy(
          fontFamily = FontFamily.Monospace,
          fontSize = 9.sp,
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
      )
    }
  }
}

@Composable
private fun StorageSectionRibbon(
  folderCount: Int,
  sortType: FolderSortType,
  sortOrder: SortOrder,
  mediaLayoutMode: MediaLayoutMode,
  onSortClick: () -> Unit,
  onToggleLayout: () -> Unit,
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(top = 4.dp, start = 2.dp, end = 2.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Icon(
        Icons.RoundedFilled.Folder,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.secondary,
        modifier = Modifier.size(16.dp),
      )
      Text(
        text = "STORAGE DIRECTORIES",
        style = MaterialTheme.typography.labelSmall.copy(
          fontWeight = FontWeight.Bold,
          letterSpacing = 0.8.sp,
        ),
        color = MaterialTheme.colorScheme.onSurface,
      )
      Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
      ) {
        Text(
          text = "$folderCount",
          style = MaterialTheme.typography.labelSmall.copy(
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
          ),
          color = MaterialTheme.colorScheme.primary,
          modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
        )
      }
    }

    // Active Sort & View Mode Quick Toggle
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      // Sort Chip
      Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.clickable(onClick = onSortClick),
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          Icon(
            if (sortOrder.isAscending) Icons.RoundedFilled.KeyboardArrowUp else Icons.RoundedFilled.KeyboardArrowDown,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.size(12.dp),
          )
          Text(
            text = sortType.displayName.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
              fontFamily = FontFamily.Monospace,
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold,
            ),
            color = MaterialTheme.colorScheme.onSurface,
          )
        }
      }

      // View Mode Toggle Button (List vs Grid)
      Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.clickable(onClick = onToggleLayout),
      ) {
        Box(
          modifier = Modifier.padding(6.dp),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            if (mediaLayoutMode == MediaLayoutMode.GRID) Icons.RoundedFilled.GridView else Icons.RoundedFilled.ViewList,
            contentDescription = "Toggle View",
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(14.dp),
          )
        }
      }
    }
  }
}

private fun RecentlyPlayedEntity.toVideo(): Video {
  val uri = Uri.parse(filePath)
  return Video(
    id = id.toLong(),
    title = videoTitle ?: fileName,
    displayName = fileName,
    path = filePath,
    uri = uri,
    duration = duration,
    durationFormatted = formatDuration(duration),
    size = fileSize,
    sizeFormatted = formatFileSize(fileSize),
    dateModified = timestamp,
    dateAdded = timestamp,
    mimeType = "video/*",
    bucketId = (uri.host ?: "local").hashCode().toString(),
    bucketDisplayName = "",
    width = width,
    height = height,
    fps = 0f,
    resolution = if (width > 0 && height > 0) "${width}x${height}" else "",
  )
}

private fun formatDuration(durationMs: Long): String {
  val seconds = durationMs / 1000
  val hours = seconds / 3600
  val minutes = (seconds % 3600) / 60
  val secs = seconds % 60

  return when {
    hours > 0 -> "${hours}h ${minutes}m"
    minutes > 0 -> "${minutes}m"
    else -> "${secs}s"
  }
}

private fun formatFileSize(bytes: Long): String {
  if (bytes <= 0) return "0 B"
  val units = arrayOf("B", "KB", "MB", "GB", "TB")
  val digitGroups = (kotlin.math.log10(bytes.toDouble()) / kotlin.math.log10(1024.0)).toInt().coerceIn(0, units.size - 1)
  val value = bytes / Math.pow(1024.0, digitGroups.toDouble())
  return String.format(java.util.Locale.US, "%.1f %s", value, units[digitGroups])
}
