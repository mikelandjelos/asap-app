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
- Show “Deterministički demo rezultat — nije AI preporuka” (English since D-028: “Deterministic demo result — not an AI recommendation”) whenever `placeholder` is true; never display a fabricated score.
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
- **Status:** Accepted and closed
- **Goal:** Establish the smallest provider-neutral domain vocabulary needed by I2–I4 without selecting providers, persistence, embeddings, ranking algorithms, or changing the accepted I1 behavior.
- **Plan approval evidence:** After reviewing the two-subtask proposal, the user instructed “okay, let's start with all of these” on 2026-09-08. Under the mandatory separate gate, that authorized S1 only; S2 was later approved separately as recorded below.

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
- **Approval evidence:** The user explicitly stated “approved s2 scope, execute” on 2026-09-08.
- **Evidence:** `docs/DOMAIN_MODEL.md` defines the accepted anonymous device-owned `PRODUCT_VIEWED` event, newest-first bounded `HistoryContext`, derived history readiness, deterministic/generic-semantic/personalized modes, independent recommendation states, ranked-item/evidence constraints, factual-versus-AI-derived boundary, I1 compatibility, and seven deterministic examples. Canonical `domain-model.puml` and `ai-enrichment-flow.puml` sources plus English, Serbian, and compact Serbian renders expose ownership/cardinality and AI data lineage without claiming implementation. All four PlantUML sources pass syntax checks in default, Serbian, and Serbian-presentation modes, and all 12 PNG variants regenerate successfully. The 12-page report builds twice with pdfLaTeX and twice with LuaLaTeX; the 18-slide presentation builds twice with LuaLaTeX. All three final logs contain no LaTeX, package, layout, or missing-glyph warnings. Report pages 8–10, the title slide, and diagram slides 13–14 were visually inspected without clipping; the AI slide uses a compact three-lane presentation view while its report render retains every stage. No Java, Android, provider, persistence, model, vector-store, or I1 payload changed.
- **User acceptance:** The user explicitly stated “I accept it, great!” on 2026-09-08 in response to the S2 and T-008 closure request.
- **Status:** Accepted; T-008 closed

### Task-level acceptance criteria

- Product, interaction, and recommendation concepts have explicit identities, fields, invariants, ownership, and lifecycle boundaries.
- I1 compatibility and future API evolution are explicit; planning language is not presented as implemented behavior.
- The contracts support I2 product resolution, I3 semantic similarity, and I4 bounded-history personalization without selecting their concrete providers, algorithms, or stores.
- S1 and S2 are separately accepted before the TODO item and T-008 are closed.

### Exclusions

- Refactoring Java/Android code or changing the accepted I1 HTTP contract.
- Selecting or calling a product provider, choosing a fallback dataset, or ingesting real products.
- Selecting persistence/vector technologies, embedding models, similarity metrics, history length, or ranking algorithms.

All task-level acceptance criteria were verified, and the user explicitly accepted S2 and closed T-008 on 2026-09-08.

## T-009 — Evaluate barcode product-data APIs

Priority update, 2026-10-06: paused by the user's deadline pivot to T-010. S1 artifacts and verification are preserved, still awaiting acceptance; S2 and S3 remain unapproved. This task is not closed.

- **TODO source:** “Pronaći i proceniti API-je za podatke o proizvodima na osnovu barkoda.”
- **Status:** Plan approved; S1 implemented and awaiting acceptance
- **Goal:** Produce an evidence-backed primary API recommendation for I2, or an explicit no-provider conclusion, against the accepted T-008 product model without integrating a provider or selecting the fallback dataset/storage implementation.
- **Planning authorization:** After T-008 closure, the user agreed that API evaluation should be next and authorized formulation of this plan on 2026-09-08.
- **Plan and S1 approval evidence:** After reviewing the written plan, the user explicitly responded “approved, authorized” on 2026-09-08. Under D-005 this approves the task plan and authorizes T-009/S1 only.

### T-009/S1 — Define the evaluation protocol and shortlist

