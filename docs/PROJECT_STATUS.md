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
- Direct ML Kit Barcode Scanning with CameraX remains an upgrade path only if the MVP later requires a custom scanner camera experience.

## Verified document builds

- `report/report.tex` compiles with both pdfLaTeX and LuaLaTeX, from the repository root or the `report/` directory.
- `presentation/asap-presentation.tex` compiles with LuaLaTeX, from the repository root or the `presentation/` directory.
- Serbian Latin glyphs render correctly with the engine-aware font setup.
- PlantUML 1.2020.02, Java 21, and Graphviz 2.43.0 are available in the current environment when PlantUML is invoked headlessly with `env -u DISPLAY`.
- The integrated report is 8 pages and the integrated presentation is 11 slides; their diagram pages/slides were visually inspected.

## Development environment audit

Last audited: 2026-09-04 under T-003/S1. No installation or configuration was performed.

- Host: Ubuntu 24.04.4 LTS, x86_64, Linux 7.0.0-30-generic.
- Java: Ubuntu OpenJDK 21.0.12 JDK/JRE and `javac` are installed and selected through system alternatives. This is already new enough for the currently documented Android Gradle Plugin requirement of JDK 17 or later; the future project should still use Android Studio's project-local Gradle JDK convention.
- Missing or undiscovered: Android Studio, standalone Gradle, Android SDK directories, `sdkmanager`, `avdmanager`, `adb`, and `emulator`.
- Environment variables: `JAVA_HOME`, `ANDROID_HOME`, and `ANDROID_SDK_ROOT` are unset. This is not yet treated as a defect because no SDK or Android project exists.
- Runtime capacity: the Ryzen 7 5800H exposes AMD-V, but KVM packages, `/dev/kvm`, and `kvm` group membership are absent. Emulator acceleration is therefore not currently usable from this environment.
- Resources: approximately 13 GiB RAM is visible with about 2.7 GiB available during the audit; approximately 533 GiB disk space is free. Disk capacity is sufficient, while a physical device is preferable to an emulator for the first camera/barcode PoC.
- Official references checked for the audit: [Android Studio installation and current Linux requirements](https://developer.android.com/studio/install), [Java versions in Android builds](https://developer.android.com/build/jdks), and [Linux emulator/KVM acceleration](https://developer.android.com/studio/run/emulator-acceleration).

The proposed next setup keeps OpenJDK 21, installs the current stable official Android Studio distribution and its recommended SDK/platform tools, and defers emulator/KVM work unless a physical device is unavailable. These installation actions require separate T-003/S2 approval.

## Immediate product decisions still open

- Exact Android project structure and minimum supported SDK.
- Backend language/framework and service boundaries.
- Product metadata API and fallback dataset.
- Embedding model and vector-index implementation.
- Concrete MVP acceptance metrics and scope for the approximately 80% functionality target.

See `TODO.md` for ordered tasks and `docs/SESSION_HANDOFF.md` for the suggested next session.
