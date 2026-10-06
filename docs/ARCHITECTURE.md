# Architecture

Scope amendment (2026-10-06, D-024): clustering, personalization, PCA, MMR and polished UI are mandatory MVP capabilities under D-024/D-025. Every AI/statistical stage requires notebook evidence; see `CURRENT_REQUIREMENTS.md` and `NOTEBOOK_VALIDATION.md`. T-011/S1 proposes the AI MVP design in [`AI_MVP_DESIGN.md`](AI_MVP_DESIGN.md) (multi-provider adapter/router, offline OFF catalog, ONNX embeddings, in-memory exact search, k-means, PCA map, history profile, MMR, Material 3 UI); the component diagram and new [recommendation-pipeline](diagrams/recommendation-pipeline.puml) view reflect it, pending S1 acceptance. The scan-to-recommendation, domain and AI-lineage views still show the earlier design and are updated when S6 revises the contract. Finish the PoC/MVP on Google Code Scanner, then replace scanner-related components only. Implementation below is unchanged.

Status: T-006/S2 architecture contract accepted on 2026-09-05. The Android scanner, coordinator, API client, I1 outcome UI, and deterministic fixture-backed backend are implemented and physically validated together. T-008/S2's domain/AI-lineage refinement was accepted on 2026-09-08; all genuine data/AI components remain planned.

T-007/S3 realizes the accepted Java 21, Spring Boot 4.1.1, Servlet Spring MVC, Maven Wrapper baseline in one project under `backend/`. The deployment and ownership boundaries below remain unchanged.

The accepted product boundary is in [`MVP_SCOPE.md`](MVP_SCOPE.md). T-008/S1 accepts the source-neutral product aggregate and normalization rules in [`DOMAIN_MODEL.md`](DOMAIN_MODEL.md) without changing the implemented I1 contract; S2 accepts its interaction, recommendation, and derived-artifact extension. Canonical views are the [component/deployment](diagrams/component-architecture.puml), [scan-to-recommendation](diagrams/scan-to-recommendation-flow.puml), [domain-model](diagrams/domain-model.puml), and [AI-enrichment](diagrams/ai-enrichment-flow.puml) sources; their rendering and terminology contract is in [`diagrams/README.md`](diagrams/README.md).

## Selected MVP topology

ASAP has two application deployment boundaries:

1. One Android application runs on the user's device. It owns all user-facing state, launches Google Code Scanner, calls the backend, and—in the required target MVP—owns a bounded local interaction history.
2. One backend application runs as a modular monolith. Its API, product-resolution, and recommendation modules execute in the same deployable unit and communicate through internal module contracts.

The recommendation logic is deliberately not a separately deployed service for the MVP. This minimizes build, deployment, network, and observability work while preserving an internal boundary that can be extracted later if measured load or independent evolution justifies it.

The external product source is outside ASAP's trust and availability boundary. A controlled fallback dataset is a backend-side input. The normalized product catalog and vector index are separate logical stores owned by backend modules; S2 does not require separate database products or processes.

## Implemented Android slice

- A single Gradle application module lives under `android/app` with namespace and application ID `rs.ac.ni.elfak.asap`.
- `MainActivity` is Java 17 code and renders a custom XML `ConstraintLayout` screen through AppCompat, with a scan action, current status, and decoded result.
- Google Code Scanner 16.1.0 handles EAN-13, EAN-8, UPC-A, and UPC-E with auto-zoom. Google Play services owns the scanner camera experience; ASAP declares no camera permission.
- Success, cancellation, empty value, module/download unavailability, and general failure have implemented user-visible states. The complete Android slice has 32 passing local unit tests and zero lint findings.
- The debug APK is installed on the verified phone. Two real-product scans and cancellation were confirmed.
- The `network` package implements an app-owned `ScanQueryClient`, I1 DTOs/invariant validation, EAN/UPC format mapping, and failure classification through Retrofit/Moshi/OkHttp. Calls are cancellable and conversion callbacks stay off the UI thread.
- The pure-Java `ScanQueryCoordinator` accepts only non-empty supported scan results, invokes the client, dispatches visible callbacks through the main-thread executor, cancels active work on a newer scan or activity destruction, and rejects stale callbacks. `MainActivity` exposes loading plus distinct transport, HTTP, and invalid-response states.
- The debug base URL is `http://127.0.0.1:8080/` for `adb reverse`; only the debug manifest permits cleartext. The release manifest denies cleartext and carries no usable production endpoint.
- `MainActivity` constructs the client and invokes it through the coordinator. Its scrollable XML outcome area renders normalized known-product data, unknown/unavailable product states, and independent result/empty/unavailable/not-applicable recommendation states. Placeholder results always carry the required non-AI label and never show a score. Application persistence, live product lookup, vector search, and genuine recommendation display do not exist yet.
- The complete debug I1 path was physically validated through ADB reverse. The phone displayed the controlled known, unknown, empty-result, recommendation-unavailable, and backend-unavailable cases with the required partial-success behavior.

