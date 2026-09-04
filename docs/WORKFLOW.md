# Working workflow

## Session loop

1. **Orient:** read the files listed in `AGENTS.md` and inspect the working tree.
2. **Select:** identify the first pending task in `TODO.md`, unless the user explicitly chooses another task.
3. **Plan:** divide the task into reasonable subtasks; define deliverables, acceptance evidence, verification, affected documentation, dependencies, and exclusions in `docs/PLANS.md`.
4. **Review:** iterate on the plan with the user. Do not implement anything while the plan is merely proposed.
5. **Approve:** record explicit task-plan approval. Obtain separate explicit approval for the first subtask before executing it.
6. **Execute one subtask:** keep changes within the approved scope.
7. **Verify and synchronize:** perform the subtask's checks and update every affected documentation surface as part of that same atomic subtask.
8. **Stop:** report evidence and wait for explicit approval before starting the next subtask.
9. **Close:** after the final subtask, ask the user to accept the task before considering any subsequent TODO item.

## Approval-state vocabulary

- **Proposed:** open for discussion; no implementation is authorized.
- **Plan approved:** the task decomposition is accepted, but subtasks still require individual execution approval.
- **Subtask approved:** exactly the named subtask may be executed.
- **Awaiting acceptance:** implementation and verification are complete; user confirmation is required before progressing.
- **Accepted:** the user accepted the subtask or task result.

Do not infer approval from silence, general encouragement, or approval of a different task/subtask.

## No-documentation-lag invariant

- No implementation subtask is complete while its documentation is stale.
- Documentation synchronization happens per subtask, not only at task closure.
- Each plan must list affected documentation before implementation is approved.
- If work is interrupted between implementation and documentation, the next action is reconciliation; new implementation remains blocked.
- Use repository evidence to correct stale claims immediately. Never preserve a known inaccuracy merely because it was previously documented.

## Definition of done

A task may be checked off only when:

- the requested artifact or behavior exists in the repository;
- relevant verification passes;
- every affected documentation surface exactly reflects what was built, including limitations and unimplemented behavior;
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

## Android workstation

The verified user-scoped installation uses:

- Android Studio: `/home/mih/.local/opt/android-studio`
- Android SDK: `/home/mih/Android/Sdk`
- command symlinks: `/home/mih/.local/bin`

No global Android environment variable is required. Use the explicit SDK root for reproducible package queries, and disable Android CLI metrics in scripted checks:

```sh
java -version
javac -version
studio --version
android --no-metrics --version
android --no-metrics --sdk=/home/mih/Android/Sdk sdk list
adb version
```

`sdkmanager` remains available for compatibility but reports itself deprecated; prefer the `android sdk` commands. Do not install standalone Gradle globally: the future project must commit and use its Gradle Wrapper. Emulator/AVD commands are intentionally absent until T-003/S3 establishes a runtime target.

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
