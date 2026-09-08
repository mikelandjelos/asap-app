# Project status

Last verified: 2026-09-08

## Current phase

ASAP is in early vertical-slice implementation. The Java/XML Android app has a physically validated Google Code Scanner-to-Retrofit/Moshi I1 flow and UI for every frozen product/recommendation outcome; all 32 local tests and lint pass. The Java 21/Spring Boot backend implements the deterministic I1 contract with 23 passing tests. The controlled end-to-end vertical slice is physically validated. T-008/S1 defines the accepted provider-neutral product contract for the next iteration; live product data, semantic search, persistence, and personalized recommendations do not exist.

## Available artifacts

- A phased product plan in `TODO.md`, transcribed and normalized from `meditations/sept_3.pdf`.
- A Serbian LaTeX report in `report/report.tex`, based on the parent-directory DOCX.
- Report sections 2–5 remain incomplete. T-006/S1 populates the detailed-solution/MVP-boundary material, while target users, competitive analysis, data selection, feedback, corrections, and final evaluation remain placeholders.
- The original architecture image from the DOCX is retained as historical source material at `report/assets/asap-architecture.png`; formal deliverables use canonical PlantUML renders.
- A Serbian ELFak-styled Beamer deck in `presentation/asap-presentation.tex`.
- The presentation contains proposal content and explicitly marked placeholders for design, implementation, and evaluation results.
- Bundled presentation theme assets under `presentation/theme/`.
- Root-level LaTeX ignores and cross-session operating instructions.
- An accepted canonical diagram specification in `docs/diagrams/README.md`.
- The accepted diagram contract now includes the accepted T-006/S2 concrete architecture in `docs/diagrams/component-architecture.puml`, shared styling in `docs/diagrams/includes/theme.puml`, and verified PNG renders. It selects Android plus one backend modular-monolith deployment and distinguishes the implemented scanner and deterministic backend slice from planned integration/AI components.
- The accepted canonical `docs/diagrams/scan-to-recommendation-flow.puml` distinguishes device-owned history, known/unknown/unavailable product outcomes, personalized/generic/empty/unavailable recommendation outcomes, and partial success without prescribing endpoints, schemas, or retry policies.
- English technical, Serbian formal, and compact Serbian presentation renders are generated from the same two canonical PlantUML sources.
- The report embeds the Serbian component and scan-to-recommendation diagrams; the presentation embeds their slide-specific Serbian variants. T-001 is accepted and complete.
- The accepted initial Android baseline is Java application code, XML-based Android Views, and Google Code Scanner. The scanner slice is implemented, locally verified, and physically validated.
- The baseline in `docs/ANDROID_BASELINE.md` is realized through T-005/S2: Android Platform 36, Build Tools 36.0.0, checksum-pinned Gradle Wrapper 9.5.0, AGP 9.3.2, AppCompat 1.8.0, ConstraintLayout 2.2.2, Google Code Scanner 16.1.0, and the JUnit/AndroidX test graph are installed or resolved.
- The Android project lives in `android/` with one `app` module, namespace/application ID `rs.ac.ni.elfak.asap`, a Java `MainActivity`, one custom XML scanner screen, an isolated `network` package, helper logic covered by local tests, and a vector launcher icon.
- The scanner is restricted to EAN-13, EAN-8, UPC-A, and UPC-E with auto-zoom. The UI reports empty value, cancellation, module/download unavailability, and general failure locally; a supported success enters the implemented I1 backend flow.
- The source manifest includes install-time `barcode_ui` metadata and declares no camera permission. The merged scanner dependency adds internet and network-state permissions.
- The debug APK was installed and its launcher activity was verified on the Samsung device. Two successful real-product scans confirm that the scanner module is usable and decoded values return to the app; cancellation also produced the intended user-visible status.
- Physical module/download and general failures were not deliberately induced. Their handlers exist and the failure classification policy is covered by local tests.
- T-005 and all three of its subtasks are accepted and closed.
- T-006/S1 is accepted and defines the durable MVP scope and iteration contract in `docs/MVP_SCOPE.md`: the complete P0 scan-to-similar-products path is the operational 80-point core, history-based recommendation is a committed 15-point extended-MVP milestone, and broader evaluation completes the remaining 5 points. The exact bounded-history method remains open; the S2 proposal builds on this contract.
- T-006 and both subtasks are accepted and closed. One Android application and one backend modular monolith form the MVP topology. The device owns bounded interaction history; the backend owns normalized product and vector data through internal product-resolution and recommendation modules. Product and recommendation outcomes fail independently.
- T-007/S1 is accepted: `docs/BACKEND_BASELINE.md` freezes OpenJDK 21, Spring Boot 4.1.1 with Servlet Spring MVC, Maven 3.9.16 through Maven Wrapper 3.3.3, and one Maven project under `backend/`. S3 realizes this exact baseline.
- T-007/S2 is accepted. `docs/I1_CONTRACT.md` freezes `POST /api/v1/scan-queries`, exact request validation, independent product/recommendation outcomes, RFC 9457 errors, deterministic-placeholder labelling, and nine acceptance cases. Canonical fixtures and a scannable restricted-circulation EAN-13 SVG live under `docs/fixtures/`.
- T-007/S3 is accepted. `backend/` packages the canonical fixture and exposes API, coordination, product-resolution, recommendation, and barcode-validation boundaries. `./mvnw verify` passes 23 tests (11 barcode rules and 12 real-HTTP contract tests); the executable JAR starts and returns the expected primary fixture response. It contains no Android client, external provider, embeddings, vector storage, or genuine recommendation logic.
- T-007/S4.1 is accepted. The Android-side `ScanQueryClient` boundary uses Retrofit 3.0.0, converter-moshi 3.0.0, resolved Moshi 1.15.2, and API-36-compatible OkHttp 5.3.2. It serializes the frozen request, validates all response unions, distinguishes transport/HTTP/invalid-response failures, maps the four scanner formats, and returns a cancellable call handle. Debug builds target device loopback and permit cleartext for `adb reverse`; release builds deny cleartext and use a non-routable placeholder URL.
- T-007/S4.2 is accepted. `MainActivity` delegates supported successful scan values to a pure-Java `ScanQueryCoordinator`, which invokes `ScanQueryClient`, posts callback state through the Android main-thread executor, exposes loading and separated transport/HTTP/invalid-response states, cancels active work on a newer scan or destruction, and rejects stale callbacks. Empty, unsupported, cancelled, module-unavailable, and scanner-error paths do not submit a request. S4.3 builds outcome rendering on this boundary; no physical end-to-end claim is made before S4.4.
- T-007/S4.3 is accepted. The scrollable custom XML screen renders normalized known-product details, distinct unknown/unavailable product states, and independent `RESULTS`, `EMPTY`, `UNAVAILABLE`, and `NOT_APPLICABLE` recommendation states. Every placeholder outcome shows “Deterministički demo rezultat — nije AI preporuka”; result rows contain rank and product summary only, with no fabricated score. Five outcome-model tests verify all frozen combinations and preservation of known product data when recommendations are empty or unavailable.
- T-007/S4.4 and task T-007 are accepted and complete. A clean build produced 32/32 passing Android tests, zero lint findings, a debug APK, and 23/23 passing backend tests. On the verified phone, the fresh APK used `adb reverse` to reach the local backend and physically displayed: the primary known product with almond/soy ranks and the exact demo-only label; unknown product with `NOT_APPLICABLE`; known product with empty recommendations; known product with unavailable recommendations while retaining product data; and backend unavailability with prior outcome content cleared. The backend was stopped gracefully, forwarding and temporary validation artifacts were removed, and the APK remains installed.
- T-008/S1 is accepted. `docs/DOMAIN_MODEL.md` defines a source-neutral product aggregate with stable internal identity, exact barcode lookup identity, minimal required metadata, bounded optional metadata, explicit provenance, normalization rules, and independent resolution outcomes. It maps the complete I1 fixtures without changing code or the frozen wire contract; interaction/recommendation contracts and two canonical model/AI-lineage diagrams remain S2.
- T-004 is accepted and complete; the next implementation task must preserve this frozen baseline unless a separately accepted compatibility issue requires a decision revision.
- Direct ML Kit Barcode Scanning with CameraX remains an upgrade path only if the MVP later requires a custom scanner camera experience.

