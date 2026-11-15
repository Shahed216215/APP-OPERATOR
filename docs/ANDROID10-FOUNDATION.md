# APP-OPERATOR — Android 10 Foundation

Status: DESIGN BASELINE — implementation and device validation pending.

## Target device profile

Initial target: TECNO KE7, Android 10 (API 29), arm64-v8a, approximately 3.9 GB RAM. The app must remain usable on a constrained device and must not require a paid model/API.

## Current repository baseline (main)

- Android application module: `:app`
- Application ID / namespace: `com.mira.operator`
- `minSdk 29` is compatible with Android 10.
- `compileSdk 35`, Android Gradle Plugin 8.7.3, Kotlin 2.0.21 and Java 17 are configured.
- A launcher Activity, an AccessibilityService declaration, an app allowlist, and a read-only UI element snapshot helper exist.
- Accessibility configuration currently has `canRetrieveWindowContent=true` and `canPerformGestures=false`.
- The service currently observes authorized package window events only. It does not execute taps, swipes, or text entry.
- The service does not currently provide a Mira bridge, task queue, approval UI, action receipt store, or screenshot capture implementation.
- A second nested path `app/app/src/main/AndroidManifest.xml` duplicates the main manifest. It is not referenced by the configured Gradle module and must be reviewed before any cleanup; do not delete it blindly.

## Android 10 compatibility rules

1. Use AccessibilityService APIs available on API 29 for UI-tree inspection and, after a separately reviewed implementation, gesture dispatch.
2. Do not use `AccessibilityService.takeScreenshot()`: it is not available on API 29 (introduced in API 30).
3. If screen pixels are later required, evaluate Android MediaProjection with explicit system consent, a visible foreground notification, and lifecycle handling. Do not silently capture or persist screenshots.
4. Prefer UI-tree semantics over coordinate-only actions. Treat coordinates as a fallback requiring fresh screen bounds and a user-visible action preview.
5. Do not require root, Shizuku, ADB, a desktop computer, paid cloud inference, or a proprietary API for the basic local operator.
6. Avoid heavy on-device vision models on the target device. The first milestone is deterministic, bounded UI inspection and manually approved actions.

## Security boundary

- Accessibility is powerful. It remains off until the user enables it in Android Settings.
- Default to observation-only mode.
- Only inspect packages explicitly enabled by the user in the app's allowlist.
- Never read password fields, OTPs, payment PINs, or credential contents.
- Do not automate banking, payment, financial transfers, account recovery, security settings, or irreversible deletion.
- Require a concrete action preview and explicit approval before any write/action. A changed target or changed parameters invalidate approval.
- Emergency Stop must immediately block queued and future actions.
- Keep action receipts minimal: timestamp, package, action type, approval reference, outcome. Do not store raw screen text, passwords, screenshots, or full UI dumps.
- Fail closed when package identity, target node, approval state, or task provenance cannot be verified.

## Milestones

- M0 — repository and compatibility audit (complete as a read-only inspection; no build/device test yet).
- M1 — Android 10-safe observation-only service and permission/status screen.
- M2 — local action preview, one-shot approval, emergency stop, bounded action executor.
- M3 — local audit receipts and offline tests.
- M4 — loopback-only Mira Bridge adapter with authentication and replay protection.
- M5 — TECNO KE7 manual validation, beginning with a harmless Calculator task.

## Validation language

Use PASS only with reproducible build/test/device evidence. Otherwise use FAIL, INCONCLUSIVE, NOT_APPROVED, or REVIEW_REQUIRED. A successful GitHub write is not proof that the Android app builds or works on the phone.
