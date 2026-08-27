# San Andreas Cheat Implementor

A native Kotlin Android overlay for entering GTA San Andreas cheat codes. It uses Shizuku's privileged shell to send individual Android key events (40 ms apart), without root.

## Requirements

- Android 8.0 (API 26) or newer
- [Shizuku](https://shizuku.rikka.app/) installed, started, and authorized for this app as injector access
- Permission to display over other apps
- JDK 17 and Android SDK 35 when building locally

## Build from the command line

On Linux/macOS/Git Bash:

```sh
./gradlew assembleDebug
```

On Windows PowerShell:

```powershell
.\gradlew.bat assembleDebug
```

The APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

## Use

1. Install and start Shizuku (wireless debugging or ADB mode).
2. Open SA Cheat Implementor, grant injector and overlay permissions, then tap **Start Cheat Bubble**.
3. Open GTA San Andreas. Drag the `SA` bubble wherever convenient.
4. Tap the bubble and select a cheat, or enter a custom alphanumeric code and tap **Send**.

The menu is removed before injection, then waits 350 ms so focus returns to GTA. The bubble itself uses `FLAG_NOT_FOCUSABLE`, so it does not take keyboard focus. Some game/Android builds may ignore synthetic keyboard events or may not support the original PC cheat-code mechanism.

## Privacy

There is no backend, database, login, analytics, or network permission. Cheat codes are stored in `CheatRepository.kt`.