## Verified document builds

- `report/report.tex` compiles with both pdfLaTeX and LuaLaTeX, from the repository root or the `report/` directory.
- `presentation/asap-presentation.tex` compiles with LuaLaTeX, from the repository root or the `presentation/` directory.
- Serbian Latin glyphs render correctly with the engine-aware font setup.
- PlantUML 1.2020.02, Java 21, and Graphviz 2.43.0 are available in the current environment when PlantUML is invoked headlessly with `env -u DISPLAY`.
- The integrated report is 9 pages and the integrated presentation is 16 slides; their architecture, flow, scope, iteration, technology, I1-contract, and accepted T-008/S1 product-model pages/slides were visually inspected.

## Android development environment

Last verified: 2026-09-05 under T-005/S3.

- Host: Ubuntu 24.04.4 LTS, x86_64, Linux 7.0.0-30-generic.
- System Java: Ubuntu OpenJDK 21.0.12 JDK/JRE and `javac`, selected through system alternatives and retained unchanged.
- IDE: Android Studio Quail 4 (2026.1.4), build `AI-261.26222.65.2614.16204760`, installed at `/home/mih/.local/opt/android-studio`. Its bundled JetBrains Runtime is OpenJDK 25.0.3.
- SDK root: `/home/mih/Android/Sdk` with Android CLI 1.0.16261425, Command-line Tools 23.0, Platform Tools 37.0.1, Android Platforms 36 revision 2 and 37.0 revision 2, and Build Tools 36.0.0 and 37.0.0.
- User commands: `studio`, `android`, `sdkmanager`, `avdmanager`, `adb`, and `fastboot` resolve through symlinks in `/home/mih/.local/bin`, which is already on `PATH`.
- `JAVA_HOME`, `ANDROID_HOME`, and `ANDROID_SDK_ROOT` remain unset deliberately. The IDE uses its bundled runtime, the SDK occupies Android Studio's default Linux path, and reproducible CLI checks pass the SDK root explicitly.
- No standalone Gradle installation is used. The project owns a checksum-pinned Gradle 9.5.0 Wrapper and builds through the system OpenJDK 21 runtime with Java 17 source/target compatibility.
- Runtime target: an authorized Samsung SM-A566B physical device running Android 16/API 36 on `arm64-v8a`, with the 2026-04-05 security patch. Enabled Google Play services 26.32.34 and rear/front camera, autofocus, and flash capabilities were verified through ADB.
- No emulator, system image, or AVD was added. Physical-device use requires USB debugging, normal file-transfer mode, an unlocked screen during first connection, and acceptance of the host RSA key. USB tethering does not expose the required ADB interface in this setup. No device serial number is recorded.
- T-003 and T-005 are accepted and complete; actual barcode scanning and cancellation are verified on the physical device.
- Official references: [Android Studio download and license](https://developer.android.com/studio), [Android Studio installation](https://developer.android.com/studio/install), [Java versions in Android builds](https://developer.android.com/build/jdks), and [Linux emulator/KVM acceleration](https://developer.android.com/studio/run/emulator-acceleration).

## Immediate product decisions still open

- T-008/S1 is accepted. The expanded S2 plan includes canonical domain-class and AI-enrichment diagrams and awaits explicit approval.
- Scanner UI behavior beyond the accepted T-005/S2 experiment remains open.
- Any post-I1 evolution of the implemented backend package boundaries.
- Product metadata API and fallback dataset.
- Embedding model and vector-index implementation.

See `TODO.md` for ordered tasks and `docs/SESSION_HANDOFF.md` for the suggested next session.
