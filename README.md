<div align="center">

  <img src="PlayStore_App_Icon_512x512.png" width="160" height="160" alt="Next Player Logo" style="border-radius: 32px; box-shadow: 0 12px 36px rgba(0, 0, 0, 0.45); margin-bottom: 16px;" />

  # ⚡ Next Player

  ### 🎬 The Definitive High-Performance Media Suite for Android
  **Dual Playback Engines (MPV + Media3) • Material 3 Expressive • Studio HDR & Audio • Shorts Feed • Cloud Streaming**

  <br>

  [![Google Play](https://img.shields.io/badge/Google_Play-Live_Now-34A853?style=for-the-badge&logo=google-play&logoColor=white&labelColor=0F5132)](https://play.google.com/store/apps/details?id=com.nextplayer.pro)
  [![Latest Release](https://img.shields.io/github/v/release/mindcreative134-creator/nextplayer?style=for-the-badge&logo=github&color=4F46E5&labelColor=1E1B4B)](https://github.com/mindcreative134-creator/nextplayer/releases)
  [![Platform](https://img.shields.io/badge/Platform-Android_8.0+-3DDC84?style=for-the-badge&logo=android&logoColor=white&labelColor=064E3B)](https://github.com/mindcreative134-creator/nextplayer)
  [![Languages](https://img.shields.io/badge/Languages-Kotlin_%7C_C%2B%2B-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white&labelColor=2E1065)](https://github.com/mindcreative134-creator/nextplayer)
  [![UI Framework](https://img.shields.io/badge/UI-Jetpack_Compose_M3-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white&labelColor=172554)](https://github.com/mindcreative134-creator/nextplayer)
  [![License](https://img.shields.io/badge/License-AGPL_v3-EF4444?style=for-the-badge&logo=open-source-initiative&logoColor=white&labelColor=450A0A)](https://github.com/mindcreative134-creator/nextplayer/blob/main/LICENSE)

  <br><br>

  <a href="https://play.google.com/store/apps/details?id=com.nextplayer.pro">
    <img src="https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png" height="72" alt="Get it on Google Play" />
  </a>

  <br><br>

  <p align="center">
    <a href="#-download--install"><b>Download & Install</b></a> •
    <a href="#-lineage--ancestral-tree"><b>Lineage & Heritage</b></a> •
    <a href="#-comparative-analysis-the-lineage-face-off"><b>Comparison Matrix</b></a> •
    <a href="#-visual-showcase"><b>Screenshots</b></a> •
    <a href="#-key-features"><b>Features</b></a> •
    <a href="#-device-compatibility--android-tv"><b>Device Compatibility</b></a> •
    <a href="#-building-from-source"><b>Build Guide</b></a>
  </p>

</div>

---

## 🧬 Lineage & Ancestral Tree

**Next Player** is the culmination of years of community-driven innovation in Android media playback. Rather than reinventing the wheel, Next Player stands proudly upon the foundations laid by visionary open-source developers:

```
                          ┌────────────────────────┐
                          │   mpv (Desktop Core)   │
                          └───────────┬────────────┘
                                      │
                                      ▼
                          ┌────────────────────────┐
                          │      mpv-android       │  ← Pure minimal libmpv port
                          └───────────┬────────────┘
                                      │
                                      ▼
                          ┌────────────────────────┐
                          │         mpvEx          │  ← Power-user property overrides
                          │  (by marlboro-advance) │     & MediaInfo integration
                          └───────────┬────────────┘
                                      │
                                      ▼
                          ┌────────────────────────┐
                          │         mpvRx          │  ← Jetpack Compose M3 redesign,
                          │   (by Riteshp2001)     │     Anime4K tuning & dual subtitles
                          └───────────┬────────────┘
                                      │
                                      ▼
                          ┌────────────────────────┐
                          │      MPV-infinity      │  ← Revolutionary Dual Engine (MPV + Media3),
                          │    (by ZHINFINITY)     │     Dolby Vision routing, Jellyfin & yt-dlp
                          └───────────┬────────────┘
                                      │
                                      ▼
                          ┌────────────────────────┐
                          │      Next Player       │  ← Unified Entertainment Hub:
                          │ (mindcreative134-crea) │     Shorts Feed, Audiophile Music Suite,
                          └────────────────────────┘     Frosted Glass UI & Refined Pipeline
```

> [!NOTE]
> ### 🌟 Lineage Acknowledgments
> - **[mpv-android](https://github.com/mpv-android/mpv-android)**: The bedrock of Android MPV playback, demonstrating that a full desktop Unix media engine can run on mobile devices.
> - **[mpvEx / mpvExtended](https://github.com/marlboro-advance/mpvEx)** by **marlboro-advance**: Pioneered deep Android-specific MPV customizations, advanced property inspector sheets, and low-level media introspection.
> - **[mpvRx](https://github.com/Riteshp2001/mpvRx)** by **Ritesh Pandit**: Elevated the user experience with an exceptional Material 3 Jetpack Compose interface, specialized anime playback enhancements, and intuitive touch controls.
> - **[MPV-infinity](https://github.com/ZHINFINITY/Mpv-infinity)** by **ZHINFINITY**: Created the historic architectural breakthrough of **Dual Playback Engines (libmpv + AndroidX Media3)** with smart auto-routing for difficult Dolby Vision Profile 5/8 streams, native Jellyfin/Seerr integration, and the native Python yt-dlp bridge.

---

## ⚔️ Comparative Analysis: The Lineage Face-Off

Every player in this lineage was engineered with specific philosophies, trade-offs, and target audiences. Here is an honest, comprehensive comparison showing **where each player excels** and where they make deliberate design compromises:

| Feature / Aspect | 📱 mpv-android | ⚙️ mpvEx | 🌸 mpvRx | ♾️ MPV-infinity | ⚡ Next Player |
|---|:---:|:---:|:---:|:---:|:---:|
| **Target Philosophy** | Purist minimalist desktop port | Low-level property tweaking | Modern anime & otaku experience | Dual-engine Dolby Vision & streaming | Unified all-in-one media powerhouse |
| **Engine Architecture** | Single (`libmpv`) | Single (`libmpv`) | Single (`libmpv`) | **Dual Engine** (MPV + Media3) | **Dual Engine** (MPV + Media3) |
| **Dolby Vision (Profile 5, 7, 8)** | ⚠️ Tonemapped (magenta/green tint on some devices) | ⚠️ Tonemapped (requires manual shader tuning) | ⚠️ Shader tonemapped (best for anime HDR) | 🏆 **Native Hardware Route** (ExoPlayer bypass) | 🏆 **Native Hardware Route** (ExoPlayer bypass) |
| **Anime4K Real-Time Shaders** | ⚠️ Manual config | ⚠️ Extended toggles | 🏆 **Pre-tuned Otaku Presets** | ✅ Bundled + Thermal Guard | ✅ Bundled + Thermal Guard |
| **UI Framework & Style** | Legacy Android Views (Minimal OSD) | Legacy Android Views + Extra Sheets | Modern Jetpack Compose (Material 3) | Material 3 Expressive with Deep Menus | 🏆 **Refined Frosted Glass Pill UI** |
| **Shorts / Reels Vertical Feed** | ❌ None | ❌ None | ❌ None | ❌ None | 🏆 **Dedicated Snap Feed** (Exclusive) |
| **Dedicated Music Suite** | ❌ Plays audio as video | ❌ Audio in file list | ❌ Basic file audio | ⚠️ MediaStore list with basic controls | 🏆 **Full Audiophile Player** (Albums, Art, Visualizer) |
| **Jellyfin & Media Servers** | ❌ Manual URL stream | ❌ Manual URL stream | ❌ Basic stream URL | 🏆 **Native Client + Seerr Requests** | 🏆 **Native Client + Seerr Requests** |
| **yt-dlp Video Streaming** | ❌ None | ❌ None | ❌ None | 🏆 **Native Python Bridge** (SDK 29+ bypass) | 🏆 **Native Python Bridge** (SDK 29+ bypass) |
| **Local Network (SMB/FTP/WebDAV)**| ⚠️ Protocol URLs only | ⚠️ Protocol URLs only | ❌ URL only | 🏆 **Native Protocol Clients** | 🏆 **Native Protocol Clients** |
| **Dual Subtitles (Simultaneous)**| ❌ Complex Lua script | ⚠️ Partial script | 🏆 **Native Primary + Secondary** | 🏆 **Native Primary + Secondary** | 🏆 **Native Primary + Secondary** |
| **AI Subtitle & Translation** | ❌ None | ❌ None | ❌ None | 🏆 **OpenAI, Claude, Groq & Whisper** | 🏆 **OpenAI, Claude, Groq & Whisper** |
| **APK Size & Footprint** | 🏆 **Ultra-Light (~20 MB)** | 🏆 **Lightweight (~30 MB)** | ⚖️ Moderate (~48 MB) | 📦 Heavy (~85 MB, multi-engine) | 📦 Heavy (~90 MB, multi-engine + hub) |
| **RAM & Idle Resource Usage** | 🏆 **Lowest RAM (< 60 MB)** | 🏆 **Very Low RAM** | ⚖️ Moderate RAM | ⚖️ Moderate to High RAM | ⚖️ Moderate to High RAM |
| **Configuration Learning Curve** | High (`mpv.conf` required) | High (technical flags) | Low to Moderate (clean UI) | Moderate (deep feature set) | Low to Moderate (intuitive defaults) |

---

### 🎯 Which Player Should You Choose?

- **Choose [mpv-android](https://github.com/mpv-android/mpv-android)** if:
  - You want an ultra-lightweight app under 25 MB with bare minimum RAM consumption.
  - You prefer configuring your player exclusively through text-based `mpv.conf` and `input.conf` files.
  - You prioritize raw simplicity and upstream code tracking above all else.

- **Choose [mpvEx](https://github.com/marlboro-advance/mpvEx)** if:
  - You are a developer or power-user who loves inspecting low-level MPV runtime properties and container metadata on the fly.
  - You want classic Android view stability with extra control toggles without Compose overhead.

- **Choose [mpvRx](https://github.com/Riteshp2001/mpvRx)** if:
  - Your primary passion is watching anime with stylized ASS/SSA subtitles.
  - You want a lightweight, elegant Material 3 interface optimized specifically for video animation without server streaming clutter.

- **Choose [MPV-infinity](https://github.com/ZHINFINITY/Mpv-infinity)** if:
  - You stream 4K Dolby Vision remuxes from a home Jellyfin or SMB server and need the groundbreaking Dual-Engine hardware routing.
  - You want a cinema-centric setup with native yt-dlp web video extraction and Syncplay room watch parties.

- **Choose Next Player** if:
  - You want an **all-in-one entertainment hub** that unifies movies, anime, vertical short-form reels, and your entire offline music library into a single app.
  - You love modern, tactile **Material 3 Expressive frosted glass design** with spring physics, 25+ curated color palettes, and AMOLED pure black.
  - You want the dual-engine power of MPV-infinity combined with a fluid, multi-media experience.

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

## 📥 Download & Install

Next Player is officially published on the **Google Play Store** for automatic updates and verified installation, as well as on **GitHub Releases** for direct APK sideloading:

<div align="center">
  <a href="https://play.google.com/store/apps/details?id=com.nextplayer.pro">
    <img src="https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png" height="74" alt="Get it on Google Play" />
  </a>
  &nbsp;&nbsp;&nbsp;&nbsp;
  <a href="https://github.com/mindcreative134-creator/nextplayer/releases">
    <img src="https://img.shields.io/badge/GitHub_Releases-Download_APK-24292F?style=for-the-badge&logo=github&logoColor=white" height="48" alt="Download APK from GitHub" />
  </a>
</div>

<br>

| Distribution Channel | Package Name | Updates | Best For |
|---|---|---|---|
| 🟢 **Google Play Store** | `com.nextplayer.pro` | Automatic background updates | Most users wanting verified, seamless, stable installation |
| 🐙 **GitHub Releases** | `com.nextplayer.pro` | In-app update check / Direct APK | Devices without Google Play Services, testers & power-users |

---

## 📱 Device Compatibility & Android TV

Next Player is engineered to deliver stellar performance across different Android hardware form factors:

### 📱 Phones & Foldables (Tier 1 Support)
- **Fluid Touch Gestures**: Fine-tuned double-tap zones, 200% volume swipe boost, brightness slider, and pinch-to-zoom (0.5x to 3x).
- **Edge-to-Edge & Foldable Adaptive**: Respects camera cutouts and dynamically adapts layouts during split-screen and device fold/unfold transitions.

### 📟 Tablets & Desktops
- **Responsive Multi-Pane**: Dual-pane file manager, multi-column media grid layouts, and expanded video metadata view.

### 📺 Android TV, Google TV & Smart Screens
**Can you use Next Player on Android TV?** Here is the honest, comprehensive breakdown:

| Capability | Status | How it Works |
|---|:---:|---|
| **📡 Google Cast to TV** | 🏆 **Supported (Built-in)** | Tap the Cast button in the player to stream local videos or network URLs directly to your Chromecast / Android TV / Google TV. |
| **🎮 TV Remote (D-Pad) Navigation** | ✅ **Supported** | If sideloaded onto an Android TV box, the player handles TV remote keys (`DPAD_LEFT`/`RIGHT` to seek, `DPAD_CENTER`/`ENTER` to play/pause). |
| **🔌 Sideload Installation** | ✅ **Supported** | `android.software.leanback` and `touchscreen` features are marked non-required in manifest, allowing installation on Android TV OS. |
| **🏠 10-Foot Leanback Home Launcher** | ⏳ *Planned Roadmap* | The current library UI is designed for touch/pointer. For Android TV, we recommend using Google Cast or a mouse/air-remote for library navigation. |

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
- **[mpvRx](https://github.com/Riteshp2001/mpvRx)** by **Ritesh Pandit**: Inspiring core player concepts, modern UI foundations, and lineage design.
- **[mpvEx / mpvExtended](https://github.com/marlboro-advance/mpvEx)** by **marlboro-advance**: Pioneer of Android MPV power-user extensions, property toggles, and MediaInfo integration.
- **[mpv-android](https://github.com/mpv-android/mpv-android)** & **[mpv](https://mpv.io/)**: The gold standard open-source media player engine that started the entire Android MPV movement.
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
