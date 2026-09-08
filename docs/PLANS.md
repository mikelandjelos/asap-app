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
- **Status:** Accepted and complete
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
- **Approval evidence:** The user explicitly instructed “execute s2” on 2026-09-04.
- **Evidence:** The five accepted S1 recommendations were frozen in `docs/ANDROID_BASELINE.md` and recorded as D-012. Operational status/workflow, README, report, presentation, plan, and handoff were synchronized. The formal PDFs were rebuilt and inspected. No SDK package, Gradle distribution, Maven artifact, project, manifest, resource, source file, or test was created or downloaded.
- **Acceptance evidence:** The user explicitly instructed “good, commit; what's next” on 2026-09-04.
- **Status:** Accepted.

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

All acceptance criteria were verified and T-004 was explicitly accepted on 2026-09-04.

## T-005 — Build and validate the Google Code Scanner technical PoC

- **TODO sources:** “Napraviti mali tehnički eksperiment sa Google Code Scanner API-jem iz ML Kit ponude” and “Definisati ponovljiv lokalni postupak za pokretanje, testiranje i izgradnju projekta.”
- **Status:** Accepted and closed
- **Plan approval evidence:** The user instructed “execute s1” after receiving the complete three-subtask plan on 2026-09-04; this explicitly authorizes S1 and accepts its stated structure.
- **Goal:** Establish, implement, and physically validate the smallest maintainable Java/XML Google Code Scanner experiment before any backend or recommendation work.

### Approved subtasks

#### T-005/S1 — Scaffold the Android client shell

- Install only Android Platform 36 and Build Tools 36.0.0 from the official SDK catalog.
- Create a single-module Groovy Gradle project in `android/` using namespace/application ID `rs.ac.ni.elfak.asap`, the accepted wrapper/toolchain, Java 17, XML Views, AppCompat, ConstraintLayout, and test baseline.
- Provide a launcher shell and placeholder state, but do not add the scanner dependency, scanner metadata, camera permission, or phone installation.
- Resolve dependencies; verify wrapper checksums, build, unit tests, lint, APK metadata, permissions, and absence of scanner dependencies.
- Synchronize README, architecture, decisions, status, workflow, plan, handoff, report, and presentation.
- **Approval evidence:** The user explicitly instructed “execute s1” on 2026-09-04.
- **Evidence:** Official `platforms/android-36` revision 2 and `build-tools/36.0.0` were installed. The official Gradle 9.5.0 archive matched published SHA-256 `553c78f50dafcd54d65b9a444649057857469edf836431389695608536d6b746`; its generated wrapper JAR matched published SHA-256 `497c8c2a7e5031f6aa847f88104aa80a93532ec32ee17bdb8d1d2f67a194a9c7`, and the temporary archive/extraction were removed. The wrapper resolved AGP 9.3.2 and the accepted AppCompat/ConstraintLayout/test graph. `testDebugUnitTest`, `lintDebug`, and `assembleDebug` pass; lint reports zero findings and the single JUnit test passes. APK inspection reports application ID `rs.ac.ni.elfak.asap`, min SDK 23, target SDK 36, and no camera permission. No scanner artifact is in the debug runtime graph, and no APK was installed.
- **Acceptance evidence:** The user explicitly responded “accepted! whats next?” on 2026-09-04.
- **Status:** Accepted.

#### T-005/S2 — Implement the scanner slice

- Add Google Code Scanner 16.1.0 and install-time `barcode_ui` module metadata without adding camera permission.
- Replace the placeholder with a custom XML screen containing scan action, status, and decoded result.
- Configure EAN-13, EAN-8, UPC-A, and UPC-E; handle success, cancellation, module/download unavailability, and general failure without product lookup.
- Add proportional unit tests, run build/lint/tests, and synchronize all affected documentation. Do not install the APK on the phone.
- **Approval evidence:** The user explicitly instructed “execute s2” on 2026-09-04 and instructed “continue” after the interrupted verification on 2026-09-05.
- **Evidence:** The app resolves `play-services-code-scanner:16.1.0`; its merged manifest contains `com.google.mlkit.vision.DEPENDENCIES=barcode_ui`, min SDK 23, target SDK 36, and no camera permission. The custom Java/XML screen launches an auto-zoom scanner restricted to EAN-13, EAN-8, UPC-A, and UPC-E and presents success, empty value, cancellation, module/download unavailability, and general failure without product lookup. Seven local JUnit tests pass, lint reports zero findings, and debug assembly succeeds. The APK was not installed or run.
- **Acceptance evidence:** The user explicitly responded “I accept it” on 2026-09-05.
- **Status:** Accepted.

