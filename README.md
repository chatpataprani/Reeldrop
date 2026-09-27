# Reeldrop

A minimal Android downloader built around one workflow:

Instagram -> Share -> Reeldrop -> download starts immediately -> keep using Instagram.

No paste box. No second download button.

## What is implemented

- Android ACTION_SEND share target for text/plain
- URL extraction from shared text
- Immediate foreground download hand-off
- Activity finishes after starting the download
- Notification progress while downloading
- Completion/failure notification
- MediaStore saving to Movies/Reeldrop on Android 10+
- Resolver isolated behind ResolverClient

## Resolver order

1. Configured Cobalt-compatible resolver (if `RESOLVER_BASE_URL` is set).
2. Public Cobalt fallbacks: `co.wuk.sh`, `co.tskau.team`, and `cobalt-api.hyper.lol`.
3. Built-in backup API: `hostmyhosting.site/api/all_dl.php?url=`.

The backup endpoint is queried with the shared URL URL-encoded. Because its live response could not be verified from the build environment, Reeldrop accepts either a direct media URL or several common JSON URL fields. If the endpoint changes its response format, only `ResolverClient.kt` needs updating.

The public Cobalt instances are third-party/community services, so they can change, rate-limit, or require protection. Reeldrop tries the next resolver automatically instead of depending on one service. For maximum reliability, a Cobalt instance you control is still preferable.

## Important: media resolution

The Android share/download flow is local, but an Instagram share URL is not itself a video file. Reeldrop therefore calls a media resolver.

This project uses the Cobalt API protocol. Cobalt documents Instagram support, but its API documentation says hosted instances can have bot protection and are not intended for use by other projects without permission. For a reliable personal setup, run your own Cobalt instance and point Reeldrop at it.

### Configure your resolver

Edit app/build.gradle.kts:

    buildConfigField("String", "RESOLVER_BASE_URL", "\"https://YOUR-COBALT-HOST/\"")

The endpoint must expose Cobalt's POST / API.

Then rebuild the APK.

## Build

Open the repository in Android Studio and sync Gradle.

    ./gradlew assembleDebug

Install:

    app/build/outputs/apk/debug/app-debug.apk

## Usage

1. Open a public Instagram Reel.
2. Tap Share.
3. Select Reeldrop.
4. The download starts immediately.
5. Reeldrop closes its activity.
6. Continue using Instagram.
7. Follow progress in the notification shade.

## Android implementation

The project targets Android 15 (API 35) and uses a dataSync foreground service because Android documents data transfer/download work under that service type. Android 14+ requires the foreground-service type and matching permission.

Android 13+ notification permission is requested on first launch so download progress can be shown.

## Current limitations

- The configured resolver must be available.
- Instagram public/login behavior can change and can break extraction.
- Private/login-only media requires an authenticated resolver setup.
- Carousel posts can have different behavior from single-video Reels.
- v0.1 has no download history/settings UI or persistent retry queue.
- The resolver endpoint is intentionally not hard-coded to an untrusted public service.

Use Reeldrop only for media you have permission to download or reuse.