## Implemented deterministic backend slice

- One executable Spring Boot application lives under `backend/` and preserves internal API, application, product-resolution, and recommendation package boundaries.
- `POST /api/v1/scan-queries` validates EAN-13, EAN-8, UPC-A, and UPC-E input, rejects bodies over 2 KiB, and returns the frozen independent outcome envelope or RFC 9457 problem details.
- Product resolution and result ordering read the canonical packaged I1 fixture. Results are always labelled `DETERMINISTIC_FIXTURE` and contain no similarity score.
- Eleven barcode-rule tests and twelve full HTTP contract tests pass; the packaged executable JAR was started and smoke-tested.
- There is no external adapter call, database, vector index, embedding, personalization or production deployment. The separate Android network client is implemented.

The diagrams mark scanner integration, Android coordination/API boundary and outcome UI, deterministic backend contract, and their controlled physical exchange as implemented. Genuine metadata/vector flows remain design intent.

## Component responsibilities and ownership

| Component | Responsibility | Persistent data ownership | Primary inputs | Primary outputs |
| --- | --- | --- | --- | --- |
| Android UI and flow coordinator | Starts scanning, requests resolution/ranking, renders product and independently labelled recommendation states | User-facing transient state only | User action, scanner outcome, backend outcome | Scan, product, generic/personalized, empty, and retryable states |
| Scanner integration | Adapts Google Code Scanner outcomes to ASAP's flow | None; barcode images are not retained | Scan request | Decoded EAN/UPC value or classified scanner outcome |
| Android API client | Crosses the device/backend boundary and preserves independent product/recommendation outcome classes | None | Barcode and optional bounded history context | Product outcome plus recommendation mode/results/status |
| Local bounded history | Supplies newest-first anonymous known-product interactions for the required MVP and explicit cold start when insufficient | Android application on the device | Confirmed displayed known product | Optional bounded `HistoryContext`; no account/device identity |
| Backend API module | Validates and coordinates one application operation and combines module results without hiding partial success | None | Barcode and optional history context | Product outcome and separate recommendation outcome |
| Product-resolution module | Checks the normalized catalog, consults the external adapter when appropriate, applies controlled fallback data, normalizes records against the accepted T-008/S1 product model, and reports provenance | Owns writes to the normalized product catalog | Barcode, external/fallback records | Known product with provenance, unknown product, or temporarily unavailable |
| Recommendation module | Produces generic semantic similarity or history-aware ranking, labels the mode, and handles cold start | Owns vector preparation/index synchronization at the logical level | Current known product and optional recent product references | Ranked candidates with scores/mode, empty result, or unavailable status |
| Normalized product catalog | Provides stable barcode-to-product records independent of source-specific formats | Backend/product-resolution module | Normalized product writes and barcode lookups | Product record and provenance |
| Vector index | Provides product-vector lookup and similarity candidates | Backend/recommendation module | Product vectors and similarity queries | Candidate product references and scores |
| External metadata adapter/source | Supplies potentially incomplete or unavailable third-party product data | External party; ASAP retains only normalized records it chooses to cache/import | Barcode lookup | Source record, missing result, or unavailable outcome |
| Controlled fallback dataset | Keeps development, automated checks, and the primary demo reproducible | Repository/backend preparation process once selected | Curated product fixtures | Deterministic source records |

“Owns” identifies the component allowed to write and interpret a data set. It does not select a database, file format, ORM, API provider, or concrete schema.

## Runtime contract

The accepted application-facing exchange remains conceptual at the architecture level:

- Request information: one decoded supported barcode plus optional bounded recent product references.
- Product outcome: exactly one of `known` (normalized product and provenance), `unknown`, or `unavailable`.
- Recommendation outcome for a known product: exactly one of `personalized`, `generic`, `empty`, or `unavailable`; results carry product references and ranking information.
- Partial-success rule: an empty or unavailable recommendation result must not discard known product details.
- History rule: absent or insufficient history yields explicitly labelled generic semantic similarity. The Android application records only confirmed known-product interactions.

T-007/S2 accepts and freezes the exact deterministic-I1 subset in [`I1_CONTRACT.md`](I1_CONTRACT.md):

- `POST /api/v1/scan-queries` accepts one scanner-provided EAN/UPC value and format; optional history remains deferred.
- Valid queries return HTTP `200` with independent `product` and `recommendations` outcomes. Product status is `KNOWN`, `UNKNOWN`, or `UNAVAILABLE`; recommendation status is `RESULTS`, `EMPTY`, `UNAVAILABLE`, or `NOT_APPLICABLE`.
- Every I1 recommendation for a known product uses mode `DETERMINISTIC_FIXTURE` and `placeholder: true`; it carries no score and must be shown as a non-AI demo result.
- Invalid requests use RFC 9457 problem details. Transport failure remains an Android-side backend-unavailable outcome, not a fabricated domain response.
- Controlled fixtures use restricted-circulation EAN-13 codes and `CONTROLLED_FIXTURE` provenance; they may never be queried against an external provider or represented as real products.
- The accepted T-008/S1 model separates an opaque stable product ID from exact barcode value/format lookup identity; requires only ID, barcode, name, and provenance; and bounds optional brand, category, description, and tags. It is an internal model contract, not an I1 payload change.