#### T-005/S3 — Validate on the physical phone

- Install and launch the debug APK on the verified Samsung device only after explicit approval.
- With user participation, verify scanner-module delivery, a real product barcode, cancellation, and feasible failure behavior.
- Record reproducible build/install/run commands and actual results; synchronize formal deliverables and stop for acceptance.
- **Approval evidence:** After accepting S2, the user explicitly instructed “you can execute s3” on 2026-09-05.
- **Evidence:** ADB reauthorization succeeded for the previously verified Samsung SM-A566B running Android 16/API 36 with Google Play services 26.32.34. `adb install -r` installed version code 1 successfully, the launcher was started, and `MainActivity` was confirmed as the top resumed activity. The user reported two successful real-product scans with decoded values returned to ASAP and confirmed that closing the scanner produced the intended cancellation status. This confirms that the scanner module is available and usable; whether Google Play services downloaded it during this run or it was already present is not observable. Module/download and general failure branches are implemented and unit-tested, but destructive or environment-altering failure injection was not justified. No device identifier or barcode value is recorded.
- **Acceptance evidence:** The user explicitly responded “I explicitly accept s3!” on 2026-09-05.
- **Status:** Accepted.

### Task-level acceptance criteria

- The committed wrapper reproduces a warning-free build, test, lint, and APK assembly from the repository.
- The custom shell launches Google Code Scanner without an application camera permission and reports user-visible outcomes.
- A real barcode scan is demonstrated on the verified physical device, with limitations recorded honestly.
- Documentation and formal deliverables distinguish implemented evidence from planned backend/recommendation behavior.
- Every subtask and T-005 itself is separately accepted before proceeding.

### Out of scope

- Product metadata lookup, network/API clients, persistence, backend, embeddings, or recommendations.
- Production UI polish, authentication, analytics, publication, signing, or release distribution.
- CameraX and direct ML Kit Barcode Scanning.

All acceptance criteria were verified and T-005 was explicitly accepted and closed by the user on 2026-09-05.

## T-006 — Define the MVP boundary and concrete system architecture

- **TODO sources:** “Precizirati arhitekturu mobilne aplikacije, API servisa, servisa preporuka i skladišta podataka” and “Definisati granice MVP-a i plan implementacije po iteracijama.”
- **Status:** Accepted and closed
- **Plan approval evidence:** The user responded “let's go - do the s1” after reviewing the two-subtask proposal on 2026-09-05; this approves the recorded plan and explicitly authorizes S1 only.
- **Goal:** Freeze a small, testable MVP contract and the logical/deployment boundaries needed to implement it without prematurely selecting product-data providers, backend libraries, embedding models, or storage products.

### Proposed subtasks

#### T-006/S1 — Define the MVP scope and iteration contract

- Reconcile the initial proposal with the validated scanner PoC and list the minimum user-visible journey, required capabilities, explicit non-goals, assumptions, and external dependencies.
- Divide delivery into thin end-to-end iterations with concrete entry/exit evidence, keeping the fastest useful MVP and the approximate 80% project target distinct.
- Resolve scope ambiguities in review with the user; do not choose backend frameworks, external product APIs, datasets, embedding models, or database products.
- Synchronize TODO, plan, project status, architecture narrative, handoff, report, and presentation.
- **Approval evidence:** The user explicitly instructed “do the s1” on 2026-09-05.
- **Evidence:** `docs/MVP_SCOPE.md` defines the minimum scan-to-similar-products journey, a non-interchangeable 80-point P0 core, a committed 15-point extended MVP based on bounded interaction history, and 5 points of broader evaluation. It records required behavior, non-goals, assumptions, external dependencies, six thin iterations, exit evidence, and change control while leaving the exact history window and ranking method open. Operational and formal documents distinguish the validated scanner foundation from all planned downstream behavior. No code, dependency, provider, model, framework, schema, or storage product was introduced.
- **Acceptance evidence:** The user explicitly responded “accepted - commit and start s2” on 2026-09-05.
- **Status:** Accepted.

