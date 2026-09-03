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
