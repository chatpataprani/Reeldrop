<div align="center">

# YAWR

### yet another yt-dlp wrapper because apparently one wasn't enough.

<p>
  <a href="https://github.com/chatpataprani/Reeldrop">
    <img src="https://img.shields.io/github/stars/chatpataprani/Reeldrop?style=for-the-badge&logo=github&label=stars" alt="GitHub Stars">
  </a>
  <a href="https://github.com/chatpataprani/Reeldrop/releases">
    <img src="https://img.shields.io/github/v/release/chatpataprani/Reeldrop?style=for-the-badge&logo=android&label=release" alt="Latest Release">
  </a>
  <a href="https://github.com/chatpataprani/Reeldrop/actions/workflows/build.yml">
    <img src="https://img.shields.io/github/actions/workflow/status/chatpataprani/Reeldrop/build.yml?style=for-the-badge&logo=github-actions&label=build" alt="Build Status">
  </a>
  <a href="https://github.com/chatpataprani/Reeldrop/blob/main/LICENSE">
    <img src="https://img.shields.io/github/license/chatpataprani/Reeldrop?style=for-the-badge&label=license" alt="License">
  </a>
</p>

<p>
  <img src="https://img.shields.io/badge/Android-7.0%2B-3DDC84?style=flat-square&logo=android&logoColor=white" alt="Android 7.0+">
  <img src="https://img.shields.io/badge/Kotlin-100%25-7F52FF?style=flat-square&logo=kotlin&logoColor=white" alt="Kotlin">
  <img src="https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?style=flat-square&logo=jetpackcompose" alt="Jetpack Compose">
  <img src="https://img.shields.io/badge/yt--dlp-powered-FF0000?style=flat-square&logo=youtube&logoColor=white" alt="yt-dlp">
</p>

<p>
  <a href="https://github.com/chatpataprani/Reeldrop/releases">
    <img src="https://img.shields.io/badge/download-latest%20APK-2ea44f?style=for-the-badge&logo=android&logoColor=white" alt="Download Latest APK">
  </a>
</p>

</div>

---

## so... what is YAWR?

**YAWR** = **Yet Another yt-dlp Wrapper**.

yeah. the name is self-explanatory.

it's an Android downloader built on top of **yt-dlp** because apparently pasting a link and downloading a file needed a whole app.

paste link → pick what you want → download → pretend the 47 build errors never happened.

> made with kotlin, caffeine, and an unreasonable amount of debugging.

## what can this thing do?

| thing | what it does |
|---|---|
| 🎬 video downloads | downloads supported videos through yt-dlp |
| 🎵 audio extraction | turns supported media into audio |
| 📚 playlists | because downloading 1 video at a time is character development |
| 🖼️ metadata | thumbnails + metadata when available |
| 💬 subtitles | downloads/embeds subtitles when supported |
| ⚡ aria2c | optional faster downloader support |
| 🧩 custom commands | for people who enjoy touching things they shouldn't |
| 🍪 cookies | handles sites that need authentication |
| 📥 download manager | queue, history and task management |
| 🎨 Material 3 | modern UI without 900 random buttons |
| 🔗 share support | share a link straight into YAWR |
| 🌐 network controls | proxy + IPv4 options |

## the important part

YAWR isn't trying to be the next super-app.

it's a downloader.

you give it a link.

it downloads the thing.

that's literally the lore.

## 📱 screenshots

coming soon™ because apparently screenshots are also a feature i haven't finished.

<!-- Add screenshots here:
<img src="docs/screenshots/home.png" width="240">
<img src="docs/screenshots/download.png" width="240">
<img src="docs/screenshots/settings.png" width="240">
-->

## ⬇️ download

grab the latest APK from **GitHub Releases**:

**[download YAWR](https://github.com/chatpataprani/Reeldrop/releases)**

> if there isn't an APK there, congrats — you found the part where i'm still fighting Gradle.

## 🛠️ build it yourself

### requirements

- Android Studio
- JDK 21
- Android SDK
- Git
- basic patience
- emotional support

### clone

```bash
git clone https://github.com/chatpataprani/Reeldrop.git
cd Reeldrop
```

### build

```bash
./gradlew assembleDebug
```

then look here:

```text
app/build/outputs/apk/
```

if it builds first try, screenshot it.

i won't believe you otherwise.

## 🧱 made with

- **Kotlin**
- **Jetpack Compose**
- **Material 3**
- **yt-dlp**
- **Gradle**
- **aria2c** — optional

basically a suspicious amount of tooling for something that downloads videos.

## 🤝 open-source stuff

YAWR stands on the shoulders of projects that are way more competent than me:

- [yt-dlp](https://github.com/yt-dlp/yt-dlp)
- [youtubedl-android](https://github.com/yausername/youtubedl-android)
- [Read You](https://github.com/Ashinch/ReadYou)
- [Music You](https://github.com/Kyant0/MusicYou)
- [aria2](https://github.com/aria2/aria2)
- [dvd](https://github.com/yausername/dvd)
- [Material Color Utilities](https://github.com/material-foundation/material-color-utilities)
- [Monet](https://github.com/Kyant0/Monet)

respect to the people who actually know what they're doing.

## 👨‍💻 who made this?

**Amir · [@chatpataprani](https://github.com/chatpataprani)**

self-taught.

mostly powered by curiosity.

occasionally powered by "why the hell is Gradle doing this".

built independently, debugged irresponsibly.

## 📄 license

YAWR is licensed under the **GNU General Public License v3.0**.

see [LICENSE](LICENSE) for the full license text.

## ⚠️ don't be stupid

YAWR is a downloading tool.

only download content you have the legal right or permission to download.

you are responsible for following copyright laws, website terms, privacy requirements, and whatever other rules apply where you live.

## final boss

<div align="center">

### YAWR

**paste link. download. disappear.**

⭐ star the repo if it actually saved you from opening a suspicious website.

</div>