#### T-006/S2 — Define and visualize the concrete architecture contract

- **Approval evidence:** After accepting S1, the user explicitly instructed “start s2” on 2026-09-05.
- Specify mobile, backend API, product-metadata adapter/store, semantic recommendation component, and vector-index responsibilities; define deployment boundaries, ownership, major data exchanges, and failure boundaries.
- Decide the MVP topology at the architecture level, including whether recommendation logic is part of one backend deployment or a separately deployed service, without selecting implementation libraries or vendor products reserved for later tasks.
- Update both canonical PlantUML diagrams and every localized render so implemented scanner behavior and planned downstream behavior remain visibly distinct.
- Synchronize TODO, decisions, architecture, status, plan, handoff, report, and presentation; validate all diagram variants and compile/visually inspect both formal deliverables.
- **Evidence:** `docs/ARCHITECTURE.md` defines one Android application plus one backend modular-monolith deployment, module and data ownership, conceptual request/outcome contracts, trust boundaries, and user-visible failure responsibility. Bounded history is device-owned request context; the backend retains no user profile. Both canonical PlantUML sources and all English/Serbian/presentation renders show the selected topology and distinguish the implemented scanner slice from planned components. No framework, vendor, provider, endpoint schema, model, storage product, backend scaffold, or Android behavior was introduced.
- **Acceptance evidence:** The user explicitly responded “I accept this” on 2026-09-05.
- **Status:** Accepted.

### Approved task-level acceptance criteria

- The MVP has an explicit included/deferred/out-of-scope contract and ordered thin delivery iterations.
- Every planned component has one clear responsibility, owner of persistent data, primary inputs/outputs, and user-visible failure responsibility.
- Canonical diagrams, technical documentation, report, and presentation agree and distinguish the validated scanner slice from planned downstream components.
- No application/backend scaffold, dependency, API provider, dataset, model, or storage product is introduced.
- The plan and each subtask are separately approved and accepted before advancing.

### Approved exclusions

- Creating the backend project or changing Android implementation.
- Selecting concrete product-data APIs or fallback datasets.
- Selecting backend framework/library versions, embedding models, vector databases, or cloud vendors.
- Defining final endpoint schemas, authentication, production deployment, or operational scaling.

All task-level acceptance criteria were verified and the user explicitly accepted and closed T-006 with “I accept you can commit” on 2026-09-05.

## T-007 — Deliver the deterministic I1 vertical slice

- **TODO sources:** “Postaviti početne projekte za mobilnu aplikaciju i backend,” “Definisati modele proizvoda, korisničke interakcije i preporuke,” and the accepted I1 iteration in `docs/MVP_SCOPE.md`.
- **Status:** Accepted and complete
- **Goal:** Demonstrate the accepted Android-to-backend boundary with controlled fixture data before introducing external product providers, embeddings, vector storage, or semantic-result claims.

The user explicitly approved the four-subtask plan and authorized only T-007/S1 with “continue with T-007:S1” on 2026-09-05.

### Approved subtasks

#### T-007/S1 — Select the backend technical baseline

- **Status:** Accepted

- Compare a small set of current backend candidates against the accepted modular-monolith boundary, local development constraints, Java familiarity, testability, Android integration, and fastest-MVP goal.
- Recommend exact runtime, framework/build-tool baseline, project location, and dependency policy; identify downloads that S3 would require.
- Record candidate evidence and tradeoffs without scaffolding a backend, downloading dependencies, choosing data providers/models/storage, or changing Android code.
- Synchronize plan, status, handoff, architecture, and relevant proposed technology text in the report/presentation.
- **Evidence:** `docs/BACKEND_BASELINE.md` compares Spring Boot 4.1.1, Javalin 7.2.3, and Quarkus 3.33 LTS using official sources and recommends Java 21, Spring Boot 4.1.1 with the Servlet MVC starter, Maven 3.9.16 through Wrapper 3.3.3, a single `backend/` Maven module, exact coordinates, a minimal dependency policy, exclusions, and the downloads deferred to S3. The installed JDK/Javac 21.0.12 were reverified; neither Maven nor Gradle is globally installed. `git diff --check` and documentation path checks pass; the synchronized 9-page report builds twice with pdfLaTeX and LuaLaTeX, and the 14-slide presentation builds twice with LuaLaTeX. Logs contain no warnings or missing glyphs, extracted text preserves Serbian Latin and version names, and the changed report page and technology slide were visually inspected without clipping. No backend scaffold, dependency, provider, model, storage product, endpoint contract, or Android change was introduced.

