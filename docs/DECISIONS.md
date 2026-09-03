# Decision log

Record accepted decisions here in chronological order. A decision is not a task: implementation may remain pending after a direction is accepted.

## D-001 — Canonical software diagrams use PlantUML

- **Status:** Accepted
- **Date:** 2026-09-03
- **Context:** The handwritten project notes require a maintainable primary medium for software diagrams.
- **Decision:** Use PlantUML sources for canonical architecture and software-design diagrams.
- **Consequence:** The existing DOCX architecture image is historical input; it should eventually be replaced or accompanied by versioned PlantUML source.

## D-002 — Mermaid is allowed for small Markdown-native diagrams

- **Status:** Accepted
- **Date:** 2026-09-03
- **Context:** Some simple flows are most useful when visible directly in repository documentation.
- **Decision:** Use Mermaid for small explanatory diagrams in Markdown, while keeping detailed software diagrams in PlantUML.

## D-003 — Formal deliverables use Serbian Latin

- **Status:** Accepted
- **Date:** 2026-09-03
- **Context:** The source report and course materials are in Serbian.
- **Decision:** Maintain the report and presentation in Serbian Latin. Configure fonts explicitly so pdfLaTeX/LuaLaTeX do not drop accented characters.

## D-004 — Documentation is synchronized during implementation

- **Status:** Accepted
- **Date:** 2026-09-03
- **Context:** Work is expected to continue across interchangeable sessions.
- **Decision:** Treat implementation, verification, and every affected documentation update as one atomic subtask. Synchronize TODO, plan state, current status, decisions, architecture, handoff, report, and presentation wherever the change applies.
- **Consequence:** Documentation may never knowingly lag behind implementation. A subtask is not complete if repository state cannot be reconstructed accurately from documentation. After an interruption or discovered mismatch, reconciliation blocks new implementation.

## D-005 — Work advances through explicit task and subtask approval gates

- **Status:** Accepted
- **Date:** 2026-09-03
- **Context:** The user wants to review and iterate on plans and retain control over every transition in the work sequence.
- **Decision:** Plan every reasonable task decomposition before implementation. Execute only one explicitly approved subtask at a time, stop after reporting its evidence, and require explicit approval before the next subtask or task.
- **Consequence:** Overall plan approval freezes the agreed direction but does not authorize all subtasks. Verification and documentation updates belong to the active subtask; material scope expansion requires replanning.

## D-006 — Canonical diagram contract and file layout

- **Status:** Accepted
- **Date:** 2026-09-03
- **Context:** T-001 requires maintainable architecture and flow diagrams that remain honest about the absence of implementation and unresolved technology choices.
- **Decision:** Use the two-view PlantUML contract, canonical terminology, boundaries, headless rendering commands, and stable source/render layout defined in `docs/diagrams/README.md`.
- **Consequence:** S2 and S3 must implement that contract without choosing unresolved technologies. PNG renders are portable deliverables, while `.puml` files remain canonical.

## D-007 — Proposed logical component view

- **Status:** Accepted
- **Date:** 2026-09-03
- **Context:** T-001/S2 translated the proposal into a maintainable logical component view without selecting unresolved implementation technologies.
- **Decision:** Accept `docs/diagrams/component-architecture.puml` and its PNG render as the canonical proposed component architecture.
- **Consequence:** Future design changes must update the PlantUML source, render, architecture documentation, report, presentation, plan state, and handoff wherever affected. The view remains design intent, not evidence of implemented software.

## D-008 — Proposed scan-to-recommendation flow

- **Status:** Accepted
- **Date:** 2026-09-03
- **Context:** T-001/S3 documents the intended end-to-end exchange and important user-visible outcome classes without inventing unselected API or recovery details.
- **Decision:** Accept `docs/diagrams/scan-to-recommendation-flow.puml` and its PNG render as the canonical proposed scan-to-recommendation flow.
- **Consequence:** The flow distinguishes unreadable barcode, unavailable/missing metadata, and empty/unavailable recommendations. Concrete endpoints, payloads, retries, frameworks, and timing guarantees remain undecided.

## D-009 — Localized diagram variants share canonical sources

- **Status:** Accepted
- **Date:** 2026-09-04
- **Context:** The same proposed architecture must support English technical review and readable Serbian report and presentation outputs without creating divergent diagram definitions.
- **Decision:** Generate English technical, Serbian formal, and compact Serbian presentation renders from the same two canonical PlantUML sources through render-time flags.
- **Consequence:** Localization and presentation simplification may change labels and secondary diagram furniture only. Components, participants, messages, and outcomes must remain structurally consistent across variants.
