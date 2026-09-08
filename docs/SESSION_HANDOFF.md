# Session handoff

Last updated: 2026-09-08

## Active approval state

- The user explicitly accepted S4.4 and closed T-007 on 2026-09-08.
- T-007 is committed as `6bd2682` (`Validate I1 vertical slice`).
- The user explicitly accepted T-008/S1 with “I accept S1, it's done” on 2026-09-08.
- D-021 records the accepted source-neutral sparse product model, committed as `5d28d23` (`Define product domain model`).
- The user explicitly approved the revised T-008/S2 scope and authorized execution on 2026-09-08.
- The user explicitly accepted T-008/S2 with “I accept it, great!” on 2026-09-08; T-008 is closed and no later task is authorized.

## T-008/S2 result

- `docs/DOMAIN_MODEL.md` now defines the accepted anonymous device-owned `PRODUCT_VIEWED` event, optional newest-first bounded `HistoryContext`, server-derived cold-start/sufficient readiness, and explicit data-minimization/privacy gates.
- Recommendation outcomes distinguish non-AI `DETERMINISTIC_FIXTURE`, AI-derived cold-start `GENERIC_SEMANTIC`, and history-applied `PERSONALIZED_HISTORY`, while retaining independent result/empty/unavailable/not-applicable states.
- Ranked AI items require typed finite score evidence and model/pipeline versioning, but scores are response-local and may not be presented as percentages without calibration.
- Factual product data, device observations, embeddings, request-scoped history profiles, and AI ranking evidence are structurally separate. AI-derived data never overwrites product facts.
- `docs/diagrams/domain-model.puml` and `docs/diagrams/ai-enrichment-flow.puml` are canonical sources for English technical, Serbian formal, and compact Serbian presentation renders.
- All four canonical sources pass default, Serbian, and Serbian-presentation syntax checks, and all 12 PNG variants regenerate successfully. The 12-page report builds twice under both pdfLaTeX and LuaLaTeX; the 18-slide presentation builds twice under LuaLaTeX. Final logs are warning-free, and report pages 8–10 plus presentation slides 1, 13, and 14 were visually inspected without clipping.
- S2 changes no Android/backend code, I1 payload, provider, dataset, persistence technology, embedding model, vector store, similarity metric, K/window, or ranking algorithm.

## T-008/S1 result

- `docs/DOMAIN_MODEL.md` defines the accepted provider-neutral product aggregate rather than treating API, fixture, Android, or future persistence shapes as the domain model.
- Stable internal product identity is separate from exact barcode value/format lookup identity. The MVP keeps one package barcode per product record and defers cross-package grouping.
- `id`, barcode, name, and provenance are mandatory. Bounded brand, category, description, and tags are optional so sparse source data remains honest.
- Provenance distinguishes `CONTROLLED_FIXTURE`, `EXTERNAL_PROVIDER`, and `FALLBACK_DATASET`; resolution remains `KNOWN`, `UNKNOWN`, or `UNAVAILABLE`.
- Normalization uses Unicode NFC, trims text, omits blank optional fields, preserves source language, and never fabricates missing metadata.
- The complete I1 fixture maps to the proposal, but its accepted wire contract and all Java/Android code remain unchanged.
- `git diff --check` and documentation path checks pass. The 9-page report builds twice under pdfLaTeX and LuaLaTeX, and the 16-slide presentation builds twice under LuaLaTeX; logs are clean, and the changed report page and product-model slide were visually inspected without clipping.

## S4.4 result

- Clean Android verification passed 32/32 unit tests, lint with zero findings, debug APK assembly, and release-manifest processing.
- Clean backend verification passed 23/23 tests and produced the executable JAR. The running JAR returned the exact primary controlled fixture in a localhost smoke test.
- A fresh debug APK was installed on the authorized Samsung SM-A566B running Android 16/API 36. `adb reverse tcp:8080 tcp:8080` connected device loopback to the local backend.
- Scanning `docs/fixtures/i1-known-product-ean13.svg` displayed barcode `2000000000015`, the known oat product, two ranked almond/soy results, and the exact label “Deterministički demo rezultat — nije AI preporuka”.
- Physical UI-hierarchy checks also confirmed: unknown product with recommendations not applicable; known product with empty recommendations; known product retained when recommendations are unavailable; and backend unavailability with previous outcome content cleared.
- Product-unavailable and malformed-request branches remain covered by automated tests rather than physical scanning, as allowed by the approved representative-case plan.

## Cleanup and reproducibility

- The backend was stopped gracefully; port 8080 was confirmed free.
- ADB reverse forwarding and the phone-side UI hierarchy dump were removed.
- The temporary local barcode-display page was deleted. The debug APK intentionally remains installed.
- No device serial number, UI dump, or temporary barcode file was persisted in the repository.
- Repeatable build, backend-run, ADB-forwarding, installation, and physical-validation notes are in `docs/WORKFLOW.md`.

## Current repository state

- The controlled I1 vertical slice is implemented and physically validated from Google Code Scanner through the Android client and local Spring Boot backend to rendered product/result states.
- Android has 32 passing local tests and zero lint findings; backend has 23 passing tests.
- The I1 data and ranked results remain deterministic local fixtures, not live metadata, semantic similarity, or personalized recommendations.
- External product providers, durable catalog/vector storage, embeddings, semantic ranking, bounded interaction history, and personalization remain unimplemented.

## Next concrete action

T-008 is accepted and closed. Await explicit user direction before selecting or planning the next task; do not implement another task.

## Environment caveat

PlantUML must be invoked with `env -u DISPLAY` in this environment. The physical phone is the preferred runtime target because emulator acceleration remains unavailable without KVM.