The user explicitly accepted T-007/S1 with “accepted! continue w s2” on 2026-09-05.

#### T-007/S2 — Define the I1 contract and deterministic fixtures

- **Status:** Accepted

- Define the minimum application-facing request and response shapes for known, unknown, and unavailable product outcomes plus independently classified placeholder-recommendation outcomes.
- Define normalized fixture products and a reproducible scannable test barcode without presenting fixture rankings as semantic or personalized results.
- Define validation limits, provenance labels, and acceptance tests at the contract level.
- Synchronize TODO, plan, status, architecture, handoff, report, and presentation; do not scaffold or download implementation dependencies.
- **Evidence:** `docs/I1_CONTRACT.md` proposes one versioned JSON operation, independent product/recommendation outcome unions, RFC 9457 request errors, exact barcode validation, provenance and placeholder rules, nine acceptance cases, and explicit deferrals. `docs/fixtures/i1-products.json` defines five fictional known products plus controlled unavailable/unknown lookup keys using valid restricted-circulation EAN-13 values. `docs/fixtures/i1-known-product-ean13.svg` is a reproducible 95-module barcode for the primary phone path. `jq` validates the JSON structure; an independent modulo-10 check validates all seven fixture values; an independent encoder comparison validates the SVG's exact 95-module EAN-13 pattern. `git diff --check` and path checks pass. The 9-page report builds twice with pdfLaTeX and LuaLaTeX, and the 15-slide presentation builds twice with LuaLaTeX; logs contain no warnings or missing glyphs, extracted Serbian text is correct, and the changed report page, contract slide, and barcode render were visually inspected without clipping. No backend or Android code, dependency, provider, model, or storage product was added.

The user explicitly accepted T-007/S2 and authorized T-007/S3 with “accepted! great, you can commit this phase, and start the next one” on 2026-09-05.

#### T-007/S3 — Implement the fixture-backed backend slice

- **Status:** Accepted

- Scaffold one backend modular-monolith project using the accepted S1 baseline.
- Implement the S2 contract through internal API/product-resolution/recommendation boundaries using deterministic in-memory or packaged fixture data.
- Add automated tests for known, unknown, unavailable, placeholder-result, empty-result, and partial-success behavior.
- Synchronize build instructions and every affected operational/formal document; do not add external providers, embeddings, vector databases, or Android networking.
- **Evidence:** `backend/` is a Java 21/Spring Boot 4.1.1 modular-monolith scaffold with a checksum-pinned Maven 3.9.16 Wrapper. API, application coordination, product-resolution, recommendation, barcode-validation, and fixture-loading boundaries implement the frozen contract while packaging the canonical documentation fixture directly. Eleven barcode-rule tests and twelve real-HTTP API tests pass (`23/23`) through `./mvnw verify`; the executable JAR contains the fixture, starts on Java 21, and returns the exact primary known-product/two-placeholder-result response. No external provider, embedding, vector database, Android networking, persistence, or AI-result claim was added. Operational/formal documentation and all diagram variants were synchronized and rebuilt.
- **Acceptance evidence:** The user explicitly responded “I accept the changes, continue with your work” on 2026-09-05.

#### T-007/S4 — Connect Android and validate the vertical slice

- **Status:** Accepted and complete
- **Communication contract:** Android sends the scanner-provided value and mapped EAN/UPC format to the frozen `POST /api/v1/scan-queries` HTTP/JSON operation. The backend returns independent product and recommendation outcomes. During physical-device development, `adb reverse tcp:8080 tcp:8080` exposes the PC backend as device loopback; cleartext HTTP is permitted only by a debug configuration. A deployed environment must use HTTPS.

