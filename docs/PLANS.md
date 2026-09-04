# Task plans and approval state

This file records the active and recent task plans. The approval protocol is mandatory and defined in `AGENTS.md` and `docs/WORKFLOW.md`. Every subtask plan must name its affected documentation; synchronizing those files is part of that subtask, never deferred to task closure.

## T-001 — Add canonical PlantUML architecture and data-flow sources

- **TODO source:** “Dodati izvorne PlantUML dijagrame arhitekture i tokova podataka.”
- **Status:** Accepted and complete
- **Approval evidence:** The user explicitly authorized “S1” on 2026-09-03.
- **Goal:** Replace the current image-only architectural knowledge with maintainable, version-controlled PlantUML sources without claiming that the proposed architecture has been implemented.

### Proposed subtasks

#### T-001/S1 — Define the diagram contract and file layout

- Specify the diagrams, intended audiences, notation, naming, boundaries, and source/rendered-file layout.
- Reconcile terminology between the DOCX-derived diagram, `docs/ARCHITECTURE.md`, report, and presentation.
- Record unresolved design points as questions rather than silently deciding them.
- Update `docs/PLANS.md` and `docs/SESSION_HANDOFF.md` with the resulting specification and approval state.
- **Evidence:** diagram specification created in `docs/diagrams/README.md`; terminology and architecture references synchronized; documentation checks passed; user responded “I like it” on 2026-09-03.
- **Status:** Accepted.

#### T-001/S2 — Create the component architecture source

- Add shared deterministic presentation-only styling in `docs/diagrams/includes/theme.puml`.
- Add `docs/diagrams/component-architecture.puml` covering the Android client, on-device barcode scanner, backend API, semantic search and recommendation component, product metadata store, and vector index.
- Label the diagram as proposed architecture.
- Generate `docs/diagrams/rendered/component-architecture.png` from the canonical source and visually inspect it for legibility.
- Synchronize `docs/ARCHITECTURE.md`, `docs/PROJECT_STATUS.md`, `docs/PLANS.md`, and `docs/SESSION_HANDOFF.md` with the new source and its still-proposed status.
- Do not create the scan-to-recommendation flow or integrate the component render into the formal report/presentation; those belong to S3 and S4.
- **Evidence:** versioned `includes/theme.puml`, `component-architecture.puml`, and `rendered/component-architecture.png`; successful PlantUML syntax check and render; visual inspection at 892×667; synchronized operational docs.
- **Approval evidence:** The user explicitly instructed “execute S2” on 2026-09-03.
- **Acceptance evidence:** The user explicitly responded “great, accepted” on 2026-09-03.
- **Status:** Accepted.

#### T-001/S3 — Create the end-to-end data-flow source

- Add a PlantUML diagram for the path from camera/barcode acquisition through metadata lookup, semantic retrieval, and recommendation display.
- Show relevant failure/empty-result boundaries only if included in the approved S1 contract.
- Synchronize `docs/ARCHITECTURE.md`, `docs/PROJECT_STATUS.md`, `docs/PLANS.md`, and `docs/SESSION_HANDOFF.md` with the new source and its still-proposed status.
- **Evidence:** versioned `scan-to-recommendation-flow.puml` and 1306×1038 PNG; successful syntax checks and renders for both canonical sources; visual inspection of both outputs; regenerated component PNG after the shared theme extension; synchronized operational docs.
- **Approval evidence:** The user explicitly instructed “yup, execute” for S3 on 2026-09-03.
- **Acceptance evidence:** The user explicitly responded “looks solid” on 2026-09-03.
- **Status:** Accepted.

#### T-001/S4 — Validate and integrate the diagrams

- Render both sources using a documented reproducible command.
- Link or embed appropriate outputs in `docs/ARCHITECTURE.md`, the report, and the presentation without overstating implementation status.
- Synchronize the formal report and presentation, then finalize task-level state in `TODO.md`, `docs/PROJECT_STATUS.md`, `docs/DECISIONS.md` if needed, `docs/PLANS.md`, and `docs/SESSION_HANDOFF.md`. This does not replace the per-subtask documentation updates required in S1–S3.
- **Evidence:** both canonical sources produce English technical, Serbian formal, and compact Serbian presentation renders; all variants pass headless PlantUML checks; the 8-page report builds with pdfLaTeX and LuaLaTeX; the 11-slide presentation builds with LuaLaTeX; integrated pages/slides were visually inspected; operational and formal documentation references are synchronized.
- **Approval evidence:** The user explicitly responded “approved, execute” for S4.
- **Acceptance evidence:** The user explicitly responded “accepted, LGTM!” on 2026-09-04, accepting S4 and closing T-001.
- **Status:** Accepted.