Endpoint evolution, retries, timing, history context, and production guarantees remain deferred. S4.2 connects successful supported scans to the client with cancellation and stale-response protection, S4.3 renders the independent response outcomes, and S4.4 physically validates the controlled exchange on the phone.

### Accepted T-008/S2 domain refinement

- Android records only `PRODUCT_VIEWED` after displaying a known product. Each event contains an opaque event ID, product ID, and device UTC time; it contains no barcode, product snapshot, account/device ID, location, free text, or inferred preference.
- Optional `HistoryContext` is newest-first, client-bounded, deduplicated by event ID, and request-scoped. The backend derives `COLD_START` or `SUFFICIENT`; it does not persist a profile. Out-of-order, malformed, duplicate, or over-limit context must be rejected by a future versioned API rather than silently sorted or reinterpreted.
- Recommendation mode is `DETERMINISTIC_FIXTURE` (not AI), `GENERIC_SEMANTIC` (AI-derived cold start), or `PERSONALIZED_HISTORY` (AI-derived with sufficient history). Status remains independent: `RESULTS`, `EMPTY`, `UNAVAILABLE`, or `NOT_APPLICABLE`.
- AI-ranked items use contiguous ranks and unique candidate product IDs. Real AI modes require finite typed score evidence plus a model/pipeline version, but scores are comparable only inside one response with matching semantics and must not be displayed as percentages without calibration.
- `ProductEmbedding`, request-scoped `HistoryProfile`, and ranking evidence are planned derived artifacts. They never overwrite source-backed `Product`, `Barcode`, or `Provenance` facts.
- Numeric K/window, sufficiency threshold, retention duration, persistence, model, vector store, similarity metric, and ranking algorithm remain separate evidence-based decisions.

## Failure boundaries

| Failure location | Responsible component | Required visible behavior |
| --- | --- | --- |
| Scan cancelled, unreadable, empty, or scanner module unavailable | Android scanner integration/UI | Remain on-device and show the already defined scanner state; make no product claim |
| Device cannot reach the backend or receives no usable response | Android API client/UI | Show a retryable backend-unavailable state; do not invent cached product/recommendation data |
| External source missing a barcode | Product-resolution module | Use an applicable catalog/fallback record, otherwise return `unknown` |
| External source unavailable | Product-resolution module | Use an applicable catalog/fallback record, otherwise return `unavailable`, distinct from `unknown` |
| Product known but vector candidates empty or search unavailable | Recommendation module/API | Return product details with independent `empty` or `unavailable` recommendation status |
| History absent or policy-insufficient | Android history boundary and recommendation module | Return clearly labelled generic/cold-start results |
| History out of order, malformed, duplicate, unsupported, or over its future limit | Backend API boundary | Reject through stable validation detail; do not silently sort, reinterpret, or claim personalization |

The exact retry policy and validation rules belong to later API/data-model work. Controlled fallback use must be visible through provenance; it must not masquerade as a live provider result.

## Personalization and privacy boundary

History-based ranking is a mandatory MVP capability, but there is no account system or central user profile. The Android application owns a bounded history of known product references and supplies it as optional request context. The backend computes the ranking for that request and does not retain the user history.

A last-K window remains the simplest candidate. T-008/S2 accepts a single `PRODUCT_VIEWED` event and a newest-first bounded request context, but K, sufficiency threshold, recency weighting, profile aggregation, deletion controls, persistence mechanism, and retention duration remain open. Before real user history is retained, the project must accept a privacy/retention decision; deterministic synthetic history may be used earlier for architecture and ranking tests.

## Trust boundaries and constraints

- Barcode images remain inside the Google Code Scanner experience; ASAP receives only the decoded value/outcome.
- Barcode values and optional history references cross from the device to the backend and must be treated as untrusted input.
- External metadata is untrusted and must pass normalization before entering the product catalog.
- The MVP has no account/authentication boundary, public-hosting commitment, or cross-device synchronization.
- Product metadata and vector data may share one physical storage technology later, but their logical ownership and consistency rules remain separate.

## Still open

- Hosting beyond local execution and any later evolution of the implemented package layout.
- Product API/provider, controlled fallback dataset, license, source precedence, attribution details, and caching policy.
- Embedding model/version, text composition, vector dimensions, exact versus approximate search, and update strategy.
- Personalization K/window, events, weighting, aggregation, retention, deletion, and evaluation.
- Concrete resilience policy, timeouts, retries, observability, security hardening, and production operation.
- Whether later UX evidence justifies replacing Google Code Scanner with direct ML Kit Barcode Scanning and CameraX.

The historical image at `report/assets/asap-architecture.png` remains source material only. Canonical PlantUML renders replace it in formal deliverables.