##### T-007/S4.1 — Establish the Android API-client boundary

- Compare and select the smallest maintainable Java HTTP/JSON client compatible with the accepted Android baseline; record the dependency/version decision before adding it.
- Add contract DTOs, barcode-format mapping, API invocation boundary, and an injectable base URL without invoking it from the scanner UI.
- Add a debug-only localhost/cleartext configuration for the `adb reverse` development path; do not weaken release network security.
- Test request serialization, all frozen response unions, malformed/unusable response handling, and transport-failure classification without requiring the physical phone.
- Synchronize `TODO.md`, `docs/DECISIONS.md`, `docs/PROJECT_STATUS.md`, `docs/ARCHITECTURE.md`, `docs/WORKFLOW.md`, `docs/PLANS.md`, `docs/SESSION_HANDOFF.md`, report, and presentation as affected.
- **Approval evidence:** The user explicitly responded “I explicitly approve s4.1; let's continue” on 2026-09-07.
- **Evidence:** The Android `network` package defines an app-owned cancellable `ScanQueryClient`, Retrofit service, frozen DTOs, strict outcome validator, scanner-format mapper, injectable/default client factory, and transport/HTTP/invalid-response failure classes. Debug builds target `http://127.0.0.1:8080/` and alone permit cleartext for the documented ADB reverse path; the release manifest denies cleartext and uses a non-routable HTTPS placeholder. Retrofit 3.0.0, converter-moshi 3.0.0, resolved Moshi 1.15.2, and API-36-compatible OkHttp 5.3.2 are resolved; OkHttp 5.5.0 was rejected by AAR metadata because it requires compile SDK 37. Twenty local tests pass, including request serialization, all product/recommendation unions, invalid JSON/invariants, HTTP/transport classification, cancellation, and four format mappings; lint reports zero issues, debug assembly succeeds, and both merged manifest policies were inspected. `MainActivity` has no reference to the new boundary, so S4.2 behavior is not claimed.
- **Acceptance evidence:** The user explicitly responded “I accept s4.1” on 2026-09-07.
- **Status:** Accepted

##### T-007/S4.2 — Connect successful scans to the backend

- Submit only successful supported EAN/UPC scans through the API-client boundary; preserve cancellation, empty-value, module-unavailable, and scanner-error behavior without network calls.
- Add explicit loading and transport/backend-unavailable states, and prevent stale responses from replacing newer UI state.
- Test format mapping, scan-to-request coordination, lifecycle-safe state behavior, and failure separation.
- Synchronize all affected operational/formal documentation in the same phase.
- **Approval evidence:** The user responded “okay, let's go” immediately after the S4.2 plan was presented on 2026-09-07.
- **Evidence:** `MainActivity` now constructs the accepted API client and delegates scan results to a pure-Java `ScanQueryCoordinator`. Only non-empty EAN-13/EAN-8/UPC-A/UPC-E results submit the exact scanned value/format; loading, response-received, transport, HTTP, and invalid-response states are distinct. Starting a newer scan or destroying the activity cancels active work, callback delivery crosses the Android main-thread executor, and callbacks from stale/closed requests cannot change visible state. Existing cancellation/module/scanner-failure paths remain local and make no request. Seven new coordinator tests bring the Android total to 27 with zero failures; lint has zero findings and the debug APK assembles from a clean build. The unchanged backend still passes all 23 tests. Product/recommendation fields are not rendered, and physical Android-to-backend behavior is not claimed before S4.4.
- **Acceptance evidence:** The user explicitly responded “s4.2 accepted” on 2026-09-07.
- **Status:** Accepted

##### T-007/S4.3 — Render product and deterministic-result outcomes

