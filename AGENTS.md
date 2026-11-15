# Repository Work Rules

- Preserve the existing application structure and user work. Do not delete or overwrite files without inspecting their current contents and documenting why.
- Keep APP-OPERATOR independent from the Mira repository. Mira integration must happen only through a documented, versioned Bridge contract.
- Target TECNO KE7 / Android 10 / API 29 first. Check API availability before using Android framework methods.
- Do not add paid API dependencies, mandatory cloud inference, root, Shizuku, or desktop-only requirements to the basic operator.
- Default to observation-only. Do not enable Accessibility, MediaProjection, overlays, or background execution automatically.
- Never read or store passwords, OTPs, payment PINs, or credential contents.
- No banking/payment automation, financial transfers, account recovery, security-setting changes, irreversible deletion, arbitrary shell execution, or arbitrary code execution.
- Any action beyond observation requires a user-visible preview and explicit approval. Emergency Stop must fail closed.
- Keep logs minimal and redact sensitive UI content. Do not store screenshots or full UI trees by default.
- Do not commit API keys, pairing secrets, keystores, signing passwords, local.properties, or environment files.
- No network downloads, package installs, or device modifications as part of a read-only audit.
- Report build, unit-test, and physical-device validation separately. Never claim PASS without evidence; use INCONCLUSIVE when evidence is unavailable.