- Create `docs/PRODUCT_DATA_API_EVALUATION.md` as the dated source of truth for this investigation.
- Derive mandatory and comparative criteria from the accepted product model: GTIN format support; product/category coverage without silently narrowing the application; required/optional field mapping; provenance; unknown versus unavailable behavior; official access method; authentication; quotas and cost; licensing, attribution, caching and redistribution constraints; localization; freshness; and operational fit for an MVP.
- Research current official provider documentation and terms, then shortlist two to four credible candidates. Record direct evidence links and a reason to test or reject each candidate. A food-specific source may be considered, but cannot redefine ASAP as food-only without a separate user decision.
- Define a reproducible probe corpus of at least 12 publicly verifiable real GTINs spanning relevant product categories and EAN-13, EAN-8, and UPC-A where available; include UPC-E only if a legitimate public example is available. Never query the local restricted-circulation `200…` fixtures.
- Define the S2 observation schema and request budget before any live endpoint call. Do not create accounts, buy plans, request credentials, call live product endpoints, or modify application code in S1.
- **Acceptance evidence:** Each criterion is testable; every shortlisted candidate has dated official-source evidence; the probe rules cover scope and supported barcode formats without using local fixtures; unknowns and any credential dependency are explicit.
- **Verification:** Validate all cited links and local paths, check the document for unsupported claims and secrets, run `git diff --check`, synchronize TODO/status/handoff/report when the research changes formal data-source content, and stop for S1 acceptance.
- **Affected documentation:** `TODO.md`, `docs/PRODUCT_DATA_API_EVALUATION.md`, `docs/README.md`, `docs/PLANS.md`, `docs/PROJECT_STATUS.md`, `docs/SESSION_HANDOFF.md`, and, if the candidate analysis materially changes formal data-source content, `report/report.tex` and `presentation/asap-presentation.tex`.
- **Evidence:** `docs/PRODUCT_DATA_API_EVALUATION.md` defines six mandatory gates and three comparative criteria against the accepted product model, exact provider-field mappings, and a dated three-provider shortlist backed by official documentation and terms. It defines 12 public product-bound inputs covering EAN-8, UPC-E, EAN-13, UPC-A, food/beverage, regional food, personal care, cosmetics, hardware, and media; all check digits and the UPC-E expansion were locally verified. The S2 protocol permits only 13 sequential read-only lookups per accessible provider (26 total), requires 11-second pacing, preserves exact scanned identity, distinguishes `KNOWN`/`UNKNOWN`/`UNAVAILABLE`/local `INVALID`, and forbids every local `200…` fixture. Barcode Lookup is documentation-only because an API key/account is required and its free-account terms prohibit automated access; UPCitemdb payload retention is blocked by unclear trial licence terms. No live product endpoint, account, credential, dependency, or application code was used or changed. Official links and local paths were checked; `git diff --check` and the secret/restricted-fixture scan pass. The synchronized 12-page report builds twice with pdfLaTeX and LuaLaTeX, and the 19-slide presentation builds twice with the ELFak LuaLaTeX workflow. Final logs contain no LaTeX, layout, or missing-glyph warnings; report page 11 and presentation slides 1 and 12 were visually inspected without clipping.
- **Status:** Implemented; awaiting user acceptance

### T-009/S2 — Run controlled read-only API probes

- Query only shortlisted endpoints whose official terms permit the experiment and whose access is available without account creation or payment. If a serious candidate requires credentials, stop and request user-supplied credentials or approval to evaluate it from documentation only; never commit or print a secret.
- Use the approved corpus and a descriptive user agent, respect published rate limits, and cap the run at 50 requests per provider. Record request time, HTTP/domain outcome, observed latency, required/optional field completeness, language, provenance support, and error/not-found behavior.
- Map observed records to the accepted T-008 model without treating provider IDs as ASAP IDs or inventing absent metadata. Confirm that unavailable, invalid, and unknown outcomes remain distinguishable where the API permits it.
- Store reproducible commands and a normalized measurement table. Preserve raw payloads only when provider terms permit redistribution; otherwise retain hashes or minimal non-copyrightable observations sufficient for verification.
- Do not integrate a provider, add runtime dependencies, persist provider data, select the fallback dataset, or alter the I1 contract.
- **Acceptance evidence:** The approved corpus was exercised consistently for each accessible candidate; totals reconcile with the recorded observations; exclusions and failed/unavailable probes are visible; no local `200…` fixture, credential, or prohibited payload entered the evidence.
- **Verification:** Parse/validate any retained machine-readable evidence, independently recompute table totals, scan tracked/untracked changes for secrets and restricted fixtures, run `git diff --check`, synchronize all affected documentation and formal deliverables, and stop for S2 acceptance.
- **Affected documentation:** S1 surfaces plus any approved evidence files under `docs/evaluation/product-data-apis/`, architecture notes if provider failure semantics reveal a required refinement, report, and presentation.
- **Status:** Blocked on accepted S1 and explicit S2 approval

