# Operator Capability Matrix

Status: requested capability scope; staged implementation and device validation pending.

The goal is a broad Android operator that can assist with ordinary tasks a phone owner can perform. This is not a promise that every Android app or protected screen is automatable. Android, individual apps, account security, and accessibility exposure impose real limits.

## Capability groups

| Group | Planned support | Approval rule |
|---|---|---|
| Open apps / navigate | Open allowlisted apps, Back | Each requested action gets its own preview |
| Read / inspect | Bounded visible UI-tree inspection | User approves each Mira-requested inspection; sensitive fields excluded |
| Search | Enter a proposed query and submit it | Separate approval for exact query text and submission |
| Write / edit | Fill ordinary non-sensitive fields | Exact field target and exact text shown before each write |
| Chat / messaging | Draft and send messages where app permits | Exact app, recipient/conversation, and full message shown; send requires explicit separate approval |
| Account login | Open login page and guide the user | User personally enters passwords, passkeys, OTPs, recovery codes and PINs; operator never reads or stores them |
| Tap / Swipe / Back | Bounded Accessibility actions | Fresh target/state + one action preview + approval for every action |
| Delete / purchase / payment | Excluded from initial release | No automation in v0.1 |
| Security/account recovery | Excluded from initial release | Human-only |

## Mandatory approval contract

Every action request must be independently approved. No approval carries over to the next action, even when the next action is part of the same task.

The preview must state:
- the exact app/package and current screen context;
- the action type and target element;
- exact text/query/message content when applicable;
- whether it reads or changes data;
- whether the action can be undone.

If the target, app, content, or parameters change after approval, the approval is invalid and a new preview is required. Cancel/deny is always safe. Emergency Stop blocks all queued and future actions.

## Credential handling

The operator must not inspect, copy, transmit, store, or autofill passwords, passkeys, OTPs, payment PINs, recovery codes, or other authentication secrets. The user performs credential entry directly in the app. The operator can wait for the user to finish and then continue only after the user explicitly approves the next action.

## Limits

- Apps may hide their UI tree or block accessibility actions.
- Banking, payment, DRM, secure, and system-protected screens may be inaccessible by design.
- Captchas and anti-bot challenges remain human tasks.
- Android may stop background services; no hidden or persistent control is promised.
- The first target is the user's TECNO KE7 on Android 10, not universal compatibility with all Android versions.
