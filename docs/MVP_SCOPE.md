# MVP scope and iteration contract

Status: Accepted under T-006/S1 on 2026-09-05.

Last updated: 2026-09-05.

## Product outcome

The operational ASAP MVP lets a person scan a supported retail barcode on the Android client, see trustworthy product metadata, and receive a small ranked list of semantically similar products. It remains demonstrable with a controlled fallback dataset when an external metadata source is unavailable.

History-based personalization is a committed part of the project scope. It forms an extended MVP milestone after the fastest useful core path, so the core can be integrated and validated before interaction history is introduced. The MVP is a technical and user-flow validation, not a production retail service.

## Minimum user-visible journey

1. The user opens the Android application and starts a scan.
2. Google Code Scanner returns an EAN-13, EAN-8, UPC-A, or UPC-E value, or the client shows a clear cancellation/failure state.
3. The client requests product resolution from the backend boundary.
4. The system returns normalized product metadata from an external source or controlled fallback data, or reports that the product is unknown/unavailable.
5. For a known product, the system returns a ranked top-N semantic-similarity result with enough metadata to explain what each result is.
6. The client presents the scanned product and similar products without implying that similarity is a personalized recommendation.
7. In the extended MVP, recent interaction history influences the ranking when sufficient history exists. The client labels those results separately and otherwise uses an explicit cold-start/generic state.

## Scope scorecard

The “approximately 80%” target is an operational threshold, not permission to complete arbitrary low-value features. All P0 rows together form the required 80-point core and must work as one demonstrable path.

| Priority | Capability | Weight | Current evidence |
| --- | --- | ---: | --- |
| P0 | Physical barcode acquisition and cancellation | 15 | Implemented and device-validated under T-005 |
| P0 | Barcode-to-product metadata resolution with controlled fallback | 20 | Planned |
| P0 | Product details and honest unknown/unavailable UI states | 10 | Planned |
| P0 | Top-N semantic similarity for known products | 20 | Planned |
| P0 | Reproducible integration, resilience checks, demo, and synchronized deliverables | 15 | Partially established; end-to-end evidence pending |
| P1 | Extended MVP: history-based personalization with an explicit cold-start state | 15 | Committed after the core path |
| P2 | Broader quality/latency evaluation and user feedback | 5 | Planned after integration |

Core operational MVP acceptance requires all P0 capabilities, a reproducible phone demonstration of the complete scan-to-similar-products path, and no known documentation mismatch. Extended MVP acceptance additionally requires the P1 history-based ranking behavior, bringing planned capability coverage to 95 points. P2 completes the 100-point project scope through broader evaluation and feedback.

## Required behavior

- Preserve the validated permissionless Google Code Scanner boundary and custom Java/XML application UI.
- Keep product metadata, semantic similarity, and personalization as distinct responsibilities and distinct user-visible concepts.
- In the extended MVP, derive personalization from a bounded recent interaction history; a last-K profile is the baseline candidate, while the exact window, weighting, and aggregation method remain a later evidence-based decision.
- Preserve an explicit cold-start path that falls back to clearly labelled generic semantic similarity when history is absent or insufficient.
- Use a controlled development/fallback dataset so the primary demo does not depend entirely on a third-party service.
- Return explicit known, unknown, unavailable, empty-result, and retryable-failure outcomes rather than fabricating product or recommendation data.
- Keep the primary flow small enough to build, test, and demonstrate locally before production concerns are introduced.
- Record reproducible commands, evidence, limitations, and formal-deliverable updates in the same subtask as each implementation increment.

## Explicit non-goals for the operational MVP

- Authentication, accounts, multi-user synchronization, social features, payments, or shopping-cart behavior.
- Production publication, app-store release, public hosting, autoscaling, monitoring platforms, or service-level guarantees.
- A custom CameraX preview, continuous scanning, visual overlays, or barcode formats outside the accepted EAN/UPC set.
- Catalog administration, crowdsourced editing, inventory, price comparison, or guaranteed global product coverage.
- Clustering, PCA visualization, MMR diversification, conversational features, or generative product descriptions.
- Offline-first synchronization, background scanning, analytics tracking, notifications, or retention of barcode images.
- Finalized security/authentication schemes and production data-retention policy; these must be resolved before any real user data is retained.

## Assumptions and external dependencies

- The demonstration device has supported Google Play services and a working camera; T-005 validates this on the current phone.
- Network access is available for backend and external-provider calls, but the controlled fallback path remains available for the demo.
- Target products use EAN-13, EAN-8, UPC-A, or UPC-E barcodes.
- A legally usable product metadata source and representative fallback dataset can be selected in a later task.
- A later-selected embedding model can represent the languages and fields present in the chosen product data.
- Early development may treat one device as one anonymous user. Any durable interaction history requires an explicit privacy/retention decision first.
- The exact personalization algorithm is not selected by this scope contract. A bounded last-K history is the simplest candidate, but recency weighting or another measurable method may replace it through an accepted technical decision.
- Exact frameworks, providers, data schemas, model versions, and storage products remain intentionally open.

## Thin delivery iterations

Each iteration must end in reviewable evidence and synchronize tests, status, architecture, report, presentation, and handoff wherever affected.

| Iteration | Goal | Entry condition | Required exit evidence |
| --- | --- | --- | --- |
| I0 — Scanner foundation | Acquire a supported barcode on Android | Android baseline selected | Complete: clean build/test/lint, physical install, two real scans, cancellation |
| I1 — Deterministic vertical slice | Connect the phone flow through the backend boundary using controlled fixture data | T-006 architecture accepted; later backend baseline accepted | A scanned fixture barcode produces product details and a clearly labelled deterministic placeholder list; unknown fixture behavior and automated contract checks pass |
| I2 — Product resolution | Replace fixture-only resolution with a normalized metadata adapter plus fallback | Product source, fallback dataset, and product model accepted | Known and unknown real barcodes exercise primary/fallback/unavailable outcomes with provenance recorded |
| I3 — Semantic similarity | Generate/store embeddings and return ranked top-N similar products | Representative dataset, embedding model, and vector-storage approach accepted | Measured offline quality/latency baseline plus phone demonstration; empty/unavailable outcomes verified |
| I4 — Extended MVP personalization | Use bounded interaction history to distinguish generic from personalized ranking | Core operational MVP accepted; privacy/retention and interaction model accepted | Cold-start behavior and a repeatable controlled example where history changes ranking; automated tests pass and the selected history method is documented |
| I5 — Hardening and course demo | Consolidate resilience, evaluation, feedback, and deliverables | Core integrations complete | End-to-end regression run, documented limitations/metrics, instructor/team feedback when available, complete report and presentation |

I1 may use deterministic placeholder recommendations only as transport/UI scaffolding. They must be visibly labelled and must not be presented as semantic or personalized results. I3 is the point at which semantic-result claims become valid.

## Change control

- Adding or removing a P0/P1 capability, changing the 80-point core or 95-point extended-MVP threshold, or promoting a non-goal requires explicit user acceptance and documentation synchronization.
- A later technical incompatibility may revise iteration order, but it must not silently weaken the user-visible journey or evidence requirements.
- T-006/S2 may refine component placement and ownership while preserving this scope. Concrete technology/provider choices remain separately planned work.
