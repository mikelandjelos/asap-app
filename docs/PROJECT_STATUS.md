# Project status

Last verified: 2026-09-03

## Current phase

ASAP is in proposal and repository-preparation phase. There is no mobile application, backend, recommendation service, dataset pipeline, or automated test suite yet.

## Available artifacts

- A phased product plan in `TODO.md`, transcribed and normalized from `meditations/sept_3.pdf`.
- A Serbian LaTeX report in `report/report.tex`, based on the parent-directory DOCX.
- Report sections 2–5 are structured placeholders and must not be represented as completed work.
- The original architecture image from the DOCX is stored at `report/assets/asap-architecture.png`.
- A Serbian ELFak-styled Beamer deck in `presentation/asap-presentation.tex`.
- The presentation contains proposal content and explicitly marked placeholders for design, implementation, and evaluation results.
- Bundled presentation theme assets under `presentation/theme/`.
- Root-level LaTeX ignores and cross-session operating instructions.
- An accepted canonical diagram specification in `docs/diagrams/README.md`; it reconciles terminology and file layout but contains no `.puml` implementation yet.

## Verified document builds

- `report/report.tex` compiles with both pdfLaTeX and LuaLaTeX, from the repository root or the `report/` directory.
- `presentation/asap-presentation.tex` compiles with LuaLaTeX, from the repository root or the `presentation/` directory.
- Serbian Latin glyphs render correctly with the engine-aware font setup.
- PlantUML 1.2020.02, Java 21, and Graphviz 2.43.0 are available in the current environment when PlantUML is invoked headlessly with `env -u DISPLAY`.

## Immediate product decisions still open

- Java versus Kotlin for the Android client.
- Exact Android project structure and minimum supported SDK.
- Backend language/framework and service boundaries.
- Product metadata API and fallback dataset.
- Embedding model and vector-index implementation.
- Concrete MVP acceptance metrics and scope for the approximately 80% functionality target.

See `TODO.md` for ordered tasks and `docs/SESSION_HANDOFF.md` for the suggested next session.
