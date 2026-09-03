# Working workflow

## Session loop

1. **Orient:** read the files listed in `AGENTS.md` and inspect the working tree.
2. **Select:** identify the first pending task in `TODO.md`, unless the user explicitly chooses another task.
3. **Plan:** divide the task into reasonable subtasks; define deliverables, acceptance evidence, verification, dependencies, and exclusions in `docs/PLANS.md`.
4. **Review:** iterate on the plan with the user. Do not implement anything while the plan is merely proposed.
5. **Approve:** record explicit task-plan approval. Obtain separate explicit approval for the first subtask before executing it.
6. **Execute one subtask:** keep changes within the approved scope.
7. **Verify and synchronize:** perform the subtask's checks and update relevant documentation as part of that same subtask.
8. **Stop:** report evidence and wait for explicit approval before starting the next subtask.
9. **Close:** after the final subtask, ask the user to accept the task before considering any subsequent TODO item.

## Approval-state vocabulary

- **Proposed:** open for discussion; no implementation is authorized.
- **Plan approved:** the task decomposition is accepted, but subtasks still require individual execution approval.
- **Subtask approved:** exactly the named subtask may be executed.
- **Awaiting acceptance:** implementation and verification are complete; user confirmation is required before progressing.
- **Accepted:** the user accepted the subtask or task result.

Do not infer approval from silence, general encouragement, or approval of a different task/subtask.

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
| Task/subtask planned, approved, completed, or accepted | `docs/PLANS.md` |
| Capability or project phase changed | `docs/PROJECT_STATUS.md` |
| Technology, architecture, API, model, or process chosen | `docs/DECISIONS.md` |
| Architecture or data flow changed | `docs/ARCHITECTURE.md`, PlantUML source, report, presentation |
| User feedback or evaluation performed | report sections 4–5, presentation, project status |
| Any meaningful session ends | `docs/SESSION_HANDOFF.md` |