### Task-level acceptance criteria

- Canonical component and end-to-end flow diagrams exist as readable PlantUML source.
- Rendering is reproducible and documented.
- Diagrams agree with the current proposal or explicitly expose unresolved differences.
- Formal and operational documentation consistently labels the architecture as proposed.
- The user explicitly accepts T-001 before any later TODO task is planned or executed.

All acceptance criteria were verified and T-001 was explicitly accepted on 2026-09-04.

### Out of scope

- Choosing Java versus Kotlin.
- Selecting backend frameworks, external APIs, datasets, embedding models, or vector databases.
- Scaffolding application code.
- Treating a diagram element as evidence of implementation.

## T-002 — Select and document the Android MVP technology baseline

- **TODO source:** “Izabrati Java ili Kotlin kao jezik mobilne aplikacije i dokumentovati odluku.”
- **Status:** Accepted and complete
- **Goal:** Establish the lowest-risk Android UI and barcode-scanning direction for a fast MVP without prematurely fixing SDK versions or project structure.

### T-002/S1 — Synchronize the accepted baseline

- Record Java for application code, XML-based Android Views for the custom application UI, and Google Code Scanner for the initial barcode-scanning implementation.
- Preserve direct ML Kit Barcode Scanning with CameraX as an upgrade path if a custom scanning camera experience becomes necessary.
- Update the TODO, decision log, project status, architecture, canonical diagrams and renders, report, presentation, plan, and session handoff.
- Keep exact SDK levels, dependency versions, project structure, and backend technologies unresolved.
- Verify all diagram variants, compile the report with pdfLaTeX and LuaLaTeX, compile the presentation with LuaLaTeX, and check the resulting text and document consistency.
- **Approval evidence:** The user explicitly accepted “Java + XML + Google Code Scanner” and then instructed “execute” on 2026-09-04.
- **Evidence:** accepted baseline recorded as D-010 and synchronized across all named documents; all six diagram variants passed headless PlantUML checks and were regenerated; Serbian component and presentation-flow renders were visually inspected; the 8-page report built with pdfLaTeX and LuaLaTeX; the 11-slide presentation built with LuaLaTeX; updated report and technology slide were visually inspected; logs contain no LaTeX warnings, layout warnings, or missing-character warnings; extracted PDF text preserves Serbian Latin and the accepted technology names.
- **Acceptance evidence:** The user explicitly responded “accepted, cool!” on 2026-09-04, accepting S1 and closing T-002.
- **Status:** Accepted.

### Acceptance criteria

- All documentation distinguishes the accepted initial scanner from the optional custom-scanner upgrade path.
- No document still describes the mobile language or initial scanner technology as undecided.
- No Android source project, SDK level, dependency version, or backend technology is introduced by this documentation-only subtask.
- The user explicitly accepts T-002/S1 before the development-environment task is planned.

All acceptance criteria were verified and T-002 was explicitly accepted on 2026-09-04.

## T-003 — Install and verify the Android development environment

- **TODO source:** “Instalirati i proveriti JDK/JVM, Android Studio i Android SDK.”
- **Status:** Accepted and complete
- **Approval evidence:** The user responded “cool, do it” to the proposed T-003 plan on 2026-09-04. Per the mandatory separate subtask gate, this approves the plan but does not yet authorize S1 execution.
- **Goal:** Establish a known, reproducible Android workstation baseline suitable for the later Java/XML Google Code Scanner PoC, without scaffolding application code or selecting unrelated libraries.

### Proposed subtasks

#### T-003/S1 — Audit the existing toolchain

