<div align="center">

# Reeldrop

### A modern Android video & audio downloader powered by yt-dlp

<p>
  <a href="https://github.com/chatpataprani/Reeldrop">
    <img src="https://img.shields.io/github/stars/chatpataprani/Reeldrop?style=for-the-badge&logo=github&label=Stars" alt="GitHub Stars">
  </a>
  <a href="https://github.com/chatpataprani/Reeldrop/releases/tag/reeldrop-latest">
    <img src="https://img.shields.io/github/v/release/chatpataprani/Reeldrop?include_prereleases&style=for-the-badge&logo=android&label=Release" alt="Release">
  </a>
  <a href="https://github.com/chatpataprani/Reeldrop/actions/workflows/build.yml">
    <img src="https://img.shields.io/github/actions/workflow/status/chatpataprani/Reeldrop/build.yml?style=for-the-badge&logo=github-actions&label=Build" alt="Build">
  </a>
  <a href="https://github.com/chatpataprani/Reeldrop/blob/main/LICENSE">
    <img src="https://img.shields.io/github/license/chatpataprani/Reeldrop?style=for-the-badge&logo=opensourceinitiative&label=License" alt="License">
  </a>
</p>

<p>
  <img src="https://img.shields.io/badge/Android-7.0%2B-3DDC84?style=flat-square&logo=android&logoColor=white" alt="Android">
  <img src="https://img.shields.io/badge/Kotlin-100%25-7F52FF?style=flat-square&logo=kotlin&logoColor=white" alt="Kotlin">
  <img src="https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white" alt="Jetpack Compose">
  <img src="https://img.shields.io/badge/yt--dlp-powered-FF0000?style=flat-square&logo=youtube&logoColor=white" alt="yt-dlp">
</p>

<p>
  <a href="https://github.com/chatpataprani/Reeldrop/releases/download/reeldrop-latest/reeldrop-debug.apk">
    <img src="https://img.shields.io/badge/Download-Latest%20APK-2ea44f?style=for-the-badge&logo=android&logoColor=white" alt="Download Latest APK">
  </a>
</p>

</div>

---

## ✨ About

**Reeldrop** is an Android frontend for **yt-dlp**, designed to make downloading supported video and audio content simple, fast, and accessible.

The project is derived from the open-source **Seal** codebase and has been rebranded and adapted as Reeldrop. The upstream project and third-party components remain credited and licensed according to their respective licenses.

> **Note:** Reeldrop is not affiliated with or endorsed by the original Seal project.

## 🚀 Features

| Feature | Description |
|---|---|
| 🎬 Video downloads | Download videos from sites supported by yt-dlp |
| 🎵 Audio extraction | Extract audio from supported media |
| 📚 Playlists | Download complete playlists |
| 🖼️ Metadata | Preserve thumbnails and metadata where supported |
| 💬 Subtitles | Download and embed subtitles |
| ⚡ aria2c | Use aria2c as an external downloader |
| 🧩 Custom commands | Create and execute custom yt-dlp command templates |
| 🍪 Cookies | Support authenticated websites through cookies |
| 📥 Download manager | Manage downloads, history and queued tasks |
| 🎨 Material 3 | Modern Android interface with dynamic theming |
| 🔗 Share support | Send supported URLs directly to Reeldrop |
| 🌐 Network controls | Proxy and IPv4-related options |

## 📱 Screenshots

Screenshots can be added here as the interface evolves.

<!--
<img src="docs/screenshots/home.png" width="240">
<img src="docs/screenshots/download.png" width="240">
<img src="docs/screenshots/settings.png" width="240">
-->

## ⬇️ Download

### Latest APK

<p align="center">
  <a href="https://github.com/chatpataprani/Reeldrop/releases/download/reeldrop-latest/reeldrop-debug.apk">
    <img src="https://img.shields.io/badge/Download%20Reeldrop-APK-34A853?style=for-the-badge&logo=android&logoColor=white" alt="Download Reeldrop APK">
  </a>
</p>

Every successful build on the `main` branch publishes the latest APK to the `reeldrop-latest` release.

> Latest APK is published automatically after the build finishes successfully.

### Build artifacts

Each GitHub Actions build also uploads the APK as a workflow artifact for that specific run.

## 🛠️ Build from source

### Requirements

- Android Studio
- JDK 21
- Android SDK
- Git

### Clone

```bash
git clone https://github.com/chatpataprani/Reeldrop.git
cd Reeldrop
```

### Build

```bash
./gradlew assembleDebug
```

The generated APK can be found under:

```text
app/build/outputs/apk/
```

## 🧱 Technology

<p>
  <img src="https://img.shields.io/badge/Language-Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white" alt="Kotlin">
  <img src="https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white" alt="Jetpack Compose">
  <img src="https://img.shields.io/badge/Design-Material%203-757575?style=flat-square&logo=materialdesign&logoColor=white" alt="Material 3">
  <img src="https://img.shields.io/badge/Downloader-yt--dlp-FF0000?style=flat-square&logo=youtube&logoColor=white" alt="yt-dlp">
  <img src="https://img.shields.io/badge/Build-Gradle-02303A?style=flat-square&logo=gradle&logoColor=white" alt="Gradle">
</p>

## 🤝 Credits & Attribution

Reeldrop is derived from **Seal**, an open-source Android frontend for yt-dlp created by **JunkFood02**.

Upstream and third-party technologies include:

- [Seal](https://github.com/JunkFood02/Seal)
- [yt-dlp](https://github.com/yt-dlp/yt-dlp)
- [youtubedl-android](https://github.com/yausername/youtubedl-android)
- [Read You](https://github.com/Ashinch/ReadYou)
- [Music You](https://github.com/Kyant0/MusicYou)
- [aria2](https://github.com/aria2/aria2)
- [dvd](https://github.com/yausername/dvd)
- [Material Color Utilities](https://github.com/material-foundation/material-color-utilities)
- [Monet](https://github.com/Kyant0/Monet)

The original Seal README identifies these projects as part of its credits. citeturn0search2

## 👨‍💻 Developer

<div align="center">

### Amir · @chatpataprani

<a href="https://github.com/chatpataprani">
  <img src="https://img.shields.io/badge/GitHub-@chatpataprani-181717?style=for-the-badge&logo=github&logoColor=white" alt="GitHub">
</a>
<a href="https://instagram.com/chatpataprani">
  <img src="https://img.shields.io/badge/Instagram-@chatpataprani-E4405F?style=for-the-badge&logo=instagram&logoColor=white" alt="Instagram">
</a>

</div>

## 📄 License

Reeldrop is distributed under the **GNU General Public License v3.0**.

See [LICENSE](LICENSE) for the complete license text.

The source code derived from Seal remains subject to the applicable GPLv3 terms. The original Seal project also states restrictions around use of the Seal name for derivatives, which is why this project uses the independent **Reeldrop** name and branding. citeturn0search2

## ⚠️ Disclaimer

Reeldrop is a downloader frontend. You are responsible for complying with the terms of service, copyright rules, privacy requirements, and applicable laws for the content and websites you access.

---

<div align="center">

**Reeldrop — download what you're allowed to download.**

⭐ Star the repository if you find it useful.

</div>
