# Session handoff

Last updated: 2026-09-07

## Completed in the latest documentation session

- The user explicitly accepted T-007/S4.2 and authorized T-007/S4.3 with “s4.2 accepted, start s4.3” on 2026-09-07. S4.3 is in progress; S4.4 remains unauthorized.
- `MainActivity` now routes only non-empty supported scan results through a pure-Java `ScanQueryCoordinator` into the accepted `ScanQueryClient`. Loading, response receipt, transport failure, HTTP failure, and invalid-response state are distinct; scanner cancellation/failure branches make no API request.
- A newer scan and activity destruction cancel active work; callbacks are dispatched through the main-thread executor and stale/closed callbacks are ignored. Seven new coordinator tests bring the Android total to 27; a clean Android build, lint, and debug assembly pass, and the unchanged backend still passes all 23 tests. Product/recommendation response content is intentionally not rendered until S4.3, and no physical end-to-end claim is made before S4.4.
- The user explicitly accepted T-007/S4.1 on 2026-09-07; it was committed as `40ed840` (`Add Android API client`).
- Android has an app-owned `ScanQueryClient` implemented with Retrofit 3.0.0, converter-moshi 3.0.0, Moshi 1.15.2, and OkHttp 5.3.2. It serializes the frozen request, validates every response union, maps all four accepted scanner formats, distinguishes transport/HTTP/invalid-response failures, and returns a cancellable handle. S4.2 connects it to `MainActivity` through the coordinator.
- Debug builds alone permit cleartext and use `http://127.0.0.1:8080/` for `adb reverse tcp:8080 tcp:8080`; release builds deny cleartext and use a non-routable HTTPS placeholder. OkHttp 5.5.0 was rejected during verification because it requires compile SDK 37; 5.3.2 preserves the accepted API-36 baseline.
- At the accepted S4.1 checkpoint, Android verification passed with 20 tests, zero lint issues, successful debug assembly, and inspected debug/release merged manifests. S4.2 now supersedes that test count and isolation status.
- T-007/S3 was committed as `94d4c9f` (`Implement I1 backend`) after explicit acceptance. The T-007/S4 integration plan is refined into four separately gated phases: API-client foundation, scan coordination, outcome UI, and physical validation; S4.1–S4.2 are accepted and S4.3 is in progress.
- The proposed development transport uses the frozen HTTP/JSON contract through `adb reverse tcp:8080 tcp:8080`, a device-loopback base URL, and debug-only cleartext permission. Any deployed endpoint must use HTTPS.
- The user explicitly accepted T-007/S3 with “I accept the changes, continue with your work” on 2026-09-05. The new `backend/` Java 21/Spring Boot 4.1.1 modular monolith implements the frozen I1 API through separate API, coordination, product-resolution, recommendation, validation, and fixture-loading boundaries.
- The checksum-pinned Maven 3.9.16 Wrapper builds an executable JAR and packages the canonical `docs/fixtures/i1-products.json` without a duplicate editable copy. No external provider, database, embedding, vector index, Android networking, or AI-result claim was introduced.
- `./mvnw verify` passes all 23 tests: 11 barcode-rule cases and 12 real-HTTP API cases covering known, unknown, product unavailable, deterministic results, empty results, recommendation unavailable, repeatability, validation, media type, and body-size behavior. The executable JAR starts on Java 21 and returns the exact primary fixture response in a localhost smoke test.
- Operational/formal documentation and canonical diagram variants were synchronized with the implemented backend boundary. The next permissible action is planning and explicit approval of T-007/S4; do not implement it without that approval.

