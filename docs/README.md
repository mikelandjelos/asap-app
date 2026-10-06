# ASAP documentation hub

This directory contains the operational documentation needed to continue the project across independent work sessions.

| Document | Purpose |
| --- | --- |
| [`CURRENT_REQUIREMENTS.md`](CURRENT_REQUIREMENTS.md) | Authoritative D-024/D-025 mandatory scope, delivery order and approval boundaries |
| [`NOTEBOOK_VALIDATION.md`](NOTEBOOK_VALIDATION.md) | Required reproducible evidence for every AI/statistical component and report results |
| [`DOCUMENTATION_AUDIT.md`](DOCUMENTATION_AUDIT.md) | Handoff-wide document inventory, reconciliation and explicit deferred artifacts |
| [`PROJECT_STATUS.md`](PROJECT_STATUS.md) | Current, evidence-based repository state |
| [`REPORT_COMPLETION.md`](REPORT_COMPLETION.md) | Report draft markers, missing evidence, submission checklist and return to the full roadmap |
| [`WORKFLOW.md`](WORKFLOW.md) | Session workflow, definition of done, and build commands |
| [`PLANS.md`](PLANS.md) | Proposed/approved task plans and subtask approval state |
| [`DECISIONS.md`](DECISIONS.md) | Lightweight decision log |
| [`ARCHITECTURE.md`](ARCHITECTURE.md) | Current architecture hypothesis and unresolved questions |
| [`MVP_SCOPE.md`](MVP_SCOPE.md) | Accepted operational MVP boundary, scorecard, and thin delivery iterations |
| [`ANDROID_BASELINE.md`](ANDROID_BASELINE.md) | Researched Android PoC build/dependency candidates and acceptance points |
| [`BACKEND_BASELINE.md`](BACKEND_BASELINE.md) | Accepted T-007/S1 backend runtime, framework, build, and dependency baseline |
| [`I1_CONTRACT.md`](I1_CONTRACT.md) | Accepted T-007/S2 HTTP/JSON contract, fixture scenarios, and acceptance cases |
| [`DOMAIN_MODEL.md`](DOMAIN_MODEL.md) | Accepted T-008 product, interaction/history, recommendation, and AI-derived-artifact contract |
| [`PRODUCT_DATA_API_EVALUATION.md`](PRODUCT_DATA_API_EVALUATION.md) | T-009 provider criteria, official-source shortlist, probe corpus, and controlled evaluation protocol |
| [`V2_CONTRACT.md`](V2_CONTRACT.md) | Implemented AI v2 scan-query and catalog-map HTTP contract |
| [`AI_MVP_DESIGN.md`](AI_MVP_DESIGN.md) | T-011/S1 proposed AI MVP design: source router, dataset, embeddings, clustering, PCA, personalization, MMR, notebooks, UI |
| [`diagrams/README.md`](diagrams/README.md) | Proposed canonical diagram contract and file layout |
| [`SESSION_HANDOFF.md`](SESSION_HANDOFF.md) | Latest handoff for the next session |

Repository-level agent behavior is defined in [`../AGENTS.md`](../AGENTS.md). Product milestones live in [`../TODO.md`](../TODO.md). The formal deliverables remain the Serbian LaTeX report and Beamer presentation.

## Maintenance rule

Documentation changes are part of implementation, not a later cleanup task. Implementation, verification, and affected documentation form one atomic subtask. Update the relevant files in the same change that alters code, architecture, technology choices, data sources, results, or priorities. If documented state is stale, reconcile it before starting new implementation.
