# Project status

Last verified: 2026-09-04

## Current phase

ASAP is in proposal and repository-preparation phase. There is no mobile application, backend, recommendation service, dataset pipeline, or automated test suite yet.

## Available artifacts

- A phased product plan in `TODO.md`, transcribed and normalized from `meditations/sept_3.pdf`.
- A Serbian LaTeX report in `report/report.tex`, based on the parent-directory DOCX.
- Report sections 2–5 are structured placeholders and must not be represented as completed work.
- The original architecture image from the DOCX is retained as historical source material at `report/assets/asap-architecture.png`; formal deliverables use canonical PlantUML renders.
- A Serbian ELFak-styled Beamer deck in `presentation/asap-presentation.tex`.
- The presentation contains proposal content and explicitly marked placeholders for design, implementation, and evaluation results.
- Bundled presentation theme assets under `presentation/theme/`.
- Root-level LaTeX ignores and cross-session operating instructions.
- An accepted canonical diagram specification in `docs/diagrams/README.md`.
- An accepted proposed logical component architecture in `docs/diagrams/component-architecture.puml`, shared styling in `docs/diagrams/includes/theme.puml`, and a verified PNG render. It documents intended structure and does not represent implemented software.
- An accepted proposed end-to-end scan-to-recommendation sequence/data-flow diagram in `docs/diagrams/scan-to-recommendation-flow.puml` with a verified PNG render. It includes success and high-level unavailable/empty outcomes without prescribing concrete APIs or retry policies.
- English technical, Serbian formal, and compact Serbian presentation renders are generated from the same two canonical PlantUML sources.
- The report embeds the Serbian component and scan-to-recommendation diagrams; the presentation embeds their slide-specific Serbian variants. T-001 is accepted and complete.
- The accepted initial Android baseline is Java application code, XML-based Android Views, and Google Code Scanner. This is a design decision; no Android project has been created yet.
- Direct ML Kit Barcode Scanning with CameraX remains an upgrade path only if the MVP later requires a custom scanner camera experience.

## Verified document builds

- `report/report.tex` compiles with both pdfLaTeX and LuaLaTeX, from the repository root or the `report/` directory.
- `presentation/asap-presentation.tex` compiles with LuaLaTeX, from the repository root or the `presentation/` directory.
- Serbian Latin glyphs render correctly with the engine-aware font setup.
- PlantUML 1.2020.02, Java 21, and Graphviz 2.43.0 are available in the current environment when PlantUML is invoked headlessly with `env -u DISPLAY`.
- The integrated report is 8 pages and the integrated presentation is 11 slides; their diagram pages/slides were visually inspected.

## Immediate product decisions still open

- Exact Android project structure and minimum supported SDK.
- Backend language/framework and service boundaries.
- Product metadata API and fallback dataset.
- Embedding model and vector-index implementation.
- Concrete MVP acceptance metrics and scope for the approximately 80% functionality target.

See `TODO.md` for ordered tasks and `docs/SESSION_HANDOFF.md` for the suggested next session.