### T-009/S3 — Compare candidates and recommend the I2 source boundary

- Apply mandatory gates first, then a transparent comparison matrix; do not hide a licensing, coverage, attribution, caching, or authentication failure behind an aggregate score.
- Recommend one primary provider, a deliberately limited hybrid, or no provider. State confidence, unsupported product categories/formats, cost/quota assumptions, attribution/cache obligations, expected fallback needs, and the exact evidence that could reverse the recommendation.
- Define the provider adapter boundary and follow-up acceptance cases at design level only: successful sparse record, unknown barcode, upstream unavailable, invalid/malformed provider response, rate limit, and safe rejection of restricted-circulation fixtures.
- Keep fallback-dataset selection, storage/cache implementation, API integration, credentials, and production operation in later separately approved tasks.
- **Acceptance evidence:** The recommendation follows from published gates and observed evidence; every important limitation is visible; the accepted product/provenance and partial-success contracts remain intact; next-task dependencies are concrete.
- **Verification:** Recheck evidence links and calculations, validate documentation paths, compile and visually inspect changed report/presentation material, run `git diff --check`, synchronize TODO/decision/status/handoff, and stop for S3 and T-009 acceptance.
- **Affected documentation:** All prior T-009 surfaces plus `docs/DECISIONS.md`, `docs/ARCHITECTURE.md`, `report/report.tex`, and `presentation/asap-presentation.tex`.
- **Status:** Blocked on accepted S2 and explicit S3 approval

### Task-level acceptance criteria

- Current official documentation and controlled observations support the conclusion; source dates and evidence links are retained.
- Coverage and field mapping are evaluated against ASAP's accepted general product model rather than assumed from a provider's marketing.
- Legal/operational constraints and unknown/unavailable/rate-limit behavior are explicit.
- The recommendation does not query restricted local fixtures, leak credentials, overstate sampled availability as an SLA, or claim integration.
- All three subtasks are separately accepted before T-009 and its TODO item are closed.

### Dependencies and exclusions

- S1 requires internet browsing of official provider documentation. S2 requires network access to permitted read-only endpoints and may require user-supplied credentials for a candidate to remain testable.
- No account creation, paid subscription, terms acceptance on the user's behalf, bulk scraping, provider integration, fallback-dataset selection, storage implementation, embedding work, or Android/backend behavior change belongs to T-009.

## 2026-10-06 — D-025 documentation audit, commit and push

- **Approval:** The user explicitly requests recording mandatory PCA, MMR, polished UI and notebook verification for every AI/statistical component, auditing all documentation, then committing and pushing the handoff. This single documentation/publishing subtask does not authorize feature implementation.
- **Plan:** Record D-025 and an authoritative requirements/evidence checklist; reconcile active docs and report; inventory historical/deferred artifacts without discarding information; verify links, scope consistency, report builds and staged diff; commit with a short one-line message and push the current branch to its configured origin.
- **Acceptance:** Required features and notebook evidence are recoverable without chat, current versus historical state is explicit, no invented results, presentation finalization remains deferred, and commit/push outcome is reported.
- **Status:** Documentation and verification complete; commit/push explicitly authorized as the final publishing step (actual outcome is recorded by Git). T-009/S1 and T-010/S1 acceptance remains pending; future implementation still needs explicit subtask approval.
- **Evidence:** `CURRENT_REQUIREMENTS.md`, `NOTEBOOK_VALIDATION.md` and `DOCUMENTATION_AUDIT.md` preserve the full revised scope and notebook criteria. Report includes the requested GitHub link on page 1 and builds to 19 pages with three clean passes under both engines; cover/notebook page visually inspected. All local Markdown links, six marker mappings, PDF copy equality and whitespace checks pass. Historical scope and deliberately deferred presentation/diagrams are explicitly classified; no runtime code, experiments or current performance claims were added.

## 2026-10-06 — Scope amendment and agent handoff

