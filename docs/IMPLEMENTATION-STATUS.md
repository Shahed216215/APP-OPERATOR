# APP-OPERATOR Delivery Status

Branch: `work/android10-foundation-mira-bridge`

| Milestone | Scope | Status |
|---|---|---|
| M1 | Android 10 Accessibility permission and status UI | SOURCE IMPLEMENTED; first CI build passed on an earlier commit; latest app-selection changes await CI validation |
| M2 | Per-action approval; Tap, Swipe, Back, read/write/search/edit/chat workflow | PARTIAL: one-action approval screen, user-managed launcher-app allowlist, UI inspection, Back, Swipe, unique text tap, non-password text entry, exact-query submission and exact-message send primitives added. Device testing and bridge submission pending. |
| M3 | Emergency Stop, verification, privacy-minimal audit log | PARTIAL: fail-closed Emergency Stop, manual resume confirmation, bounded local metadata log and action receipts added. Post-action state verification remains pending. |
| M4 | Authenticated loopback-only Mira Bridge | CONTRACT DOCUMENTED; pairing, authenticated transport, replay protection and request queue not implemented |
| M5 | TECNO KE7 Calculator smoke test | NOT STARTED |

## Current action boundary

- Every supported action must pass through the internal, non-exported approval screen and receive a separate one-time user approval.
- The manual screen exposes inspect, Back, Swipe up and Swipe down.
- The approval surface also supports typed requests for unique visible text taps, non-password text entry, search submission and chat-message sending.
- Search submission verifies that the exact approved query is still in the named field. Chat sending verifies the exact approved message is still in the named composer and the expected conversation label is visible. The Send action is separate from composing.
- Text entry/submission is blocked if a password field or common authentication-screen label is detected. Passwords, passkeys, OTPs, PINs and recovery codes remain user-entered only.
- A user-managed allowlist can select launchable apps visible to Android's package-visibility query. This is not permission to bypass app-level security or Android restrictions.
- Actions currently report dispatch/acceptance, not verified completion. Do not treat `DISPATCHED` as `VERIFIED`.

## Build evidence

- CI run `43691396188` passed `:app:assembleDebug` on commit `7656305efc9c4bce4ff5a1ef51876e609d0a7831`.
- That run predates the latest user-managed allowlist and exact search/chat submission changes. The latest commit must pass CI separately.
- The first two CI failures were workflow setup errors caused by requesting the obsolete Android SDK package `tools`; the workflow was corrected to install explicit SDK packages.

## Evidence policy

GitHub commits prove source changes only. A CI build proves compilation for that exact commit, not installation, permission behavior, or action correctness on TECNO KE7. Device validation remains separate.

No app installation, Android permission activation, or phone setting change is performed by repository commits.
