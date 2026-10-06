# Project status

Documentation/report last verified: 2026-10-06. Runtime evidence remains dated September 2026.

Current report: 19 pages, repository link on page 1, D-024/D-025 scope and notebook verification protocol included. Three passes under each report engine succeeded with clean final logs; cover and notebook page visually checked. Local links, six-marker register and whitespace checks pass. See `DOCUMENTATION_AUDIT.md` for all-document dispositions, including intentionally deferred presentation and design renders.

## Current phase

D-024/D-025 require clustering, personalization, PCA, MMR, polished UI and notebook verification for **every** AI/statistical component. See `CURRENT_REQUIREMENTS.md` and `NOTEBOOK_VALIDATION.md`. Google Code Scanner stays temporarily; dataset-trained CNN/TFLite replacement of scanner-related parts comes after the working PoC/MVP. Dataset/model/decoder and algorithm details remain unselected.

Implemented reality is unchanged: Java/XML Android and Spring Boot deterministic I1 fixture flow, historically verified with 32 Android tests, clean lint, 23 backend tests and representative physical-phone cases. There is no live provider, embedding/vector system, genuine semantic retrieval, clustering, history/personalization, PCA, MMR, final polished UI, custom CNN/TFLite scanner or executed AI notebook.

T-010/S1 is accepted as a working draft; T-010/S2–S3 are deferred (D-026). T-009/S1 is accepted. The T-011 AI MVP plan is approved. T-011/S1 design (`AI_MVP_DESIGN.md`, D-027) is accepted with D-028 amendments. S1a translated all app text and fixtures to English; on 2026-10-06 it was reverified with 32 Android tests, 0 lint issues and 23 backend tests. Report is 22 pages after adding the proposed-design subsection.

The report now includes the repository link and current scope/notebook requirements; see `DOCUMENTATION_AUDIT.md` for the latest build evidence. The presentation remains an explicitly deferred earlier snapshot under `presentation/README.md`; diagrams require an approved design extension. Earlier page counts and test records below describe their dated stage, not newly measured results.

T-011/S2 (complete, awaiting acceptance):
- Provider priority and the offline catalog are selected (D-029). `data/processed/catalog.jsonl` holds 10,000 products; it is git-ignored and reproducible from `data/catalog_manifest.json`.
- Executed notebooks: `00_source_probes` (S2), `01_data_embeddings_retrieval` (S3), both accepted, `02_clustering_pca` (S4, accepted; D-031) and `03_personalization_mmr` (S5, awaiting acceptance; D-032: multi-interest β = 0.4, MMR λ = 0.6). Every MVP AI/statistical component now has executed notebook evidence. S6a produced a verified runtime bundle (D-034). S6b ported loading, the ONNX encoder, char-TF-IDF and hybrid retrieval to Java, passing parity tests (D-035; backend tests now 32). S6c exposes the full pipeline as `/api/v2` (V2_CONTRACT.md) when a bundle is configured. Backend tests: 46. Results collapse same name+brand variants (S6c.1, D-036; bundle `20261006-ee94fdb0`). Products outside the 10k catalog are `UNKNOWN` until S6d. The Android app still uses v1 until S7. None is integrated into the backend/app yet (S6–S7).
- S3 adopted hybrid retrieval: e5-small plus char TF-IDF (D-030). Catalog embeddings and the ONNX model are in git-ignored `data/processed/` and reproducible from the notebook.
- The Python ML workspace is `ml/`, with a pinned `ml/requirements.txt` and a local `.venv`.

## Available artifacts