- **Authorization:** User explicitly requested recording revised scope and preparing the repository for another agent; this is documentation-only handoff work, not approval to execute future implementation.
- **Bounded plan:** Record D-024 and mandatory TODO items; reconcile scope/status/report and flag historical diagrams; preserve dirty work and deferred presentation; verify report build and whitespace; stop with a restart prompt.
- **New requirements:** Clustering and personalization are required for MVP acceptance. Finish PoC/MVP with the existing scanner, then replace only scanner-related parts with a dataset-trained CNN/TFLite EAN/UPC pipeline. Decoder/dataset/model/clustering details require later planning.
- **Next-agent gate:** Propose one concise revised delivery plan, including minimal clustering/personalization acceptance evidence and the scanner replacement last. Do not automatically execute T-009/S2 or T-010/S2. T-009/S1 and T-010/S1 still await acceptance.
- **Presentation:** Explicitly deferred until the end; no edits or rebuild in this handoff.
- **Result:** Handoff complete. D-024 and pending design work recorded across current docs; report scope reconciled and PDF rebuilt to 18 pages with three pdfLaTeX passes and clean final log. `git diff --check` passes. No application code, presentation, provider access, training or commit changed. Historical verification entries remain dated; this amendment did not rerun runtime tests or LuaLaTeX.

## T-010 — Deadline mock MVP and complete report draft

D-024 supersedes this plan's original MVP assumptions. Its mock package remains an interim demonstration, never a replacement for required clustering/personalization. Reconfirm next work with the user through a revised plan.

- **Status:** Plan and S1 approved on 2026-10-06 by “let's do it quickly, and then go back to finishing the plan”; S1 complete and awaiting acceptance. Later subtasks remain separately gated.
- **User direction (2026-10-06):** Prioritize speed, minimal mock MVP, and a report draft in its final form with explicit markers for unfinished work.
- **Goal:** Reuse the working I1 implementation for a reproducible simulation and produce a complete Serbian report structure quickly. Preserve the larger AI roadmap without claiming its completion.
- **Scope:** Existing Java/XML scanner, Spring backend, bundled fictional catalog and deterministic recommendation simulation. No new provider, model download, database, hosting, personalization, or framework is required for this deadline milestone.
- **Draft convention:** Verified results may use completed tense. Future completion wording is permitted only inside visibly labelled conditional draft blocks: `NACRT — NIJE IMPLEMENTIRANO/PROVERENO`, with a stable marker ID and required evidence. Markers must appear in the PDF, not only source comments. Measurements, participant counts, dates and feedback remain explicit blanks until supplied or observed. Draft status is visible on the cover. Remove markers only after evidence or replace the passage with an accurate limitation before submission.

### T-010/S1 — Complete the report draft first

- Fill all five report sections in Serbian Latin using existing implementation, design contracts and diagrams; frame target users and benefits as design assumptions where unvalidated.
- Explain the real scanner/backend flow and the deterministic simulation. Provide final-form conditional draft wording for later AI work with visible markers; do not fabricate experiments, teacher meetings or user feedback.
- Add `docs/REPORT_COMPLETION.md` mapping every marker to missing implementation, verification or user input. Separate submission needs from work deferred until the defense.
- Synchronize README/documentation hub, TODO, status, plan, handoff, and the affected scope/decision records to distinguish this deadline milestone from the full MVP. Correct the stale AGENTS claim that no application code exists.
- **Verification:** Compile the report twice, inspect changed pages and Serbian glyphs, validate marker/register correspondence and documentation links, run `git diff --check`.
- **Acceptance:** A readable complete report draft and PDF, no generic empty section, and an actionable list of remaining evidence. Existing code and T-009 work preserved.
- **Status:** Accepted as a working draft on 2026-10-06 (D-026); to be rewritten after the MVP. Documentation-only execution, no runtime changes. The user explicitly deferred presentation work until the end; existing presentation changes were preserved without further editing or rebuilding it.
- **Evidence:** All five Serbian sections populated, with six visible conditional draft blocks R01–R06 and a cover notice; `docs/REPORT_COMPLETION.md` maps each to missing work/evidence and a truthful submission alternative. Both report engines compile repeatedly to 17 pages; final logs have no warnings, missing glyphs or overfull/underfull boxes. Cover and pages 13, 14, 16 and 17 visually inspected; all marker blocks are readable and unclipped. PDF copied to `report/report.pdf`. Marker correspondence, local paths and `git diff --check` verified. Runtime test counts remain dated September evidence, not new test runs. README, scope, D-023, TODO, status, hub, handoff and stale AGENTS statement reconciled.

