# Coffee Pulse Android

Native Android implementation of Coffee Pulse, built as a separate Gradle project under `android/`.

The app is a native Kotlin + Jetpack Compose implementation with a local-only
brew timer, DataStore-backed settings, Room-backed brew history, foreground
timer notifications, programmatic audio cues, and haptic feedback.

## Requirements

- Android Studio with Android SDK 36 installed
- Android Studio's bundled JDK or JDK 17

## Assets

Bundled font resources mirror the web app typography:

- `Manrope` weights 300, 400, 500, 600, and 700
- `Bebas Neue` weight 400

The font files were sourced from Google Fonts.

## Build

From this directory:

```sh
./gradlew testDebugUnitTest
./gradlew connectedDebugAndroidTest
./gradlew assembleDebug
```

The project intentionally keeps the existing React/Vite web app untouched.
