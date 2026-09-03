# Working workflow

## Session loop

1. **Orient:** read the files listed in `AGENTS.md` and inspect the working tree.
2. **Select:** choose a bounded task from `TODO.md`; identify its acceptance evidence before implementation.
3. **Implement:** keep changes scoped and update diagrams or formal documentation when the design changes.
4. **Verify:** run relevant builds, tests, or small experiments and retain concise evidence.
5. **Synchronize:** update TODO, project status, decisions, and handoff before ending the session.

## Definition of done

A task may be checked off only when:

- the requested artifact or behavior exists in the repository;
- relevant verification passes;
- the current-state documentation does not overstate what was built;
- new architectural or technology decisions are recorded;
- the next session can understand remaining work without relying on chat history.

## LaTeX builds

Run these commands from the repository root:

```sh
mkdir -p build

pdflatex -interaction=nonstopmode -halt-on-error \
  -output-directory=build report/report.tex
pdflatex -interaction=nonstopmode -halt-on-error \
  -output-directory=build report/report.tex

lualatex -interaction=nonstopmode -halt-on-error \
  -output-directory=build presentation/asap-presentation.tex
lualatex -interaction=nonstopmode -halt-on-error \
  -output-directory=build presentation/asap-presentation.tex
```

The report also supports LuaLaTeX. Two passes are used so tables of contents, references, and Beamer frame totals settle correctly.

## Documentation synchronization guide

| Change | Also update |
| --- | --- |
| Task completed or scope discovered | `TODO.md` |
| Capability or project phase changed | `docs/PROJECT_STATUS.md` |
| Technology, architecture, API, model, or process chosen | `docs/DECISIONS.md` |
| Architecture or data flow changed | `docs/ARCHITECTURE.md`, PlantUML source, report, presentation |
| User feedback or evaluation performed | report sections 4–5, presentation, project status |
| Any meaningful session ends | `docs/SESSION_HANDOFF.md` |