- The user explicitly accepted T-007/S2, authorized its commit, and authorized T-007/S3 with “accepted! great, you can commit this phase, and start the next one” on 2026-09-05. D-017 freezes the I1 contract/fixtures subsequently implemented by S3 and consumed by S4.
- T-007/S2 is accepted. `docs/I1_CONTRACT.md` defines the frozen versioned operation, exact validation and outcome invariants, RFC 9457 errors, deterministic-placeholder labelling, explicit deferrals, and nine S3/S4 acceptance cases.
- `docs/fixtures/i1-products.json` contains five fictional products plus controlled product-unavailable and unknown keys. All seven EAN-13 values have valid modulo-10 digits and use restricted-circulation prefix 200; they are local-only and must never reach an external provider.
- `docs/fixtures/i1-known-product-ean13.svg` encodes the primary `2000000000015` happy-path fixture in a 95-module EAN-13 pattern with quiet zones for later physical-phone scanning. No application code or dependency was created/downloaded.
- T-007/S2 verification passes: `jq` structure checks, unique/count checks, modulo-10 validation of all seven EAN-13 values, independent comparison of the SVG against the expected 95-module encoding, `git diff --check`, path checks, two-pass pdfLaTeX and LuaLaTeX builds of the 9-page report, and a two-pass LuaLaTeX build of the 15-slide presentation. Logs have no warnings or missing glyphs; extracted text is correct, and the changed report page, contract slide, and barcode render were visually inspected without clipping. Physical barcode decoding remains the explicit S4 case I1-C09.
- The user explicitly accepted T-007/S1 and authorized T-007/S2 with “accepted! continue w s2” on 2026-09-05. D-016 and `docs/BACKEND_BASELINE.md` now freeze the Java 21/Spring Boot 4.1.1/Maven Wrapper baseline; it remains unimplemented until S3.
- The user approved the four-subtask T-007 plan and explicitly authorized only T-007/S1 with “continue with T-007:S1” on 2026-09-05.
- T-007/S1 is accepted. Its official-source comparison selected Java 21, Spring Boot 4.1.1 with `spring-boot-starter-webmvc`, Maven 3.9.16 through Maven Wrapper 3.3.3, and one `backend/` Maven project; exact policy, alternatives, exclusions, and S3 downloads are in `docs/BACKEND_BASELINE.md`.
- At the S1 checkpoint, OpenJDK/Javac 21.0.12 and the absence of global Maven/Gradle were reverified; no backend scaffold or dependency was added. The later S2 authorization produced only the design artifacts listed above.
- T-007/S1 verification passes: `git diff --check`, documentation path checks, two-pass pdfLaTeX and LuaLaTeX builds of the 9-page report, and a two-pass LuaLaTeX build of the 14-slide presentation. Logs contain no warnings or missing glyphs; extracted Serbian Latin/version text is correct, and the changed report page and technology slide were visually inspected without clipping.
- User explicitly accepted and closed T-006 and authorized its commit with “I accept you can commit” on 2026-09-05.
- User explicitly accepted T-006/S2 with “I accept this” on 2026-09-05; both T-006 subtasks are accepted.
- T-006/S2 defines the accepted Android deployment plus one backend modular monolith; bounded history is device-owned request context, product/vector stores have explicit backend module owners, and product/recommendation failures remain independent. Both canonical diagrams and all localized renders were updated; no application code or concrete technology/provider/schema was added.
- T-006/S2 verification passes for all three PlantUML variants, two-pass pdfLaTeX and LuaLaTeX report builds, and a two-pass LuaLaTeX presentation build. The report remains 8 pages, the presentation is 14 slides, relevant logs contain no document warnings or missing glyphs, and both diagram pages plus all three changed architecture/diagram slides were visually inspected without clipping.
- User explicitly accepted T-006/S1 and authorized T-006/S2 with “accepted - commit and start s2” on 2026-09-05.
- User approved the two-subtask T-006 plan and explicitly authorized only T-006/S1 with “let's go - do the s1” on 2026-09-05.
- T-006/S1 created the accepted `docs/MVP_SCOPE.md` contract with the minimum user journey, mandatory 80-point P0 core, committed 15-point extended MVP for history-based recommendation, final 5-point broader evaluation increment, required behavior, non-goals, assumptions/dependencies, six thin delivery iterations, exit evidence, and change control. A bounded last-K history is the baseline candidate, but the exact method remains open.
- T-006/S1 verification passes: `git diff --check`, path checks for the linked scope/architecture/formal sources, a two-pass 8-page pdfLaTeX report build, and a two-pass 13-slide LuaLaTeX presentation build. Both logs have no LaTeX warnings, overflow/underflow warnings, missing characters, or undefined references; the changed scope page and slide were visually inspected.
- User explicitly accepted and closed T-005 on 2026-09-05 after reviewing the task-level evidence.
- User explicitly accepted T-005/S3 on 2026-09-05. ADB reauthorization succeeded, the S2 debug APK was installed on the verified Samsung phone, and `MainActivity` was confirmed as the top resumed activity.
- The user reported two successful real-product scans with decoded values returned to ASAP and confirmed the intended cancellation status. This confirms the scanner module is usable, but not whether it was newly downloaded or already present. Failure handling is implemented and locally tested; physical failure injection was not attempted.
- User explicitly authorized T-005/S2 with “execute s2” on 2026-09-04 and continued the interrupted verification on 2026-09-05.
- T-005/S2 integrated Google Code Scanner 16.1.0 and `barcode_ui`, configured auto-zoom plus EAN-13/EAN-8/UPC-A/UPC-E, and replaced the placeholder with a custom scan/status/result screen.
- Success, empty value, cancellation, module/download unavailability, and general failure have user-visible outcomes. No product lookup, backend behavior, phone installation, or runtime claim was added.
- Seven local tests, lint with zero findings, and debug assembly pass. APK inspection confirms min SDK 23, target SDK 36, `barcode_ui`, and no camera permission; the dependency contributes internet and network-state permissions.
- User accepted T-005/S2 and explicitly authorized T-005/S3 on 2026-09-05.
- User approved the T-005 plan and explicitly authorized only T-005/S1 on 2026-09-04.
- T-005/S1 installed stable Platform 36 and Build Tools 36.0.0, created the single-module Java/XML shell under `android/`, generated a checksum-pinned Gradle 9.5.0 Wrapper, and resolved only the accepted S1 UI/test dependencies. The temporary verified Gradle distribution was removed.
- User accepted T-005/S1 with “accepted! whats next?” on 2026-09-04.
- The shell's unit test, lint, and debug assembly pass; lint has zero findings. APK metadata is application ID `rs.ac.ni.elfak.asap`, min SDK 23, and target SDK 36; no camera permission or scanner dependency exists and nothing was installed on the phone.
- User explicitly authorized T-004/S2 on 2026-09-04.
- User accepted T-004/S2 and completion of T-004 with “good, commit; what's next” on 2026-09-04.
- T-004/S2 froze the accepted Android PoC baseline in `docs/ANDROID_BASELINE.md` and D-012, then synchronized operational and formal documentation. No package, project, or dependency was downloaded.
- User approved the T-004 plan and explicitly authorized only T-004/S1 on 2026-09-04.
- User accepted T-004/S1 and its recommended candidate matrix on 2026-09-04. No SDK package, dependency, or project was created or downloaded at that checkpoint; the subsequently authorized S2 has now recorded the versions as durable decisions.
- User explicitly authorized T-003/S3 on 2026-09-04 after connecting a physical phone.
- User accepted T-003/S3 and completion of T-003 with “lgtm! whats next?” on 2026-09-04.
- T-003/S3 verified an authorized Samsung SM-A566B running Android 16/API 36 through ADB; enabled Google Play services 26.32.34 and rear/front camera, autofocus, and flash features were verified. No application was installed and no device serial number was recorded.
- Added repeatable physical-device setup and verification commands to `docs/WORKFLOW.md`; USB tethering must remain off for this wired ADB setup.
- User accepted T-003/S2 with “LGTM!” on 2026-09-04.
- Executed only approved T-003/S2: retained system OpenJDK 21, installed verified official Android Studio Quail 4 (2026.1.4), and installed the core Android 37 SDK toolchain under user-scoped paths.
- Verified Android CLI 1.0.16261425, Command-line Tools 23.0, Platform Tools 37.0.1, Android Platform 37.0 revision 2, Build Tools 37.0.0, ADB, AAPT2, Studio's bundled OpenJDK 25.0.3, and all command symlinks.
- Added reproducible Android environment commands to `docs/WORKFLOW.md`; no emulator, AVD, application project, or dependency was created.
- Excluded Android Studio and Gradle machine-local state from version control.
- Synchronized the formal technology sections, rebuilt the 8-page report with pdfLaTeX and LuaLaTeX and the 11-slide presentation with LuaLaTeX, found no warnings or missing glyphs, and visually inspected both changed pages.
- User accepted the completed T-003/S1 audit with “approved!” on 2026-09-04.
- Executed only approved T-003/S1 as a read-only audit: verified Ubuntu 24.04.4 and OpenJDK/JDK/`javac` 21.0.12, found no discoverable Android Studio or Android SDK tooling, and changed no host configuration.
- Confirmed ample disk and AMD-V support, but no KVM packages or `/dev/kvm`; visible RAM is below the current Android Studio plus Emulator minimum, so the proposed runtime path prefers a physical Play-enabled Android device.
- Documented the exact S2 gap list: retain OpenJDK 21, install the current stable official Android Studio build and its recommended SDK/platform tools, and defer emulator/KVM setup unless needed.
- User accepted T-002/S1 and closed T-002 with “accepted, cool!” on 2026-09-04.
- User accepted Java + XML Views + Google Code Scanner as the fastest initial Android MVP baseline and explicitly authorized T-002/S1 on 2026-09-04.
- Synchronized the accepted baseline across the task tracker, decision log, status, architecture, diagrams, report, presentation, plan, and handoff; no Android project or dependency was created.
- Regenerated and visually inspected all canonical diagram variants, compiled the 8-page report with pdfLaTeX and LuaLaTeX, compiled the 11-slide presentation with LuaLaTeX, and found no document warnings or missing Serbian glyphs.
- User accepted T-001/S4 and closed T-001 with “accepted, LGTM!” on 2026-09-04.
- Marked the canonical PlantUML TODO complete and recorded accepted localization decision D-009.
- Executed only approved T-001/S4: parameterized both canonical sources to generate English technical, Serbian formal, and compact Serbian presentation renders without duplicating architecture structure.
- Replaced the historical report image with the canonical Serbian component view, added the scan-to-recommendation view, and added both compact views to the Beamer deck.
- Verified all PlantUML variants, built the 8-page report with pdfLaTeX and LuaLaTeX, built the 11-slide presentation with LuaLaTeX, and visually inspected the integrated pages and slides.
- User accepted T-001/S3 on 2026-09-03; synchronized its accepted status and recorded decision D-008.
- Executed only approved T-001/S3: added the canonical proposed scan-to-recommendation sequence/data-flow source and a visually inspected 1306×1038 PNG render.
- Added sequence-diagram styling to the shared theme, regenerated the component PNG, and revalidated both canonical PlantUML sources.
- Included success, unreadable-barcode, unavailable/missing-metadata, and empty/unavailable-recommendation outcomes without selecting endpoints, schemas, retries, or technologies.
- User accepted T-001/S2 on 2026-09-03; synchronized its accepted status and recorded decision D-007.
- Committed the accepted planning/S1 baseline as `9ea0267` (`Add planning workflow and diagram contract`) before S2, as requested.
- Executed only approved T-001/S2: added shared PlantUML styling, canonical proposed component source, and a visually inspected 892×667 PNG render.
- Verified `component-architecture.puml` with headless PlantUML syntax checking and rendering; no flow source or formal-deliverable integration was created.
- User accepted T-001/S1 on 2026-09-03; synchronized its accepted status and recorded decision D-006.
- Executed only approved subtask T-001/S1: defined the diagram contract, canonical terminology, view boundaries, stable file layout, and reproducible PlantUML commands in `docs/diagrams/README.md`.
- Verified local diagram tooling: PlantUML 1.2020.02, Java 21, and Graphviz 2.43.0 work when invoked with `env -u DISPLAY`.
- Synchronized architecture, project status, plan state, and this handoff; no `.puml` source or application implementation was created.
- Strengthened documentation synchronization into a no-lag invariant: implementation, verification, and affected docs are one atomic subtask.
- Revised proposed plan T-001 so every subtask names and updates its affected documentation instead of deferring synchronization to S4.
- Added mandatory task-plan and per-subtask approval gates at the user's request.
- Added `docs/PLANS.md` to persist proposed plans and approval state across sessions.
- Initialized the cross-session documentation hub and root `AGENTS.md` instructions.
- Established sources of truth, definition of done, and documentation synchronization rules.
- Recorded the decisions already supported by the handwritten notes and repository setup.
- Captured the current architecture as an explicitly unvalidated hypothesis.
- Documented verified LaTeX invocation modes and the Serbian Latin font requirement.

## Current repository state

- Documentation scaffolding is operational.
- Formal report and presentation sources compile.
- The accepted MVP scope/iteration contract and concrete architecture contract are synchronized; T-006 is closed.
- A buildable Android Java/XML scanner-to-client slice exists with 27 passing local tests. The scanner itself remains physically verified; source-level backend coordination exists, but the end-to-end exchange has not yet been exercised on the phone.
- The backend baseline, I1 contract/fixtures, S3 implementation, and S4.1–S4.2 Android work are accepted; S4.3 is in progress. Product-data providers, models, storage products, and physical integration evidence remain open.

## Suggested next session

Complete and verify only the approved T-007/S4.3 outcome UI, then stop for acceptance. Do not execute S4.4 without explicit approval.

## Blockers

PlantUML must currently be invoked with `env -u DISPLAY` in this environment to avoid an inaccessible X11 display. Emulator acceleration remains unavailable because KVM packages and `/dev/kvm` are absent; a physical device is the preferred first runtime target.
