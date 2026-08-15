# BearGuard Mobile

Sibling project to [Bearguard](https://github.com/trophyscar-bit/Bearguard) (the Windows/JavaFX
bot driving Whiteout Survival on a MuMu emulator). This is the native-Android version: no PC, no
ADB, no emulator — runs directly on-device against a real WS install.

Separate repo on purpose, not a git fork — the two codebases share almost no buildable code
(JavaFX/Maven/tess4j on Windows vs Kotlin/Gradle/AccessibilityService/ML Kit here). What they
*should* share over time is calibration data (pixel coordinates, crop rects, OCR settings) — see
"Shared calibration" below.

## Architecture

- **`BearGuardAccessibilityService`** — the whole engine. Two primitives, same shape as the
  Windows app's `tapPoint()` / `screencap()`:
  - `captureScreenshot()` — `AccessibilityService.takeScreenshot()` (API 30+). No MediaProjection
    "start recording?" dialog, no repeat permission prompts.
  - `tap(x, y)` — `dispatchGesture()` with a single-point `Path`.
- Scoped to `com.gof.global` (Whiteout Survival's real package name) via `packageNames` in
  `accessibility_service_config.xml` — this service literally cannot see or touch any other app.
- `minSdk = 30` deliberately, to guarantee `takeScreenshot()` exists with no fallback path needed.
- No root required. Not Play-Store-distributable (Accessibility Service automation of another
  app gets rejected by policy) — sideload only, same as any personal-use APK.

## Status (2026-08-15)

Proven working, live, in MuMu (Android 15 / API 35, `127.0.0.1:16384`):
- Builds clean (Gradle 8.9, AGP 8.5.2, Kotlin 1.9.24).
- Installs, launches, no crash.
- Accessibility service enables and connects.
- `captureScreenshot()` returns a real 720x1280 bitmap of the live WS screen.

Not yet built:
- `tap()` is implemented but not yet exercised against a real target (untested beyond compiling).
- Zero WS-specific automation logic — no Routine-equivalent classes exist yet. Everything in
  `fg-tasks/.../*Routine.java` on the Windows side needs deliberate, one-at-a-time porting, not a
  bulk copy (every touchpoint that calls the old ADB-based tap/capture layer needs rewriting
  against the two primitives above).
- Real hardware target: a Samsung Galaxy Tab A16 (2000x1200, ordered, not yet arrived). MuMu is
  standing in for dev/test in the meantime, but every pixel coordinate calibrated against MuMu's
  720x1280 profile is **not** valid on the tablet's real resolution — full recalibration pass
  needed once it arrives.

## Build

Toolchain lives outside the repo (not portable across machines yet — no `gradlew` wrapper
checked in):
```
JAVA_HOME=C:/Frostguard-tools/jdk-21.0.12+8
ANDROID_HOME=C:/BearguardAndroid-tools
PATH="$ANDROID_HOME/../BearguardAndroid-tools/gradle-8.9/bin:$PATH"   # gradle.bat
```
`local.properties` (gitignored) points `sdk.dir` at `ANDROID_HOME`.

```
gradle.bat assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Enabling the service for testing (skips the manual Settings tap):
```
adb shell am force-stop com.bearguard.mobile
adb shell settings put secure enabled_accessibility_services com.bearguard.mobile/com.bearguard.mobile.service.BearGuardAccessibilityService
```

## Shared calibration

Not yet extracted. Once both platforms have real pixel data worth keeping in sync, pull it into a
small JSON/YAML file both repos reference rather than re-deriving twice by hand.