### T-010/S2 — Package the smallest reproducible mock demo

- Reuse the existing fixture catalog and recommendation lists; preserve explicit simulation labels and independent product/result failure states.
- Supply a compact printable barcode sheet for existing scenarios and one concise startup/demo procedure. Avoid UI expansion unless a verified demonstration blocker requires a scoped fix.
- Build/test Android and backend, smoke-test the HTTP flow, and document what was reverified versus inherited physical evidence. A fresh phone check requires a connected authorized device; do not claim it if unavailable.
- **Affected docs:** README, workflow, demo instructions/assets, TODO, status, plan, handoff, report and presentation where results change.
- **Acceptance:** Repeatable known/unknown/empty/unavailable scenarios, usable build artifacts, test results, and no live-provider dependency.
- **Status:** Deferred until after the T-011 MVP (D-026).

### T-010/S3 — Prepare the submission package

- Reconcile report markers against the verified demo. Replace unsupported submission claims with limitations; retain a separate visibly marked future draft only if useful.
- Synchronize the Serbian presentation, README, TODO/status/handoff and defense backlog. Describe full AI features as pending until implemented.
- Rebuild and inspect final PDFs, verify documented commands/paths, and present deliverables plus any course-required evidence still missing.
- **Acceptance:** Consistent repository and formal deliverables, clear implemented/simulated/deferred scope, and no unmarked hypothetical results. Request task acceptance; do not start the defense backlog automatically.
- **Status:** Deferred until after the T-011 MVP (D-026).

## T-011 — AI MVP delivery (revised plan, D-026)

- **Status:** Plan approved 2026-10-06 (“I accept the revised plan for now”). T-010/S1 accepted as a draft; T-010/S2–S3 deferred until after the MVP (D-026). T-009/S1 accepted 2026-10-06; its S2 is folded into T-011/S2.
- **S1:** Approved 2026-10-06 (“I approve t-011/s1”); complete, awaiting acceptance. The user proposed multiple APIs behind an adapter + router; this is incorporated (AI_MVP_DESIGN §2).
  - **Evidence:** `docs/AI_MVP_DESIGN.md` covers every open choice with a recommendation, alternatives, rationale and the notebook matrix; D-027 is recorded.
  - **Diagrams:** `component-architecture.puml` revised and `recommendation-pipeline.puml` added. Both pass syntax checks in the default and Serbian variants; their renders were regenerated and visually checked. Presentation renders were not regenerated.
  - **Report:** new §3.6 and pipeline figure; caption updated. pdfLaTeX and LuaLaTeX each built twice to 22 pages with clean final logs; pages 16–17 inspected; copied to `report/report.pdf`.
  - **Docs synced:** architecture, requirements, domain note, evaluation, hub, audit, TODO, status, handoff. `git diff --check` passes.
  - No code, downloads or provider calls.
- **S1 accepted** 2026-10-06 with amendments (D-028): English app text, vector DB after the MVP.
- **S1a (explicit user instruction “correct all the serbian text before you continue”):** translate `strings.xml`, the I1 fixture JSON, Android test literals and I1 contract examples to English; update label references in report/status. Verification: 32/32 Android tests, 0 lint issues, debug APK; 23/23 backend tests. Report rebuilt. Presentation (deferred) still quotes the old label.
- **S2:** Approved 2026-10-06 (“I agree with everything else”); complete and accepted 2026-10-06 (“I accept”).
  - **Probes:** terms rechecked; 26 probe calls run (`ml/probes/run_probes.py`); `notebooks/00_source_probes.ipynb` executed clean with results in `notebooks/results/00_source_probes/`.
  - **Catalog:** OFF, OBF and OPFF exports downloaded and checksummed; `ml/asap_ml/catalog.py` built a 10,000-product catalog plus manifest. The first selection run produced 25k junk strata; this was fixed with taxonomy, script and meta-tag filters.
  - **Records:** D-029, the evaluation S2 section and design §2/§3 updated; T-009 closed.
  - **Report facts for the end:** the router-merge completeness table, provider latencies and catalog stats.