- Perform a read-only inventory of the operating system, JDK/JVM and compiler, Android Studio, Android SDK locations, command-line tools, platform tools, installed SDK platforms/build tools, emulator/AVD support, and connected devices.
- Distinguish tools that are installed and usable from tools that are missing, present only on `PATH`, or configured inconsistently.
- Produce an exact gap list and proposed installation/configuration actions for S2; do not install, update, accept licenses, launch a GUI, or create an AVD.
- Update `docs/PROJECT_STATUS.md`, `docs/PLANS.md`, and `docs/SESSION_HANDOFF.md` with observed evidence and remaining gaps.
- **Approval evidence:** The user explicitly instructed “execute s1” on 2026-09-04.
- **Evidence:** Ubuntu 24.04.4 LTS x86_64 identified; OpenJDK/JRE/JDK and `javac` 21.0.12 verified through system alternatives; Android Studio, Gradle, Android SDK directories, `sdkmanager`, `avdmanager`, `adb`, and `emulator` were not discoverable through `PATH`, package records, desktop entries, or common installation locations; `JAVA_HOME`, `ANDROID_HOME`, and `ANDROID_SDK_ROOT` are unset; AMD-V is exposed on all 16 logical CPUs, but KVM packages and `/dev/kvm` are absent and the user is not in a `kvm` group; approximately 13 GiB RAM and 533 GiB free disk were observed; no runtime target can be queried without `adb`.
- **Proposed S2 gap actions:** retain the working system OpenJDK 21; install the current stable Android Studio from Google's official Linux distribution; use its setup flow to install the SDK command-line tools, platform tools, and recommended current platform/build tools; record the installed versions and SDK path; defer emulator packages and KVM setup unless the user wants an emulator after the physical-device-first recommendation is reviewed.
- **Runtime recommendation for S3:** prefer a physical Android device with Google Play services because it directly exercises the camera and Google Code Scanner, avoids the currently unavailable KVM path, and fits the host's below-current-minimum visible RAM for Studio plus Emulator. An emulator remains optional after KVM access and memory pressure are reassessed.
- **Acceptance evidence:** The user explicitly responded “approved!” on 2026-09-04.
- **Status:** Accepted.

#### T-003/S2 — Fill approved toolchain gaps

- Install or configure only the missing components identified and accepted after S1: a suitable JDK, Android Studio, Android SDK command-line/platform tools, an Android platform, and matching build tools.
- Use stable official distribution channels and record exact versions and resolved paths. Any network download, privileged package operation, license acceptance, or write outside the repository requires its normal explicit permission.
- Verify the installed commands independently; do not create the ASAP Android project, choose application dependencies, or implement the scanner.
- Update `docs/PROJECT_STATUS.md`, `docs/DECISIONS.md` if a durable version/distribution choice is made, `docs/PLANS.md`, `docs/WORKFLOW.md`, and `docs/SESSION_HANDOFF.md`.
- **Approval evidence:** The user explicitly instructed “execute s2” on 2026-09-04 and explicitly allowed the official downloads, user-scoped installation, SDK license acceptance, and command symlinks through the required permission prompts.
- **Evidence:** Google Android Studio Quail 4 archive size and SHA-256 matched the official publication before extraction; Android Studio 2026.1.4 build `AI-261.26222.65.2614.16204760` installed under `/home/mih/.local/opt/android-studio` and reports its version through `studio --version`; system OpenJDK/`javac` 21.0.12 remained unchanged while Studio's bundled runtime reports OpenJDK 25.0.3; Google command-line tools archive matched its official size and SHA-256, then Command-line Tools were updated to 23.0; Android CLI 1.0.16261425, Platform Tools 37.0.1, Android Platform 37.0 revision 2, and Build Tools 37.0.0 were installed under `/home/mih/Android/Sdk`; `studio`, `android`, `sdkmanager`, `avdmanager`, `adb`, `fastboot` resolve through verified symlinks in `/home/mih/.local/bin`; the new Android CLI lists every installed package with metrics disabled; report and presentation technology sections were synchronized, rebuilt without warnings, and visually inspected; the verified temporary archives were removed after installation; no emulator, AVD, Gradle project, application dependency, or ASAP source was created.
- **Acceptance evidence:** The user explicitly responded “LGTM!” on 2026-09-04.
- **Status:** Accepted.

#### T-003/S3 — Verify a usable Android runtime target

- Establish one verified deployment target using either an existing compatible physical Android device or an approved emulator/AVD with Google Play services.
- Verify `adb` discovery and basic device metadata. Do not install an ASAP application because no Android project exists yet.
- Document the repeatable environment and device checks, known limitations for camera/barcode testing, and any remaining manual Android Studio step.
- Update `docs/PROJECT_STATUS.md`, `docs/PLANS.md`, `docs/WORKFLOW.md`, and `docs/SESSION_HANDOFF.md`; update the report or presentation only if the verified environment changes a formal project claim.
- **Approval evidence:** The user explicitly instructed “execute the s3” after connecting a phone on 2026-09-04.
- **Evidence:** After disabling USB tethering, enabling USB debugging, selecting normal file-transfer mode, and accepting the device-side RSA prompt, `adb devices -l` and `adb get-state` reported an authorized Samsung SM-A566B physical device. It runs Android 16/API 36 on `arm64-v8a`, reports the 2026-04-05 security patch, has enabled Google Play services 26.32.34, and declares rear/front camera, autofocus, and flash features. Repeatable checks were added to `docs/WORKFLOW.md`; the report and presentation were synchronized. No application was installed and no device serial number is persisted in the repository.
- **Acceptance evidence:** The user explicitly responded “lgtm! whats next?” on 2026-09-04, accepting S3 and requesting progression beyond T-003.
- **Status:** Accepted.

