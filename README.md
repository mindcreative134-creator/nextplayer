<div align="center">

  <img src="PlayStore_App_Icon_512x512.png" width="160" height="160" alt="Next Player Logo" style="border-radius: 32px; box-shadow: 0 12px 36px rgba(0, 0, 0, 0.45); margin-bottom: 16px;" />

  # ⚡ Next Player

  ### 🎬 The Definitive High-Performance Media Suite for Android
  **Dual Playback Engines (MPV + Media3) • Material 3 Expressive • Studio HDR & Audio • Shorts Feed • Cloud Streaming**

  <br>

  [![Latest Release](https://img.shields.io/github/v/release/mindcreative134-creator/nextplayer?style=for-the-badge&logo=github&color=4F46E5&labelColor=1E1B4B)](https://github.com/mindcreative134-creator/nextplayer/releases)
  [![Platform](https://img.shields.io/badge/Platform-Android_8.0+-3DDC84?style=for-the-badge&logo=android&logoColor=white&labelColor=064E3B)](https://github.com/mindcreative134-creator/nextplayer)
  [![Languages](https://img.shields.io/badge/Languages-Kotlin_%7C_C%2B%2B-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white&labelColor=2E1065)](https://github.com/mindcreative134-creator/nextplayer)
  [![UI Framework](https://img.shields.io/badge/UI-Jetpack_Compose_M3-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white&labelColor=172554)](https://github.com/mindcreative134-creator/nextplayer)
  [![License](https://img.shields.io/badge/License-AGPL_v3-EF4444?style=for-the-badge&logo=open-source-initiative&logoColor=white&labelColor=450A0A)](https://github.com/mindcreative134-creator/nextplayer/blob/main/LICENSE)

  <br>

  <p align="center">
    <a href="#-overview"><b>Overview</b></a> •
    <a href="#-lineage--acknowledgment"><b>Lineage & Credits</b></a> •
    <a href="#-visual-showcase"><b>Screenshots</b></a> •
    <a href="#-feature-matrix"><b>Comparison</b></a> •
    <a href="#-key-features"><b>Features</b></a> •
    <a href="#-tech-stack--architecture"><b>Architecture</b></a> •
    <a href="#-building-from-source"><b>Build Guide</b></a>
  </p>

</div>

---

> [!NOTE]
> ### 🌟 Heritage & Evolution
> **Next Player** is an advanced open-source evolution building upon and incorporating core architectural strengths from **[MPV-infinity](https://github.com/ZHINFINITY/Mpv-infinity)** (by ZHINFINITY) and the **[mpvRx](https://github.com/Riteshp2001/mpvRx)** ecosystem. We have combined MPV-infinity's powerful libmpv + Media3 dual-engine foundation with our brand new **Shorts/Reels vertical feed**, **refined Material 3 Expressive frosted glass UI**, **enhanced gesture navigation**, **modernized theme engine**, and **seamless playback optimizations**.

---

## 🌟 Overview

**Next Player** represents the next generation of mobile media playback. While conventional video players force you to choose between standard platform decoders (limited codec support, no shaders) and heavyweight standalone engines (high battery drain, tricky HDR fallback), Next Player seamlessly unites the best of both worlds:

1. **Desktop-Grade libmpv Engine**: Uncompromising format playback (MKV, MP4, WebM, AVI, FLV, TS, etc.), full ASS/SSA styling, custom GLSL shader chains (Anime4K, debanding), and Lua/JS scripting.
2. **Platform-Optimized AndroidX Media3 (ExoPlayer)**: Zero-overhead hardware acceleration, battery-sipping playback, and native Dolby Vision (Profile 5, 8, 8.1) + HDR10+ support.
3. **Smart Dynamic Engine Router**: Auto-routes high-bitrate Dolby Vision and HLS/DASH streams through Media3, while channeling complex anime and multi-audio MKVs through MPV.
4. **All-In-One Modern Media Hub**: Video folders, an immersive vertical Shorts feed, a dedicated audiophile Music player, and private media vaults.

---

## 📸 Visual Showcase

<div align="center">
  <table>
    <tr>
      <td align="center" width="33%">
        <img src="PlayStore_Phone_Screenshots/1_all_format_4k.png" alt="All Formats 4K Playback" style="border-radius: 16px;" /><br>
        <b>🎬 All Formats 4K & HDR</b><br>
        <sub>Ultra-HD 60/120fps with Zero Lag</sub>
      </td>
      <td align="center" width="33%">
        <img src="PlayStore_Phone_Screenshots/2_hardware_acceleration.png" alt="Hardware Acceleration" style="border-radius: 16px;" /><br>
        <b>⚙️ Dual Hardware Engines</b><br>
        <sub>MPV + Media3 with Auto-Switching</sub>
      </td>
      <td align="center" width="33%">
        <img src="PlayStore_Phone_Screenshots/3_folder_manager.png" alt="Smart Folder Library" style="border-radius: 16px;" /><br>
        <b>📁 Smart Media Library</b><br>
        <sub>Album Grid, Tree View & Pinned Folders</sub>
      </td>
    </tr>
    <tr>
      <td align="center" width="33%">
        <img src="PlayStore_Phone_Screenshots/4_subtitles_audio.png" alt="Subtitles and Equalizer" style="border-radius: 16px;" /><br>
        <b>📝 Subtitles & Equalizer</b><br>
        <sub>Dual Subtitles, Font Styling & 7.1 Audio</sub>
      </td>
      <td align="center" width="33%">
        <img src="PlayStore_Phone_Screenshots/5_floating_pip.png" alt="Floating PiP & Background" style="border-radius: 16px;" /><br>
        <b>🪟 Floating PiP & Background</b><br>
        <sub>Seamless Multi-Tasking Everywhere</sub>
      </td>
      <td align="center" width="33%">
        <img src="PlayStore_Phone_Screenshots/6_precise_frame_seeking.png" alt="Precise Frame Seeking" style="border-radius: 16px;" /><br>
        <b>⏱️ Frame-by-Frame Seeking</b><br>
        <sub>Millisecond Precision Navigation</sub>
      </td>
    </tr>
  </table>
</div>

<br>

<div align="center">
  <img src="docs/showcase/player-glass.jpg" width="96%" alt="Frosted Glass Surface" style="border-radius: 20px; box-shadow: 0 10px 30px rgba(0,0,0,0.5);" /><br>
  <sub><i>Next Player's signature Material 3 Expressive frosted glass interface with dynamic controls</i></sub>
</div>

---

## ⚔️ Feature Matrix

| Capability | Next Player ⚡ | Standard MPV-Android | Generic Android Players |
|---|:---:|:---:|:---:|
| **Dual Engine (MPV + Media3)** | ✅ **Yes (Auto-Routing)** | ❌ MPV Only | ❌ ExoPlayer Only |
| **Dolby Vision (Profiles 5, 7, 8)** | ✅ **Native + Fallback** | ⚠️ Partial | ⚠️ Limited Profile 5 |
| **Anime4K Real-Time Shaders** | ✅ **7 Preset Tiers + Thermal Guard** | ⚠️ Manual Config | ❌ Not Supported |
| **Shorts & Reels Snap Feed** | ✅ **Built-in Vertical Feed** | ❌ None | ❌ None |
| **Audiophile Offline Music Suite**| ✅ **Full Player with Album Art** | ❌ Basic Video Audio | ❌ Separate App |
| **Jellyfin & Seerr Streaming** | ✅ **Native Integrated Client** | ❌ None | ❌ None |
| **yt-dlp Native Streaming** | ✅ **Built-in Python Bridge** | ❌ None | ❌ None |
| **Network Streaming (SMB/FTP/WebDAV)**| ✅ **Native Clients** | ⚠️ Protocols Only | ❌ Rare / Paid |
| **Dual Subtitles (Simultaneous)** | ✅ **Primary + Secondary** | ⚠️ Complex Lua | ❌ Single Only |
| **AI Subtitle Translation & Format**| ✅ **OpenAI / Claude / Groq** | ❌ None | ❌ None |
| **200% Smart Volume Boost** | ✅ **Gesture-Driven** | ⚠️ Config Tweak | ❌ 100% Hardware Cap |
| **Material 3 Expressive Design** | ✅ **Frosted Glass + 25+ Themes** | ❌ Classical UI | ❌ Ad-Heavy Clutter |

---

## 🚀 Key Features

### ⚙️ 1. Dual Playback Engine Architecture
Next Player revolutionizes playback flexibility with its unified dual-engine pipeline:
- **libmpv Core**: Full compilation of `libmpv` targeting modern Android ABIs. Enjoy zero-latency playback of exotic codecs, full soft-sub ASS typesetting, custom shaders, and Lua scripting.
- **AndroidX Media3 (ExoPlayer)**: Provides platform-native hardware rendering, ultra-low power consumption, and direct access to system HDR metadata pathways.
- **Automatic Codec Routing**: Next Player inspects container metadata and seamlessly delegates playback to Media3 for Dolby Vision or adaptive streams, leaving heavy MKV/anime workloads to MPV.
- **On-The-Fly Switching**: Change playback engines instantly from the in-player decoder sheet without rewinding or buffering.

---

### 🎨 2. Material 3 Expressive & Frosted Glass Design
- **Expressive Pill Controls**: Floating translucent navigation and playback pills with spring-loaded physics and backdrop blur.
- **25+ Curated Color Schemes**: Dynamic Material You (adapts to wallpaper), Catppuccin, Nord, Tokyo Night, Rosé Pine, Gruvbox, Dracula, Cyberpunk, and more.
- **AMOLED Pure Black**: Dedicated high-contrast pure black mode for maximum battery savings on OLED screens.
- **5 Animation Styles**: Customize control entrance/exit animations (Elastic Bounce, Cinematic Scale, Slide Up, Minimal Fade, Default).
- **Customizable Buttons**: 4 layout zones with over 25 assignable action buttons (A-B loop, aspect ratio, audio stream, PiP, screenshot, etc.).

---

### 📱 3. Dedicated Shorts & Reels Feed
- **Vertical Full-Screen Reels**: Browse short-form videos through an intuitive, swipeable vertical feed.
- **Automatic Classification**: Videos with vertical aspect ratios (< 60s or 9:16) are automatically grouped into the Shorts section.
- **Smooth Snap-Scrolling**: Seamless pager transitions, instant preloading, and auto-looping playback.

---

### 🎵 4. Audiophile Music Player & Visualizer
- **Complete Offline Audio Suite**: Dedicated library with Songs, Albums, and Artists tabs with embedded cover art retrieval.
- **Hi-Res Audio Decoding**: Bundled Jellyfin FFmpeg Media3 decoder for FLAC, ALAC, Opus, APE, WavPack, and DSD.
- **Multi-Channel Surround**: 7.1 and 5.1 spatial audio passthrough for Dolby Atmos, DTS-HD, and TrueHD.
- **3D Fluid Audio Blob Visualizer**: OpenGL ES 3.0 reactive audio visualizer with touch interaction, zoom, and bloom lighting.

---

### 📺 5. 4K/8K HDR, Codecs & Shaders
- **Dolby Vision & HDR10+**: Seamless decoding of Dolby Vision (Profile 5, Profile 7 MEL/FEL fallback, Profile 8) and HDR10+.
- **Anime4K Real-Time Upscaling**: Bundled with [Anime4K](https://github.com/bloc97/Anime4K) shader presets (Tiers A, B, C, A+, B+, C+) for crystal-clear anime upscaling.
- **Smart Thermal Guard**: Proactively adjusts shader quality tiers when device thermal headroom decreases to prevent dropped frames.
- **GPU Deband & Dithering**: Eliminates color banding artifacts with fine-grained threshold and grain controls.
- **Dynamic Display Refresh Rate**: Auto-switches display refresh rate (24Hz, 48Hz, 60Hz, 120Hz) to match the media's native frame rate.
- **Ambient Lighting Mode**: Dynamic `GLOW` and `FRAME_EXTEND` runtime GLSL shaders that radiate video edge colors into surrounding letterboxes.

---

### 📝 6. Subtitle Powerhouse & AI Intelligence
- **Dual Subtitles**: Render two independent subtitle tracks at the same time (e.g. target language dialogue + native audio signs).
- **Advanced ASS/SSA Typesetting**: Full vector font rendering, custom fonts (`.ttf`/`.otf`), font cache manager, and sizing controls.
- **Online Downloader**: Built-in multi-source search across SubtitleHub (6 aggregated providers), Wyzie, and TMDB.
- **AI Subtitle Translation**: Translate and restyle subtitles in real time using OpenAI, Anthropic Claude, Groq, or OpenRouter APIs.
- **Speech-to-Text Transcription**: Auto-generate subtitles from speech using cloud providers or offline Whisper models.

---

### 🌐 7. Network Streaming & Ecosystem
- **Network Storage**: Built-in clients for SMB (Windows Samba), FTP/FTPS, and WebDAV.
- **Native Jellyfin Client**: Connect directly to your Jellyfin media server, browse libraries with posters, and stream with resume points.
- **Seerr & Overseerr Integration**: Search media libraries and submit requests directly within the app.
- **yt-dlp Native Python Bridge**: Stream videos from YouTube, Twitch, Bilibili, and 1000+ websites with custom resolution and codec pickers.
- **Google Cast**: Cast local and network streams directly to Chromecast and smart TVs with handoff support.
- **Syncplay**: Join synchronized viewing rooms to watch movies with friends in perfect lockstep.

---

### 🖐️ 8. Ergonomic Gestures & Precision Controls
- **Triple-Zone Double Tap**: Configurable seek zones (left 10s, center pause, right 10s) with continuous multi-tap accumulation.
- **Vertical Swipe Sliders**: Smooth gesture controls for brightness (left) and volume (right) with up to **200% smart volume boost**.
- **Horizontal Swipe Seek**: Precision timeline scrubbing with large time/delta overlay and thumbnail preview.
- **Pinch-to-Zoom & Pan**: Multi-touch zoom from 0.5x to 3x with single-finger viewport panning.
- **Dynamic Speed Boost**: Long-press for instant 2x/3x speed boost with dynamic slider presets.
- **Subtitle Gestures**: Long-press to drag subtitles anywhere on screen; pinch to resize subtitle text live.
- **Frame-by-Frame Navigation**: Step through individual frames backward and forward with millisecond counters.

---

## 🛠️ Tech Stack & Architecture

```
┌──────────────────────────────────────────────────────────────┐
│                    Next Player UI Layer                      │
│     Jetpack Compose • Material 3 Expressive • Koin DI        │
└──────────────┬───────────────────────────────┬───────────────┘
               │                               │
┌──────────────▼──────────────┐ ┌──────────────▼───────────────┐
│     libmpv Engine (C/JNI)   │ │  AndroidX Media3 (ExoPlayer) │
│  • Shaders (Anime4K, HDR)   │ │  • Native Codec Pipeline     │
│  • QuickJS & Lua Scripting  │ │  • Jellyfin FFmpeg Audio     │
│  • ASS/SSA Subtitle Core    │ │  • Dolby Vision Profile 5/8  │
└──────────────┬──────────────┘ └──────────────┬───────────────┘
               │                               │
┌──────────────▼───────────────────────────────▼───────────────┐
│                     Smart Dynamic Router                     │
│        Auto-Detects Codecs, HDR, Protocols & Formats         │
└──────────────────────────────┬───────────────────────────────┘
                               │
┌──────────────────────────────▼───────────────────────────────┐
│                  Media, Data & Network Layer                 │
│ Room Database • SMB/FTP/WebDAV • Jellyfin • yt-dlp • OkHttp4 │
└──────────────────────────────────────────────────────────────┘
```

| Component | Implementation |
|---|---|
| **Core Architecture** | Clean Architecture with MVVM + MVI, Kotlin Coroutines, and Flow |
| **UI Stack** | 100% Jetpack Compose with Material 3 Expressive components and custom canvas shaders |
| **Video Decoding** | `libmpv` (C API via custom JNI bindings) + `androidx.media3:media3-exoplayer` |
| **Audio Decoding** | `org.jellyfin.media3:media3-ffmpeg-decoder` (TrueHD, DTS, Atmos, FLAC, Opus) |
| **Shaders & Post-FX** | OpenGL ES 3.0, Vulkan (gpu-next), Anime4K, and hdr-toys GLSL chains |
| **Local Cache & DB** | Room Database with SQLite Flow observers |
| **Scripting Engine** | QuickJS-NG for JavaScript and embedded Lua 5.2 |
| **Network Protocols** | `smbj` (SMB2/3), `jsch` (SFTP), `sardine-android` (WebDAV), OkHttp 4 |

---

## 🔨 Building from Source

### Prerequisites
- **Java Development Kit**: JDK 17 or JDK 21 (recommended)
- **Android SDK**: Build Tools `35.0.0`+, SDK Platform `35`
- **Android NDK**: Version `27.0.12077973` or compatible
- **Git**

### Build Commands

```bash
# 1. Clone the repository
git clone https://github.com/mindcreative134-creator/nextplayer.git
cd nextplayer

# 2. Build Debug APK (Playstore or Standard flavor)
./gradlew.bat :app:assemblePlaystoreDebug
# or for standard:
./gradlew.bat :app:assembleStandardDebug

# 3. Build Release Bundle / APK
./gradlew.bat :app:assembleStandardRelease
```

### Supported ABI Architectures
- `arm64-v8a` — Optimized for modern 64-bit Android smartphones & tablets (Recommended)
- `armeabi-v7a` — For legacy 32-bit devices
- `x86_64` — For 64-bit Android emulators and ChromeOS devices
- `x86` — For 32-bit x86 devices
- `universal` — Multi-architecture fat APK

---

## 🤝 Lineage & Acknowledgments

**Next Player** stands on the shoulders of giants in the open-source multimedia landscape. We express our deepest gratitude and recognition to the following projects and maintainers:

- **[MPV-infinity](https://github.com/ZHINFINITY/Mpv-infinity)** by **ZHINFINITY**: A foundational pillar whose libmpv and Media3 dual-engine architecture, engine routing, and player groundwork played an integral role in shaping Next Player.
- **[mpvRx](https://github.com/Riteshp2001/mpvRx)** by **Ritesh Pandit**: Inspiring core player concepts and lineage design.
- **[mpv](https://mpv.io/)** & **[mpv-android](https://github.com/mpv-android/mpv-android)**: The gold standard open-source media player engine.
- **[AndroidX Media3](https://developer.android.com/jetpack/androidx/releases/media3)**: Google's media framework powering hardware-accelerated playback.
- **[Jellyfin](https://github.com/jellyfin/jellyfin-androidx-media)**: For the exceptional Media3 FFmpeg audio decoder.
- **[Anime4K](https://github.com/bloc97/Anime4K)** by **bloc97**: State-of-the-art anime scaling GLSL shaders.
- **[hdr-toys](https://github.com/natural-harmonia-gropius/hdr-toys)**: Shaders for high-dynamic-range tonemapping and color transform.
- **[SunnyVishnu3](https://github.com/SunnyVishnu3)**: For yt-dlp native integration logic and Android SDK 29+ bypass.
- **[Rosemoe Sora Editor](https://github.com/Rosemoe/sora-editor)**: High-performance code editor for embedded scripts.

---

## 📜 License

Next Player is licensed under the **GNU Affero General Public License v3.0 or later (AGPL-3.0-or-later)**.
See the [`LICENSE`](LICENSE) file for the full license text.

```
Next Player - High-Performance Android Media Player
Copyright (C) 2026 Next Player Contributors & Upstream Authors

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published
by the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
```

---

<div align="center">
  <b>Built with passion for media enthusiasts worldwide.</b><br>
  <sub>⭐ If you enjoy Next Player, don't forget to star the repository on GitHub!</sub>
</div>