- **S3:** Approved 2026-10-06 (“yes, continue”); complete, awaiting acceptance.
  - **Run:** ML stack pinned in `ml/requirements.txt`; model licences checked (MIT/Apache-2.0) and revisions pinned. Shared code is in `ml/asap_ml/retrieval.py`. `notebooks/01_data_embeddings_retrieval.ipynb` was executed clean with results in `notebooks/results/01_data_embeddings_retrieval/`.
  - **Gate failure:** the provisional gate "embedding beats TF-IDF" failed for e5 alone. A hybrid tuned on disjoint queries passes with significance (D-030).
  - **Issues fixed during the run:** `optimum` was incompatible with transformers 5.x, so export is now done with `torch.onnx` (wrapper module).
  - **Report facts for the end:** the leak-free methodology, the results table with CIs, the α sweep, ONNX parity and the latency table.
- **S3:** accepted 2026-10-06 (“I accept S3”).
- **S4:** Approved 2026-10-06 (“continue with S4”); complete, awaiting acceptance.
  - **Run:** `ml/asap_ml/structure.py` added; `notebooks/02_clustering_pca.ipynb` executed clean with results in `notebooks/results/02_clustering_pca/`.
  - **Rule revision:** the first run gave language clusters. A representation comparison (full / type / debiased) followed, and the user approved a revised selection rule (“yeah”, D-031) on UX grounds.
  - **Report facts for the end:** the representation table, k sweep, rule revision, themes, PCA variance and reduction tables, and the map.
- **S4:** accepted 2026-10-06 (“good, I accept it”).
- **S5:** Approved 2026-10-06 (“you can continue”); complete, accepted 2026-10-06 (“very cool”). The user added D-033 (map views and personal analytics dashboard).
  - **Run:** `ml/asap_ml/ranking.py` added; `notebooks/03_personalization_mmr.ipynb` executed clean with results in `notebooks/results/03_personalization_mmr/`.
  - **Bug fixed during the run:** bootstrap index sizing for subgroup CIs.
  - **User note recorded:** post-MVP drift monitoring and refit of PCA, clusters and TF-IDF (TODO).
  - **Report facts for the end:** the synthetic-user protocol, personalization table with CIs, cold-start table, MMR trade-off and runtime.
