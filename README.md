# APP-OPERATOR

APP-OPERATOR is an Android application shell for exploring a user-controlled, allowlisted accessibility-observation workflow for Mira.

> **Current status: early prototype.** The app is not a connected AI agent, does not execute missions, and does not click, type, send, delete, or otherwise control other apps. Several visible sections are architecture placeholders or local-only preferences.

## Current capabilities

- Native Android application shell (Evolution 20 UI).
- Local mission text registration; records remain on the device and do not start execution.
- Static allowlist for ChatGPT, Google app/Gemini, and Chrome package identifiers.
- Accessibility service observes only window-state and window-content events for allowlisted packages.
- UI snapshots are bounded and exclude password and editable-field text; snapshots are not currently persisted or transmitted.
- Emergency Stop preference pauses accessibility-event processing before active-window retrieval. The Android accessibility service remains enabled until the user disables it in Android Settings.
- No external model, API, agent, or server connection is implemented.

## Security and privacy boundaries

- Accessibility access is opt-in and must be enabled by the device owner in Android Accessibility Settings.
- The service uses an explicit package allowlist and re-checks the active window package.
- The current service is observation-only. Gesture dispatch is disabled in the accessibility configuration.
- Emergency Stop is a local processing control, not a system-level service shutdown.
- Authority modes and governance checkboxes in the UI are local presentation preferences; they do not enforce authorization or policy.
- The event stream is local illustrative history, not a tamper-resistant audit log.
- Do not use this prototype with sensitive accounts or data until an independent privacy and security review is complete.

**Platform-policy note:** Android accessibility services are intended to assist users with disabilities. Review the current Android and Google Play accessibility-service policies before distributing this app. Do not represent this prototype as an assistive service unless its actual purpose and functionality meet those requirements.

## Build requirements

- JDK 17
- Android SDK Platform 35
- Android Build Tools 35.0.0
- Gradle 8.9
- Android Gradle Plugin 8.7.3
- Kotlin Android plugin 2.0.21

The repository currently does not include the Gradle Wrapper. The CI workflow installs Gradle 8.9 directly.

## Build locally

From the repository root, with the requirements installed:

```sh
gradle --no-daemon :app:assembleDebug
```

The debug APK is generated at:

```text
app/build/outputs/apk/debug/
```

## CI

GitHub Actions workflow: `.github/workflows/android-build.yml`

It builds the debug APK for pushes to `evolution-20-native-android` and pull requests targeting `main` or `evolution-20-native-android`.

## Project structure

- `app/src/main/java/com/mira/operator/MainActivity.kt` — native UI shell and local controls.
- `app/src/main/java/com/mira/operator/apps/AppRegistry.kt` — package allowlist.
- `app/src/main/java/com/mira/operator/accessibility/OperatorAccessibilityService.kt` — observation-only accessibility service and Emergency Stop enforcement.
- `app/src/main/java/com/mira/operator/accessibility/UIElementFinder.kt` — bounded UI tree inspection helpers.
- `app/src/main/res/xml/operator_accessibility_config.xml` — accessibility service configuration.

## Licensing

No license has been selected or added yet. Until a license is added by the repository owner, do not assume that the source is licensed for redistribution or reuse.
