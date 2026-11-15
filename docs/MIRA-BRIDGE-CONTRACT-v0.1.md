# Mira Bridge Contract v0.1

Status: PROPOSED CONTRACT — no bridge endpoint is implemented yet.

## Purpose

Allow Mira to submit a bounded task to APP-OPERATOR and receive a result, while keeping Android permissions, user approval, and action execution under APP-OPERATOR's control.

Mira is an untrusted task requester, not an authority source. A request from Mira cannot bypass the phone user's allowlist, approval policy, emergency stop, or Android permission state.

## Transport decision

Preferred first transport: same-device loopback only (`127.0.0.1`) between a local Mira bridge process and APP-OPERATOR. Do not bind to `0.0.0.0`, expose a LAN port, or publish an unauthenticated public endpoint.

The exact transport (HTTP loopback, bound local socket, or Android-bound service) remains an implementation decision after checking the actual Mira runtime. No remote Render URL is assumed to be directly reachable as a local Android control channel.

## Request envelope

A future request must contain:

- `protocol_version`: exact supported contract version.
- `request_id`: unique random identifier; duplicates are rejected.
- `created_at` and `expires_at`: short validity window.
- `task_id`: caller's correlation identifier.
- `target_package`: exact Android package name.
- `intent`: short plain-language task description.
- `actions`: bounded typed actions only; never arbitrary shell, JavaScript, code, or URL execution.
- `provenance`: source of the instruction (user, Mira plan, or untrusted app/page content).

The schema must reject unknown action types, oversized strings/lists, expired requests, replayed IDs, and packages outside the user's allowlist.

## Action lifecycle

1. Receive request and validate schema, authentication, expiry, replay status, and target package.
2. Check Emergency Stop and current Android Accessibility permission/service state.
3. Build a concrete preview showing target app, exact action, and relevant parameters.
4. Wait for an explicit user decision. Default is deny; no standing approval in v0.1.
5. Revalidate target package and parameters immediately before execution.
6. Execute only the approved typed action.
7. Verify the resulting UI state where possible.
8. Return an action receipt and terminal status.

If any validation or verification step is inconclusive, stop and return a non-success status.

## Initial action allowlist (planned, not yet implemented)

- `OPEN_APP`: launch an explicitly allowlisted package.
- `INSPECT_UI`: return a bounded, redacted UI-tree summary for an allowlisted package.
- `TAP_NODE`: tap a fresh, uniquely identified, enabled clickable node after approval.
- `BACK`: navigate back after approval.
- `SCROLL`: bounded vertical scroll after approval.

Text entry, form submission, messaging, deletion, purchases, payments, settings changes, and arbitrary coordinates are excluded from the first bridge version.

## Response envelope

A response must include:

- `protocol_version`, `request_id`, `task_id`
- `status`: one of `ACCEPTED`, `AWAITING_USER_APPROVAL`, `REJECTED`, `EXECUTED`, `VERIFIED`, `FAILED`, `INCONCLUSIVE`, `STOPPED`
- `reason_code`: stable machine-readable reason
- `action_receipts`: minimal per-action receipts
- `verification`: observed result or explicit unknown

Never return credentials, password-field content, OTPs, payment data, or raw full-screen dumps.

## Authentication and privacy requirements

- Generate a high-entropy pairing secret locally during an explicit pairing flow; do not hardcode it or commit it.
- Store pairing material using Android Keystore-backed protection where supported.
- Authenticate every request; compare secrets safely and rotate/revoke pairing.
- Add replay protection and strict request expiry.
- Keep transport loopback-only; reject non-loopback peers.
- Do not log authorization headers, pairing secrets, raw UI text, or sensitive action parameters.
- Pairing must be revocable from APP-OPERATOR without requiring Mira to be available.

## Compatibility and versioning

APP-OPERATOR owns the final safety decision. Mira may request; it cannot directly invoke Accessibility APIs. Breaking schema changes require a new protocol version. Unsupported versions fail closed.
