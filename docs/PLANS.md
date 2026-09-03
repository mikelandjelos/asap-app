# Task plans and approval state

This file records the active and recent task plans. The approval protocol is mandatory and defined in `AGENTS.md` and `docs/WORKFLOW.md`.

## T-001 — Add canonical PlantUML architecture and data-flow sources

- **TODO source:** “Dodati izvorne PlantUML dijagrame arhitekture i tokova podataka.”
- **Status:** Proposed — awaiting plan review and approval
- **Goal:** Replace the current image-only architectural knowledge with maintainable, version-controlled PlantUML sources without claiming that the proposed architecture has been implemented.

### Proposed subtasks

#### T-001/S1 — Define the diagram contract and file layout

- Specify the diagrams, intended audiences, notation, naming, boundaries, and source/rendered-file layout.
- Reconcile terminology between the DOCX-derived diagram, `docs/ARCHITECTURE.md`, report, and presentation.
- Record unresolved design points as questions rather than silently deciding them.
- **Evidence:** an approved diagram specification in `docs/diagrams/README.md`.
- **Status:** Proposed; not approved for execution.

#### T-001/S2 — Create the component architecture source

- Add a PlantUML component diagram covering the Android client, backend API, semantic/recommendation component, product metadata store, and vector index.
- Label the diagram as proposed architecture.
- **Evidence:** versioned `.puml` source that parses and renders successfully.
- **Status:** Proposed; blocked by acceptance of S1 and explicit approval of S2.

#### T-001/S3 — Create the end-to-end data-flow source

- Add a PlantUML diagram for the path from camera/barcode acquisition through metadata lookup, semantic retrieval, and recommendation display.
- Show relevant failure/empty-result boundaries only if included in the approved S1 contract.
- **Evidence:** versioned `.puml` source that parses and renders successfully.
- **Status:** Proposed; blocked by acceptance of S2 and explicit approval of S3.

#### T-001/S4 — Validate and integrate the diagrams

- Render both sources using a documented reproducible command.
- Link or embed appropriate outputs in `docs/ARCHITECTURE.md`, the report, and the presentation without overstating implementation status.
- Synchronize `TODO.md`, `docs/PROJECT_STATUS.md`, `docs/DECISIONS.md` if needed, and `docs/SESSION_HANDOFF.md`.
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
