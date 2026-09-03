# ASAP documentation hub

This directory contains the operational documentation needed to continue the project across independent work sessions.

| Document | Purpose |
| --- | --- |
| [`PROJECT_STATUS.md`](PROJECT_STATUS.md) | Current, evidence-based repository state |
| [`WORKFLOW.md`](WORKFLOW.md) | Session workflow, definition of done, and build commands |
| [`DECISIONS.md`](DECISIONS.md) | Lightweight decision log |
| [`ARCHITECTURE.md`](ARCHITECTURE.md) | Current architecture hypothesis and unresolved questions |
| [`SESSION_HANDOFF.md`](SESSION_HANDOFF.md) | Latest handoff for the next session |

Repository-level agent behavior is defined in [`../AGENTS.md`](../AGENTS.md). Product milestones live in [`../TODO.md`](../TODO.md). The formal deliverables remain the Serbian LaTeX report and Beamer presentation.

## Maintenance rule

Documentation changes are part of implementation, not a later cleanup task. Update the relevant files in the same change that alters code, architecture, technology choices, data sources, results, or priorities.
