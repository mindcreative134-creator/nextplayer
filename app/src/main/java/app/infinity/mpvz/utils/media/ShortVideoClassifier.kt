/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package app.infinity.mpvz.utils.media

import app.infinity.mpvz.domain.media.model.Video

/**
 * Single source of truth for Shorts (vertical/short-form) video classification.
 * Follows exact deterministic rules:
 *
 * 1. File Type: Only actual video files (!video.isAudio). Never audio, images, etc.
 * 2. User priority 1: Explicitly excluded by user -> NOT SHORT.
 * 3. User priority 2: Explicitly included by user -> SHORT.
 * 4. Missing / corrupt metadata fail-safe:
 *    If width <= 0, height <= 0, or duration <= 0 -> NOT SHORT.
 * 5. Duration Rule:
 *    Must be between 1 second (1,000 ms) and 180 seconds (180,000 ms / 3 mins).
 *    Duration < 1s or > 180s -> NOT SHORT.
 * 6. Orientation Rule:
 *    Must be portrait: effectiveHeight > effectiveWidth.
 *    Landscape (aspectRatio >= 1.0) and Square (aspectRatio == 1.0) -> NOT SHORT.
 * 7. Aspect Ratio Calculation:
 *    aspectRatio = effectiveWidth / effectiveHeight
 * 8. Confidence Levels:
 *    - Definite Short: aspectRatio <= 0.80 and duration <= 180s (e.g. 9:16, 3:4, 4:5) -> SHORT.
 *    - Possible Short: 0.80 < aspectRatio < 1.0 and duration <= 60s -> SHORT.
 *    - Everything else -> NOT SHORT.
 */
object ShortVideoClassifier {

  fun isShort(
    video: Video,
    manuallyIncluded: Set<String> = emptySet(),
    manuallyExcluded: Set<String> = emptySet(),
  ): Boolean {
    // 1. File Type: Must be video, never audio
    if (video.isAudio) return false

    // 2. User Priority 1: Manually excluded
    if (video.path in manuallyExcluded) return false

    // 3. User Priority 2: Manually included
    if (video.path in manuallyIncluded) return true

    // 4. Missing metadata / failsafe check
    if (video.width <= 0 || video.height <= 0 || video.duration <= 0) {
      return false
    }

    val durationMs = video.duration
    // Rule: Must be at least 1 second
    if (durationMs < 1_000L) return false

    // Rule: Maximum duration 180 seconds (3 minutes)
    if (durationMs > 180_000L) return false

    // Effective display dimensions
    val width = video.width
    val height = video.height

    // Rule: Must be portrait (height > width)
    if (height <= width) return false

    val aspectRatio = width.toFloat() / height.toFloat()

    // Definite Short: aspectRatio <= 0.80f and duration <= 180s
    if (aspectRatio <= 0.80f) {
      return true
    }

    // Possible Short: slightly less vertical (0.80 < aspectRatio < 1.0) and duration <= 60s
    if (aspectRatio < 1.0f && durationMs <= 60_000L) {
      return true
    }

    return false
  }
}