- A phased product plan in `TODO.md`, transcribed and normalized from `meditations/sept_3.pdf`.
- A Serbian LaTeX report in `report/report.tex`, based on the parent-directory DOCX.
- All five report sections have substantive draft content. Target users/benefits are unvalidated assumptions; actual I1 implementation and dated technical evidence are separated from planned AI work. Six visible conditional blocks R01–R06 mark missing implementation, measurements and genuine feedback. `docs/REPORT_COMPLETION.md` maps each to its evidence requirements. The draft is not submission-ready until unresolved blocks are supported or replaced with limitations.
- The original architecture image from the DOCX is retained as historical source material at `report/assets/asap-architecture.png`; formal deliverables use canonical PlantUML renders.
- A Serbian ELFak-styled Beamer deck in `presentation/asap-presentation.tex`.
- The presentation contains proposal content and explicitly marked placeholders for design, implementation, and evaluation results.
- Bundled presentation theme assets under `presentation/theme/`.
- Root-level LaTeX ignores and cross-session operating instructions.
- An accepted canonical diagram specification in `docs/diagrams/README.md`.
- The accepted diagram contract now includes the accepted T-006/S2 concrete architecture in `docs/diagrams/component-architecture.puml`, shared styling in `docs/diagrams/includes/theme.puml`, and verified PNG renders. It selects Android plus one backend modular-monolith deployment and distinguishes the implemented scanner and deterministic backend slice from planned integration/AI components.
- The accepted canonical `docs/diagrams/scan-to-recommendation-flow.puml` distinguishes device-owned history, known/unknown/unavailable product outcomes, personalized/generic/empty/unavailable recommendation outcomes, and partial success without prescribing endpoints, schemas, or retry policies.
- T-008/S2 adds accepted canonical `docs/diagrams/domain-model.puml` and `docs/diagrams/ai-enrichment-flow.puml` views. They show structure/cardinality plus factual-to-AI data lineage, explicitly marking all embeddings, profiles, scores, semantic retrieval, and AI ranking as planned rather than implemented.
- English technical, Serbian formal, and compact Serbian presentation renders are generated from the same four canonical PlantUML sources.
- The report embeds all four Serbian diagram views; the presentation embeds their slide-specific Serbian variants. The T-008/S2 domain and AI-enrichment views are accepted design contracts, while their genuine AI elements remain planned.
- The accepted initial Android baseline is Java application code, XML-based Android Views, and Google Code Scanner. The scanner slice is implemented, locally verified, and physically validated.
- The baseline in `docs/ANDROID_BASELINE.md` is realized through T-005/S2: Android Platform 36, Build Tools 36.0.0, checksum-pinned Gradle Wrapper 9.5.0, AGP 9.3.2, AppCompat 1.8.0, ConstraintLayout 2.2.2, Google Code Scanner 16.1.0, and the JUnit/AndroidX test graph are installed or resolved.
- The Android project lives in `android/` with one `app` module, namespace/application ID `rs.ac.ni.elfak.asap`, a Java `MainActivity`, one custom XML scanner screen, an isolated `network` package, helper logic covered by local tests, and a vector launcher icon.
- The scanner is restricted to EAN-13, EAN-8, UPC-A, and UPC-E with auto-zoom. The UI reports empty value, cancellation, module/download unavailability, and general failure locally; a supported success enters the implemented I1 backend flow.
- The source manifest includes install-time `barcode_ui` metadata and declares no camera permission. The merged scanner dependency adds internet and network-state permissions.
- The debug APK was installed and its launcher activity was verified on the Samsung device. Two successful real-product scans confirm that the scanner module is usable and decoded values return to the app; cancellation also produced the intended user-visible status.
- Physical module/download and general failures were not deliberately induced. Their handlers exist and the failure classification policy is covered by local tests.
- T-005 and all three of its subtasks are accepted and closed.
- T-006/S1 is historically accepted and defined the now-superseded 80/95-point scope and original iteration contract in `docs/MVP_SCOPE.md`: the complete P0 scan-to-similar-products path is the operational 80-point core, history-based recommendation is a committed 15-point extended-MVP milestone, and broader evaluation completes the remaining 5 points. The exact bounded-history method remains open; the accepted T-008/S2 model builds on this contract.
- T-006 and both subtasks are accepted and closed. One Android application and one backend modular monolith form the MVP topology. The device owns bounded interaction history; the backend owns normalized product and vector data through internal product-resolution and recommendation modules. Product and recommendation outcomes fail independently.
- T-007/S1 is accepted: `docs/BACKEND_BASELINE.md` freezes OpenJDK 21, Spring Boot 4.1.1 with Servlet Spring MVC, Maven 3.9.16 through Maven Wrapper 3.3.3, and one Maven project under `backend/`. S3 realizes this exact baseline.
- T-007/S2 is accepted. `docs/I1_CONTRACT.md` freezes `POST /api/v1/scan-queries`, exact request validation, independent product/recommendation outcomes, RFC 9457 errors, deterministic-placeholder labelling, and nine acceptance cases. Canonical fixtures and a scannable restricted-circulation EAN-13 SVG live under `docs/fixtures/`.
- T-007/S3 is accepted. `backend/` packages the canonical fixture and exposes API, coordination, product-resolution, recommendation, and barcode-validation boundaries. `./mvnw verify` passes 23 tests (11 barcode rules and 12 real-HTTP contract tests); the executable JAR starts and returns the expected primary fixture response. It contains no Android client, external provider, embeddings, vector storage, or genuine recommendation logic.
- T-007/S4.1 is accepted. The Android-side `ScanQueryClient` boundary uses Retrofit 3.0.0, converter-moshi 3.0.0, resolved Moshi 1.15.2, and API-36-compatible OkHttp 5.3.2. It serializes the frozen request, validates all response unions, distinguishes transport/HTTP/invalid-response failures, maps the four scanner formats, and returns a cancellable call handle. Debug builds target device loopback and permit cleartext for `adb reverse`; release builds deny cleartext and use a non-routable placeholder URL.
- T-007/S4.2 is accepted. `MainActivity` delegates supported successful scan values to a pure-Java `ScanQueryCoordinator`, which invokes `ScanQueryClient`, posts callback state through the Android main-thread executor, exposes loading and separated transport/HTTP/invalid-response states, cancels active work on a newer scan or destruction, and rejects stale callbacks. Empty, unsupported, cancelled, module-unavailable, and scanner-error paths do not submit a request. S4.3 builds outcome rendering on this boundary; no physical end-to-end claim is made before S4.4.
- T-007/S4.3 is accepted. The scrollable custom XML screen renders normalized known-product details, distinct unknown/unavailable product states, and independent `RESULTS`, `EMPTY`, `UNAVAILABLE`, and `NOT_APPLICABLE` recommendation states. Every placeholder outcome shows “Deterministic demo result — not an AI recommendation” (Serbian “Deterministički demo rezultat — nije AI preporuka” during the September physical validation; app text is English since D-028); result rows contain rank and product summary only, with no fabricated score. Five outcome-model tests verify all frozen combinations and preservation of known product data when recommendations are empty or unavailable.
- T-007/S4.4 and task T-007 are accepted and complete. A clean build produced 32/32 passing Android tests, zero lint findings, a debug APK, and 23/23 passing backend tests. On the verified phone, the fresh APK used `adb reverse` to reach the local backend and physically displayed: the primary known product with almond/soy ranks and the exact demo-only label; unknown product with `NOT_APPLICABLE`; known product with empty recommendations; known product with unavailable recommendations while retaining product data; and backend unavailability with prior outcome content cleared. The backend was stopped gracefully, forwarding and temporary validation artifacts were removed, and the APK remains installed.
- T-008/S1 is accepted. `docs/DOMAIN_MODEL.md` defines a source-neutral product aggregate with stable internal identity, exact barcode lookup identity, minimal required metadata, bounded optional metadata, explicit provenance, normalization rules, and independent resolution outcomes. It maps the complete I1 fixtures without changing code or the frozen wire contract.
- T-008/S2 and task T-008 are accepted and complete. The contract defines one anonymous device-owned `PRODUCT_VIEWED` event, an optional newest-first bounded history context, derived cold-start/sufficient readiness, deterministic/generic-semantic/personalized recommendation modes, independent result states, ranked-item evidence constraints, and a hard boundary preventing AI-derived artifacts from overwriting product facts. It changes no application code or I1 payload.
- T-009's plan and S1 were explicitly approved. `docs/PRODUCT_DATA_API_EVALUATION.md` defines model-derived gates, field mappings, the official-documentation shortlist (Open Food Facts, Barcode Lookup, and UPCitemdb), a 12-product EAN-8/UPC-E/EAN-13/UPC-A corpus, and a bounded S2 observation protocol. Open Food Facts and UPCitemdb are eligible for controlled S2 reads after separate approval; Barcode Lookup remains documentation-only without separately authorized compatible access. No provider has been selected or queried.
- T-004 is accepted and complete; the next implementation task must preserve this frozen baseline unless a separately accepted compatibility issue requires a decision revision.
- D-024 replaces the earlier optional ML Kit/CameraX upgrade direction with a required trained CNN/TFLite scanner after the PoC/MVP; camera/decoder details remain open.