- Add minimal custom XML views for normalized product details and the independent recommendation status/results.
- Render `KNOWN`, `UNKNOWN`, product `UNAVAILABLE`, recommendation `RESULTS`, `EMPTY`, `UNAVAILABLE`, and `NOT_APPLICABLE` distinctly while preserving known product data on recommendation failure.
- Show “Deterministički demo rezultat — nije AI preporuka” whenever `placeholder` is true; never display a fabricated score.
- Add UI/state tests for the frozen outcomes and synchronize all affected documentation.
- **Approval evidence:** The user explicitly instructed “start s4.3” on 2026-09-07.
- **Evidence:** A scrollable custom XML outcome area now renders normalized known-product fields and separate recommendation state/content. `KNOWN`, `UNKNOWN`, product `UNAVAILABLE`, recommendation `RESULTS`, `EMPTY`, `UNAVAILABLE`, and `NOT_APPLICABLE` have distinct Serbian states. Every `placeholder: true` outcome shows the exact mandatory demo-only label; rows show only rank/name/brand/category and no score. Product data remains present when recommendations are empty or unavailable. Five new pure-Java outcome-model tests cover all frozen combinations, bringing the Android total to 32; lint has zero findings and debug assembly succeeds. Physical end-to-end execution remains reserved for S4.4.
- **Acceptance evidence:** The user explicitly responded “approved s4.3” on 2026-09-07.
- **Status:** Accepted

##### T-007/S4.4 — Validate and close the physical vertical slice

- Build both projects, start the backend locally, configure `adb reverse`, install the debug APK, and scan the controlled barcode asset on the verified phone.
- Exercise the happy path plus feasible unknown, empty-result, recommendation-unavailable, and backend-unavailable behavior; record which cases are automated versus physically observed.
- Re-run Android tests/lint/build and backend verification, inspect deliverables, synchronize TODO/status/architecture/plan/handoff/report/presentation, and stop for S4 and T-007 acceptance.
- **Approval evidence:** The user explicitly instructed “continue to s4.4” on 2026-09-07.
- **Evidence:** Clean Android verification passed all 32 tests, lint with zero findings, debug APK assembly, and release-manifest processing; clean backend verification passed all 23 tests and built the executable JAR. The started backend returned the exact primary fixture response in a localhost smoke test. After `adb reverse tcp:8080 tcp:8080` and a fresh debug APK installation, the physical API-36 phone displayed the known product with almond/soy ranks and the exact demo-only label. Physical checks also confirmed unknown/`NOT_APPLICABLE`, known product with `EMPTY`, known product retained with recommendation `UNAVAILABLE`, and backend-unavailable with stale outcome content cleared. UI hierarchy evidence was inspected for every case. The backend was stopped gracefully, ADB forwarding and temporary validation artifacts were removed, the APK remains installed, and no device identifier or UI dump was persisted.
- **Acceptance evidence:** The user explicitly accepted S4.4 and then explicitly stated “T-007 is also accepted” on 2026-09-08.
- **Status:** Accepted

### Approved task-level acceptance criteria

- One reproducible command sequence builds and tests both projects.
- A controlled barcode scan on the phone reaches the backend and displays fixture product details plus a clearly labelled deterministic placeholder list.
- Unknown product, backend unavailability, and empty recommendation outcomes remain distinguishable; recommendation failure does not hide known product data.
- Tests enforce the accepted I1 contract, and all documentation/formal deliverables distinguish deterministic scaffolding from semantic or personalized recommendations.
- No external product API, embedding model, vector-storage product, authentication system, or durable interaction history is introduced.

### Approved exclusions

- Live metadata lookup or catalog ingestion.
- Embedding generation, cosine similarity, MMR, vector indexing, or history-based personalization.
- Production hosting, authentication, accounts, analytics, or final UI polish.
- Changing the accepted scanner technology or architecture topology.

All task-level acceptance criteria were verified, and the user explicitly accepted and closed T-007 on 2026-09-08.

## T-008 — Define the MVP domain models

- **TODO source:** “Definisati modele proizvoda, korisničke interakcije i preporuke.”
- **Status:** Plan approved; S1 accepted; S2 revised and awaiting explicit approval
- **Goal:** Establish the smallest provider-neutral domain vocabulary needed by I2–I4 without selecting providers, persistence, embeddings, ranking algorithms, or changing the accepted I1 behavior.
- **Plan approval evidence:** After reviewing the two-subtask proposal, the user instructed “okay, let's start with all of these” on 2026-09-08. Under the mandatory separate gate, this authorizes S1 only; S2 still requires explicit approval after S1 acceptance.

### T-008/S1 — Define the normalized product model

