# Project status

Last verified: 2026-09-05

## Current phase

ASAP is in early Android technical-PoC implementation. A buildable Java/XML Google Code Scanner slice and seven local unit tests exist. Its debug APK is installed on the verified physical phone, where two real-product scans successfully returned decoded values and cancellation produced the intended status. Backend, recommendation service, dataset pipeline, and end-to-end tests do not exist.

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
- The accepted diagram contract now includes the accepted T-006/S2 concrete architecture in `docs/diagrams/component-architecture.puml`, shared styling in `docs/diagrams/includes/theme.puml`, and verified PNG renders. It selects Android plus one backend modular-monolith deployment while marking only the scanner slice implemented.
- The accepted canonical `docs/diagrams/scan-to-recommendation-flow.puml` distinguishes device-owned history, known/unknown/unavailable product outcomes, personalized/generic/empty/unavailable recommendation outcomes, and partial success without prescribing endpoints, schemas, or retry policies.
- English technical, Serbian formal, and compact Serbian presentation renders are generated from the same two canonical PlantUML sources.
- The report embeds the Serbian component and scan-to-recommendation diagrams; the presentation embeds their slide-specific Serbian variants. T-001 is accepted and complete.
- The accepted initial Android baseline is Java application code, XML-based Android Views, and Google Code Scanner. The scanner slice is implemented, locally verified, and physically validated.
- The baseline in `docs/ANDROID_BASELINE.md` is realized through T-005/S2: Android Platform 36, Build Tools 36.0.0, checksum-pinned Gradle Wrapper 9.5.0, AGP 9.3.2, AppCompat 1.8.0, ConstraintLayout 2.2.2, Google Code Scanner 16.1.0, and the JUnit/AndroidX test graph are installed or resolved.
- The Android project lives in `android/` with one `app` module, namespace/application ID `rs.ac.ni.elfak.asap`, a Java `MainActivity`, one custom XML scanner screen, helper logic covered by local tests, and a vector launcher icon.
- The scanner is restricted to EAN-13, EAN-8, UPC-A, and UPC-E with auto-zoom. The UI reports success, empty value, cancellation, module/download unavailability, and general failure; it performs no product lookup.
- The source manifest includes install-time `barcode_ui` metadata and declares no camera permission. The merged scanner dependency adds internet and network-state permissions.
- The debug APK was installed and its launcher activity was verified on the Samsung device. Two successful real-product scans confirm that the scanner module is usable and decoded values return to the app; cancellation also produced the intended user-visible status.
- Physical module/download and general failures were not deliberately induced. Their handlers exist and the failure classification policy is covered by local tests.
- T-005 and all three of its subtasks are accepted and closed.
- T-006/S1 is accepted and defines the durable MVP scope and iteration contract in `docs/MVP_SCOPE.md`: the complete P0 scan-to-similar-products path is the operational 80-point core, history-based recommendation is a committed 15-point extended-MVP milestone, and broader evaluation completes the remaining 5 points. The exact bounded-history method remains open; the S2 proposal builds on this contract.
- T-006 and both subtasks are accepted and closed. One Android application and one backend modular monolith form the MVP topology. The device owns bounded interaction history; the backend owns normalized product and vector data through internal product-resolution and recommendation modules. Product and recommendation outcomes fail independently. No backend implementation exists.
- T-007/S1 is accepted: `docs/BACKEND_BASELINE.md` freezes OpenJDK 21, Spring Boot 4.1.1 with Servlet Spring MVC, Maven 3.9.16 through Maven Wrapper 3.3.3, and one Maven project under `backend/`. The baseline is not implemented and no backend dependency has been downloaded.
- T-007/S2 is accepted. `docs/I1_CONTRACT.md` freezes `POST /api/v1/scan-queries`, exact request validation, independent product/recommendation outcomes, RFC 9457 errors, deterministic-placeholder labelling, and nine acceptance cases. Canonical design fixtures and a scannable restricted-circulation EAN-13 SVG live under `docs/fixtures/`. S3 is authorized to implement this contract; nothing is implemented at the S2 checkpoint.
- T-004 is accepted and complete; the next implementation task must preserve this frozen baseline unless a separately accepted compatibility issue requires a decision revision.
- Direct ML Kit Barcode Scanning with CameraX remains an upgrade path only if the MVP later requires a custom scanner camera experience.

## Verified document builds

- `report/report.tex` compiles with both pdfLaTeX and LuaLaTeX, from the repository root or the `report/` directory.
- `presentation/asap-presentation.tex` compiles with LuaLaTeX, from the repository root or the `presentation/` directory.
- Serbian Latin glyphs render correctly with the engine-aware font setup.
- PlantUML 1.2020.02, Java 21, and Graphviz 2.43.0 are available in the current environment when PlantUML is invoked headlessly with `env -u DISPLAY`.
- The integrated report is 9 pages and the integrated presentation is 15 slides; their updated architecture, flow, scope, iteration, technology, and proposed I1-contract pages/slides were visually inspected.

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

- T-007/S3 fixture-backed backend implementation is active; S4 Android integration is not authorized.
- Scanner UI behavior beyond the accepted T-005/S2 experiment remains open.
- Concrete internal backend package structure is being realized under S3 within the accepted contract and baseline.
- Product metadata API and fallback dataset.
- Embedding model and vector-index implementation.

See `TODO.md` for ordered tasks and `docs/SESSION_HANDOFF.md` for the suggested next session.
