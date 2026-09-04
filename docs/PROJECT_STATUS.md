# Project status

Last verified: 2026-09-04

## Current phase

ASAP is in proposal and repository-preparation phase. There is no mobile application, backend, recommendation service, dataset pipeline, or automated test suite yet.

## Available artifacts

- A phased product plan in `TODO.md`, transcribed and normalized from `meditations/sept_3.pdf`.
- A Serbian LaTeX report in `report/report.tex`, based on the parent-directory DOCX.
- Report sections 2–5 are structured placeholders and must not be represented as completed work.
- The original architecture image from the DOCX is retained as historical source material at `report/assets/asap-architecture.png`; formal deliverables use canonical PlantUML renders.
- A Serbian ELFak-styled Beamer deck in `presentation/asap-presentation.tex`.
- The presentation contains proposal content and explicitly marked placeholders for design, implementation, and evaluation results.
- Bundled presentation theme assets under `presentation/theme/`.
- Root-level LaTeX ignores and cross-session operating instructions.
- An accepted canonical diagram specification in `docs/diagrams/README.md`.
- An accepted proposed logical component architecture in `docs/diagrams/component-architecture.puml`, shared styling in `docs/diagrams/includes/theme.puml`, and a verified PNG render. It documents intended structure and does not represent implemented software.
- An accepted proposed end-to-end scan-to-recommendation sequence/data-flow diagram in `docs/diagrams/scan-to-recommendation-flow.puml` with a verified PNG render. It includes success and high-level unavailable/empty outcomes without prescribing concrete APIs or retry policies.
- English technical, Serbian formal, and compact Serbian presentation renders are generated from the same two canonical PlantUML sources.
- The report embeds the Serbian component and scan-to-recommendation diagrams; the presentation embeds their slide-specific Serbian variants. T-001 is accepted and complete.
- The accepted initial Android baseline is Java application code, XML-based Android Views, and Google Code Scanner. This is a design decision; no Android project has been created yet.
- The accepted PoC build/dependency baseline is documented in `docs/ANDROID_BASELINE.md`: Android 16/API 36, `minSdk 23`, AGP 9.3.2/Gradle 9.5.0, Java 17 source/target with Gradle on JDK 21, Code Scanner 16.1.0, AppCompat 1.8.0, ConstraintLayout 2.2.2, and the minimal JUnit/AndroidX test set. These versions are selected but not yet installed or resolved as a project.
- T-004 is accepted and complete; the next implementation task must preserve this frozen baseline unless a separately accepted compatibility issue requires a decision revision.
- Direct ML Kit Barcode Scanning with CameraX remains an upgrade path only if the MVP later requires a custom scanner camera experience.

## Verified document builds

- `report/report.tex` compiles with both pdfLaTeX and LuaLaTeX, from the repository root or the `report/` directory.
- `presentation/asap-presentation.tex` compiles with LuaLaTeX, from the repository root or the `presentation/` directory.
- Serbian Latin glyphs render correctly with the engine-aware font setup.
- PlantUML 1.2020.02, Java 21, and Graphviz 2.43.0 are available in the current environment when PlantUML is invoked headlessly with `env -u DISPLAY`.
- The integrated report is 8 pages and the integrated presentation is 11 slides; their diagram pages/slides were visually inspected.

## Android development environment

Last verified: 2026-09-04 under T-003/S3.

- Host: Ubuntu 24.04.4 LTS, x86_64, Linux 7.0.0-30-generic.
- System Java: Ubuntu OpenJDK 21.0.12 JDK/JRE and `javac`, selected through system alternatives and retained unchanged.
- IDE: Android Studio Quail 4 (2026.1.4), build `AI-261.26222.65.2614.16204760`, installed at `/home/mih/.local/opt/android-studio`. Its bundled JetBrains Runtime is OpenJDK 25.0.3.
- SDK root: `/home/mih/Android/Sdk` with Android CLI 1.0.16261425, Command-line Tools 23.0, Platform Tools 37.0.1, Android Platform 37.0 revision 2, and Build Tools 37.0.0.
- User commands: `studio`, `android`, `sdkmanager`, `avdmanager`, `adb`, and `fastboot` resolve through symlinks in `/home/mih/.local/bin`, which is already on `PATH`.
- `JAVA_HOME`, `ANDROID_HOME`, and `ANDROID_SDK_ROOT` remain unset deliberately. The IDE uses its bundled runtime, the SDK occupies Android Studio's default Linux path, and reproducible CLI checks pass the SDK root explicitly.
- No standalone Gradle installation is required before a project exists; the future Android project will own its Gradle Wrapper and project-local Gradle JDK selection.
- Runtime target: an authorized Samsung SM-A566B physical device running Android 16/API 36 on `arm64-v8a`, with the 2026-04-05 security patch. Enabled Google Play services 26.32.34 and rear/front camera, autofocus, and flash capabilities were verified through ADB.
- No emulator, system image, or AVD was added. Physical-device use requires USB debugging, normal file-transfer mode, an unlocked screen during first connection, and acceptance of the host RSA key. USB tethering does not expose the required ADB interface in this setup. No device serial number is recorded.
- T-003 is accepted and complete; actual barcode scanning remains unverified until the separately planned application PoC exists.
- Official references: [Android Studio download and license](https://developer.android.com/studio), [Android Studio installation](https://developer.android.com/studio/install), [Java versions in Android builds](https://developer.android.com/build/jdks), and [Linux emulator/KVM acceleration](https://developer.android.com/studio/run/emulator-acceleration).

## Immediate product decisions still open

- Exact Android project structure remains open.
- Backend language/framework and service boundaries.
- Product metadata API and fallback dataset.
- Embedding model and vector-index implementation.
- Concrete MVP acceptance metrics and scope for the approximately 80% functionality target.

See `TODO.md` for ordered tasks and `docs/SESSION_HANDOFF.md` for the suggested next session.