- Inventory the implemented I1 product/identity/provenance fields and distinguish domain concepts from API DTOs, fixture format, Android UI state, and future persistence.
- Define the minimal product aggregate, barcode and provenance value objects, mandatory/optional fields, normalization limits, and `KNOWN`/`UNKNOWN`/`UNAVAILABLE` outcome invariants.
- Preserve the frozen I1 behavior and explicitly defer provider, fallback, database, image/nutrition, embedding, interaction, and recommendation choices.
- Add the canonical model document and synchronize TODO, documentation hub, I1 relationship, status, architecture, scope scorecard, handoff, report, and presentation.
- **Approval evidence:** The user instructed “okay, let's start with all of these” on 2026-09-08; per D-005, execution is limited to this first subtask.
- **Acceptance evidence:** A source-neutral product contract exists in `docs/DOMAIN_MODEL.md`; it separates stable internal identity from exact barcode lookup identity, defines bounded required/optional metadata and provenance, preserves independent resolution outcomes, maps the complete I1 fixture model without changing its wire contract, and records all later choices as deferrals. No Java, Android, provider, persistence, or AI artifact changed. Documentation paths and `git diff --check` pass; the 9-page report builds twice with pdfLaTeX and LuaLaTeX, and the 16-slide presentation builds twice with LuaLaTeX. Logs contain no document/layout/missing-glyph warnings, and the changed report page and model slide were visually inspected without clipping.
- **User acceptance:** The user explicitly stated “I accept S1, it's done” on 2026-09-08 and requested that the two model/AI diagrams be added to S2.
- **Status:** Accepted

### T-008/S2 — Define interaction and recommendation models

- Define the minimal anonymous `Interaction` event owned by Android: identity, known-product reference, event kind, ordering/time information, valid/invalid states, and data-minimization boundary.
- Define the bounded `HistoryContext`: ordered recent interactions, client ownership, cold-start/insufficient-history meaning, request validation, truncation/deduplication behavior, and privacy/retention decision points. Do not select persistence or a final K/weighting algorithm.
- Define recommendation concepts separately from product facts: deterministic fixture, generic semantic similarity, and personalized modes; ranked items; score semantics and comparability limits; `RESULTS`, `EMPTY`, `UNAVAILABLE`, and `NOT_APPLICABLE`; and the rule that recommendation failure never hides a known product.
- Define the AI-derived boundary: normalized factual product metadata remains source-backed, while embedding representations, similarity candidates/scores, history profiles, and reranked results are derived artifacts that never overwrite the product aggregate.
- Add canonical `docs/diagrams/domain-model.puml` for static entities/value objects, relationships, cardinalities, and ownership.
- Add canonical `docs/diagrams/ai-enrichment-flow.puml` for factual-source normalization, embedding derivation, semantic retrieval, optional history enrichment, cold start, and displayed result lineage.
- Generate English technical, Serbian report, and compact Serbian presentation variants from both sources; integrate the appropriate renders into architecture docs, report, and presentation without claiming that planned AI behavior is implemented.
- Specify I1 compatibility plus deterministic acceptance examples for no history, sufficient synthetic history, empty/unavailable recommendations, malformed history, and partial success. Do not alter the accepted I1 payload or application code.
- Synchronize TODO, domain model, status, decisions if accepted, architecture, diagram contract, plan, handoff, report, and presentation; validate/render all diagram variants, compile affected LaTeX deliverables, visually inspect them, and stop for S2/T-008 acceptance.
- **Status:** Revised plan; requires explicit user approval

### Task-level acceptance criteria

- Product, interaction, and recommendation concepts have explicit identities, fields, invariants, ownership, and lifecycle boundaries.
- I1 compatibility and future API evolution are explicit; planning language is not presented as implemented behavior.
- The contracts support I2 product resolution, I3 semantic similarity, and I4 bounded-history personalization without selecting their concrete providers, algorithms, or stores.
- S1 and S2 are separately accepted before the TODO item and T-008 are closed.

### Exclusions

- Refactoring Java/Android code or changing the accepted I1 HTTP contract.
- Selecting or calling a product provider, choosing a fallback dataset, or ingesting real products.
- Selecting persistence/vector technologies, embedding models, similarity metrics, history length, or ranking algorithms.