### Task-level acceptance criteria

- A suitable JDK/JVM and Java compiler are installed and their versions are documented.
- Android Studio and the Android SDK/tooling are installed, locatable, and versioned.
- Required platform and build-tool packages for the future PoC are present without yet creating the application project.
- At least one Android runtime target is visible through `adb`, or an explicit user-accepted limitation is recorded.
- Reproducible verification commands and environment-specific caveats are documented.
- Each subtask is explicitly approved and accepted before the next begins; T-003 is explicitly accepted before planning the dependency-selection task.

### Out of scope

- Creating Gradle or Android application source files.
- Selecting or adding Google Code Scanner and other application dependency versions.
- Implementing barcode scanning or any ASAP UI.
- Choosing backend, product-data, embedding, or vector-index technologies.

All acceptance criteria were verified and T-003 was explicitly accepted on 2026-09-04.

## T-004 — Select the minimal Android PoC build and dependency baseline

- **TODO source:** “Izdvojiti potrebne biblioteke i njihove verzije.”
- **Status:** Plan approved; T-004/S1 accepted; T-004/S2 awaiting separate execution approval
- **Plan approval evidence:** The user responded “cool, let's go” to the proposed two-subtask plan on 2026-09-04.
- **Goal:** Select an exact, minimal, mutually compatible build and dependency baseline for the Java/XML Google Code Scanner PoC without creating or resolving an Android project.

### Approved subtasks

#### T-004/S1 — Research compatibility and candidates

- Use current primary documentation to determine compatible SDK, AGP, Gradle, JDK, Google Code Scanner, essential AndroidX Views, and minimal test versions.
- Compare stable and newest-toolchain options, record scanner delivery constraints, and exclude unrelated implementation dependencies.
- Produce a recommendation for explicit review; do not install SDK packages, create build files, resolve artifacts, or accept a version decision on the user's behalf.
- Update `docs/ANDROID_BASELINE.md`, the documentation hub, `docs/PROJECT_STATUS.md`, `docs/PLANS.md`, and `docs/SESSION_HANDOFF.md`. Formal deliverables remain unchanged because candidates are not accepted decisions.
- **Approval evidence:** The user explicitly instructed “execute s1” on 2026-09-04.
- **Evidence:** Official sources support the recommended candidate of stable Android 16 `compileSdk`/`targetSdk 36`, `minSdk 23`, Java source/target 17, Gradle runtime JDK 21, patched AGP 9.3.2 with Gradle 9.5.0, Build Tools 36.0.0, Google Code Scanner 16.1.0, AppCompat 1.8.0, ConstraintLayout 2.2.2, JUnit 4.13.2, AndroidX JUnit 1.3.0, and Espresso 3.7.0. The documented alternatives are the just-released AGP 9.4.0/Gradle 9.6.0 pair, preview API 37, optional maintenance-mode Material Views 1.14.0, and deferred direct ML Kit/CameraX.
- **Acceptance evidence:** The user explicitly stated “I accept your proposal” on 2026-09-04.
- **Status:** Accepted.

#### T-004/S2 — Record the accepted baseline

- Resolve the five decision points in `docs/ANDROID_BASELINE.md` from explicit user feedback and freeze exact accepted versions.
- Record the durable choice and compatibility rationale in `docs/DECISIONS.md`, then synchronize TODO, project status, workflow, plan, handoff, report, and presentation wherever the accepted baseline changes a formal claim.
- Do not scaffold a Gradle project, install Platform/Build Tools 36, download dependencies, or implement the scanner.

### Task-level acceptance criteria

- Every required build/runtime/dependency coordinate is exact and mutually compatible.
- Required, optional, deferred, and rejected dependencies are clearly distinguished.
- Dynamic dependency selectors are prohibited and repository requirements are documented.
- The scanner's Google Play services delivery and first-use limitation are explicit.
- S1 and S2 are separately approved and accepted, and T-004 is explicitly accepted before project scaffolding is planned.

### Out of scope

- SDK/package installation or dependency resolution.
- Gradle wrapper and Android project creation.
- Android manifest, resources, Java source, tests, or scanner implementation.
- Backend, product-data, recommendation, or other mobile feature dependencies.
