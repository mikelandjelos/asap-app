# Task plans and approval state

This file records the active and recent task plans. The approval protocol is mandatory and defined in `AGENTS.md` and `docs/WORKFLOW.md`. Every subtask plan must name its affected documentation; synchronizing those files is part of that subtask, never deferred to task closure.

## T-001 — Add canonical PlantUML architecture and data-flow sources

- **TODO source:** “Dodati izvorne PlantUML dijagrame arhitekture i tokova podataka.”
- **Status:** Plan approved; S1 and S2 accepted; S3 awaiting explicit execution approval
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
- **Evidence:** versioned `.puml` source that parses and renders successfully.
- **Status:** Proposed; prerequisite S2 accepted; awaiting separate explicit approval of S3.

#### T-001/S4 — Validate and integrate the diagrams

- Render both sources using a documented reproducible command.
- Link or embed appropriate outputs in `docs/ARCHITECTURE.md`, the report, and the presentation without overstating implementation status.
- Synchronize the formal report and presentation, then finalize task-level state in `TODO.md`, `docs/PROJECT_STATUS.md`, `docs/DECISIONS.md` if needed, `docs/PLANS.md`, and `docs/SESSION_HANDOFF.md`. This does not replace the per-subtask documentation updates required in S1–S3.
- **Evidence:** successful renders, successful LaTeX builds, and consistent documentation references.
- **Status:** Proposed; blocked by acceptance of S3 and explicit approval of S4.

### Task-level acceptance criteria

- Canonical component and end-to-end flow diagrams exist as readable PlantUML source.
- Rendering is reproducible and documented.
- Diagrams agree with the current proposal or explicitly expose unresolved differences.
- Formal and operational documentation consistently labels the architecture as proposed.
- The user explicitly accepts T-001 before any later TODO task is planned or executed.

### Out of scope

- Choosing Java versus Kotlin.
- Selecting backend frameworks, external APIs, datasets, embedding models, or vector databases.
- Scaffolding application code.
- Treating a diagram element as evidence of implementation.
