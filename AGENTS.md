# ASAP agent instructions

These instructions apply to the entire repository. They exist so a new human or agent session can resume work without reconstructing project state from chat history.

## Start every session

Read these files in order before changing the project:

1. `README.md` — project purpose and entry points.
2. `docs/PROJECT_STATUS.md` — what actually exists now.
3. `TODO.md` — ordered work still to do.
4. `docs/DECISIONS.md` — accepted technical and process decisions.
5. `docs/SESSION_HANDOFF.md` — the latest context, verification, and suggested next action.

Inspect the working tree before editing. Existing changes belong to the user unless clearly produced by the current task; preserve unrelated work.

## Sources of truth

- `TODO.md` is the task and milestone source of truth.
- `docs/PROJECT_STATUS.md` describes implemented reality, not intent.
- `docs/DECISIONS.md` records accepted decisions and their rationale.
- `docs/SESSION_HANDOFF.md` is short-lived operational context for the next session.
- `report/report.tex` is the formal Serbian project report.
- `presentation/asap-presentation.tex` is the evolving Serbian presentation.

When these disagree, do not silently choose one. Verify the repository, correct stale documentation, and record any real decision that resolves the discrepancy.

## Working rules

- Select work from `TODO.md`, or add an explicitly requested task there before or alongside implementation.
- Mark a checkbox complete only when the repository contains evidence and relevant verification has passed.
- Do not mark recurring documentation work permanently complete.
- Keep planning claims separate from implemented functionality. The repository currently has documentation scaffolding but no application code.
- Prefer small, reviewable changes. Do not introduce a framework, service, API, data source, or model without documenting the decision.
- Use PlantUML for canonical software diagrams. Mermaid is acceptable for small Markdown-native diagrams.
- Keep formal report and presentation content in Serbian Latin. Operational developer documentation may remain in English.
- Preserve source files; generated LaTeX auxiliaries are ignored. Commit PDFs only when they are intentional deliverables.

## Finish every meaningful session

In the same change as the implementation:

1. Update completed or newly discovered tasks in `TODO.md`.
2. Update `docs/PROJECT_STATUS.md` if repository capabilities changed.
3. Append accepted decisions to `docs/DECISIONS.md` when applicable.
4. Refresh `docs/SESSION_HANDOFF.md` with what changed, verification performed, blockers, and the next concrete action.
5. Update the report and presentation when the work changes project requirements, architecture, technology selection, implementation results, or evaluation results.

## Verification

Run checks proportional to the change. Documentation changes must at least validate links/paths manually and compile affected LaTeX documents when their sources or assets change. Build commands are documented in `docs/WORKFLOW.md`.
