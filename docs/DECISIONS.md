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
- **Decision:** Treat TODO, current status, decisions, and handoff updates as part of the definition of done for meaningful changes.
- **Consequence:** A task is not complete if the repository state cannot be reconstructed from its documentation.