- **S6 split** into S6a–S6d, each approved separately (proposed 2026-10-06).
- **S6a:** Approved 2026-10-06 (“cool, let's go”); complete, awaiting acceptance.
  - **Export:** `ml/export_bundle.py` exports bundle `20261006-d7293828` (12 files, 526 MB), asserting the notebook hashes and parameters.
  - **Verification:** `ml/verify_bundle.py` passes all 10 checks from bundle files only, with a pure-Python TF-IDF max error of 5e-9.
- **S6a:** accepted 2026-10-06 (“good, continue”).
- **S6b:** Approved 2026-10-06 (“good, continue”); complete, awaiting acceptance.
  - **Dependencies:** versions checked on Maven Central; the DJL natives are bundled (no runtime download).
  - **Code:** 9 new main classes in `backend/.../ai/`, plus `CharTfidfTest` and `BundleParityTest`.
  - **Verification:** `./mvnw verify` passes 32/32 (23 existing + 4 unit + 5 parity, none skipped). v1 still starts and serves without a bundle. The JAR is ~101 MB.
  - **Report facts for the end:** Java parity and Java latency.
- **S6b:** accepted 2026-10-06 (“I think this is good”).
- **S6c:** Approved 2026-10-06 (“you can continue”); complete, awaiting acceptance.
  - **Code:** `PersonalRanker`, `RecommendationEngine`, `AiConfiguration` and the `api/v2` package (`V2Contract`, `HistoryValidator`, `V2Controller`), plus `InvalidRequestException`. The size filter was generalized to per-path limits; v1 is unchanged.
  - **Pre-existing bug fixed:** unknown routes returned 500 through the catch-all handler and now return a 404 problem.
  - **Tests:** 46/46 (+5 ranking parity, +8 v2 HTTP, +1 no-bundle 404).
  - **Smoke test:** packaged-JAR startup 5.4 s, about 9 ms per request over HTTP.
  - **Issue found:** same name+brand variants flood results; S6c.1 is proposed.
- **S6c:** accepted 2026-10-06 (“do as per you recommended”); S6c.1 approved in the same message.
- **S6c.1:** complete, awaiting acceptance.
  - **Changes:** `ranking.variant_key/collapsed_candidates`; notebook 03 re-executed with a new before/after section; exporter/verifier/Java updated (`variantKey` exported per product; `ProductText.variantKey` and `recommendUncatalogued` for S6d).
  - **Versioning bug fixed:** bundle versions now hash the params and notebook summaries, since the first re-export kept the old version id. New bundle `20261006-ee94fdb0`.
  - **Verification:** verifier 10/10; Java 46/46. The smoke query now returns 10 distinct products.
- **S6c.1:** accepted 2026-10-06 (“yes, go on, great”); S6d approved in the same message.
- **S6d:** complete, awaiting acceptance.
  - **Code:** `sources` package (types, OFF, UPCitemdb, router, fetcher), AI configuration properties, and the v2 uncatalogued path (`gtin:` ids, `fieldSources`, UPC attribution).
  - **Tests:** 54/54 (+6 source/router unit, +2 HTTP with a scripted fetcher; the tests never use the network).
  - **Live check:** about 5 OFF and 2 UPCitemdb calls; results in V2_CONTRACT.
  - **Fix:** UPCitemdb spacing is now configurable (tests use 0 s).
- **S6d:** accepted 2026-10-06 (“yes I accept”).
- **Theme label pass:** approved 2026-10-06 (“do the label pass”); done.
  - **Labels:** 60 short English labels in `ml/theme_labels.json`, based on each cluster's top categories, shares and sample names; status "proposed, user review pending". The exporter applies them and keeps `autoLabel`; the label file is part of the bundle version hash.
  - **Verification:** bundle `20261006-07904a7f`, verifier 10/10, Java 54/54.
  - **Remaining limitation:** external products whose OFF categories are non-English (e.g. "Pâtes à tartiner") can still land in mixed themes.
- **Post-MVP note:** `docs/POST_MVP_ARCHITECTURE.md` written on request (2026-10-06).
- **S7 — Android v2 + polished UI.** Plan written 2026-10-06 after “then continue to the 7”. Five sub-steps, committed one at a time:

| Sub-step | Scope | Acceptance evidence |
| --- | --- | --- |
| S7a | Data layer: strict v2 Retrofit client + validator; device history store (app-private JSON, newest 50, 90-day max age, clear); `PRODUCT_VIEWED` recorded after a known product is shown; newest 20 sent | unit tests (client, validator, store ordering/limits/expiry), lint |
| S7b | Material 3 shell: Material Components, light/dark theme, single Activity + bottom navigation (Scan, Product, Map, History), scanner flow moved over, English strings | build, lint, tests, phone launch |
| S7c | Product screen: product card (theme chip, source/attribution), results list with mode label, tap a result to open it, all states (loading/empty/unknown/unavailable/error + retry) | tests for UI models, phone check of each state |
| S7d | **Analytics** tab (chart icon, D-039): PCA "you vs themes" as the main chart plus further personal analytics, and the History screen (list, clear, privacy notice) | phone check, screenshots |
| S7e | Accessibility and end-to-end verification on the phone: TalkBack labels, 48 dp targets, contrast, 200 % font, dark mode | checklist + screenshots; Android/backend test counts |

  The UI decisions (visual style, navigation, results layout, map content) are asked of the user before S7b. S7a needs none of them.
- **UI decisions (user, 2026-10-06, D-038):** fresh grocery green; bottom tabs; one labelled results list; map with themes, you and scans.
- **S7a:** approved by “then continue to the 7”; complete, awaiting acceptance.
  - **Code:** `V2ApiModels`, `V2ApiService`, `V2ResponseValidator`, `V2Client` and `ApiClientFactory.createV2`; `history/HistoryStore`. API 23-safe (no `java.time`/`List.of`).
  - **Tests:** 41/41 (+9: request serialization, validator accept/reject cases, client failure classes, store order/window/limits/expiry/clear/corrupt file). Lint 0.
  - **Visible change:** none on the phone yet (UI in S7b–c).
- **S7a:** accepted 2026-10-06 (“good, you can continue”); S7b approved in the same message.
- **S7b:** complete in code, awaiting acceptance.
  - **Dependency:** Material Components 1.14.0.
  - **Theme and shell:** grocery-green Material 3 light/dark palette (`values[-night]/colors.xml`); single Activity with toolbar + `BottomNavigationView` (Scan, Product, Map, History) and kept-alive fragments; edge-to-edge insets.
  - **Scan screen:** scan card and the 5 most recent distinct products (tap to reopen).
  - **Session:** `ScanSession` (v2 successor of the v1 coordinator: cancellation, stale-drop, history recorded only for KNOWN) held in `SessionViewModel`.
  - **Product screen:** state summary (full card and list in S7c). Map/History are placeholders until S7d.
  - **Removed:** v1-only UI classes `ScanQueryCoordinator` and `I1OutcomeUiModel`, with their tests; v1 strings replaced by English UI strings.
  - **Verification:** tests 34/34 (+5 `ScanSessionTest`, −7 removed v1 UI tests); lint 0 after fixing 4 findings; debug APK 9.2 MB installed on the phone. The visual check is pending because the phone was locked.
- **S7b phone check (2026-10-06):** "Quattro Plazma" resolved in dark mode with theme "Biscuits & crackers" and 10 results.
  - **Bug reported by the user:** every later scan showed "temporarily unavailable". Cause: UPCitemdb's 11 s spacing guard. Fix: a 5/min rolling burst window.
  - **Test-run OOM:** the backend test JVM was OOM-killed while the phone-test backend held a second copy of the model; it was rerun with the backend stopped and passed 54/54.
  - **Second root cause:** after the burst fix, scans still showed "unavailable". The new per-lookup logging showed `transport` failures at exactly the 1.5 s per-source timeout.
    - Diagnosis: `curl` took 0.8–1.0 s, a Java client 1.0–4.8 s; IPv6 was ruled out (IPv4-only DNS).
    - Fix: configurable timeouts (8 s per source, 12 s total, 4 s connect). The failing barcode now resolves to an honest `UNKNOWN` in 2.0 s.
  - **Pending:** dark-mode status-bar icon contrast, to fix in S7c.
- **Order after the MVP (user, 2026-10-06, D-040):**
  1. Finish S7c–S7e and S8.
  2. CNN/TFLite scanner replacement, before the report, because deep learning for barcodes is a promised component.
  3. A new report written from scratch, following the user's instructions.
  4. Then: user-perspective testing, demo preparation, presentation, deployment, and the post-MVP refit pipeline (Airflow-style orchestration, `POST_MVP_ARCHITECTURE.md`).
- **S7c:** approved 2026-10-06 (“let's go legend!”). The S7d Analytics content proposal (PCA main chart, top themes, personalization status, 14-day activity, data sources) still needs explicit confirmation before S7d.
- **Commit rule (user, 2026-10-06):** commit at the end of every subtask before proceeding.
- **Rule:** One explicitly approved subtask at a time; each updates affected docs atomically. Optional expansion/polish only after MVP, report and slides are done.

| Subtask | Scope | Acceptance evidence |
| --- | --- | --- |
| S1 | Design decisions, docs only: offline dataset (Open Food Facts subset candidate), embedding model, vector storage, roles of clustering/PCA/MMR, history/profile method, notebook decomposition and metrics, UI direction. | Each open choice from `CURRENT_REQUIREMENTS.md` with recommendation, alternatives, rationale; notebook-component matrix; updated PlantUML sources/renders; TODO/PLANS/DECISIONS/handoff synchronized; report build if changed. No code, downloads or provider calls. |
| S2 | Data source: bounded T-009/S2 read-only probes (≤26 calls), then select primary API and offline dataset. | Observation rows, selection decision, license notes. |
| S3 | Notebook 01: dataset preparation, embeddings, top-N retrieval quality and latency. | Clean run-all, exported tables/plots, result summary. |
| S4 | Notebook 02: clustering and PCA with plots. | As S3. |
| S5 | Notebook 03: history personalization vs generic baseline; MMR parameter sweep. | As S3. |
| S6 | Backend semantic/clustering/MMR pipeline behind a v2 API contract; notebook parity checks. | Contract, tests, parity evidence. |
| S7 | Android local history and polished Java/XML UI. | Tests, lint, phone screenshots/checks. |
| S8 | End-to-end phone verification; report updated with real results. | Device evidence, rebuilt report. |
| Later | CNN/TFLite scanner replacement with notebook; presentation last. | Per `NOTEBOOK_VALIDATION.md`. |