## Verified document builds

- `report/report.tex` compiles with both pdfLaTeX and LuaLaTeX, from the repository root or the `report/` directory.
- `presentation/asap-presentation.tex` compiles with LuaLaTeX, from the repository root or the `presentation/` directory.
- Serbian Latin glyphs render correctly with the engine-aware font setup.
- PlantUML 1.2020.02, Java 21, and Graphviz 2.43.0 are available in the current environment when PlantUML is invoked headlessly with `env -u DISPLAY`.
- T-010/S1 expands the report to 17 pages; repeated pdfLaTeX and LuaLaTeX builds pass with clean final logs. Cover and pages 13, 14, 16 and 17 were visually inspected, including all six marker blocks and Serbian Latin glyphs. The report deliverable copy is synchronized with `build/report.pdf`. The unchanged 19-slide presentation retains its earlier T-009/S1 verification; it is intentionally deferred until the end, not newly verified in October.

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

- T-009/S1 is accepted; its S2 probes are folded into T-011/S2 and not yet authorized.
- Scanner UI behavior beyond the accepted T-005/S2 experiment remains open.
- Any post-I1 evolution of the implemented backend package boundaries.
- Primary product metadata API (T-009/S3) and fallback dataset.
- Embedding model and vector-index implementation.

See `TODO.md` for ordered tasks and `docs/SESSION_HANDOFF.md` for the suggested next session.
