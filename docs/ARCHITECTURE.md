# Architecture

Status: T-006/S2 architecture contract accepted on 2026-09-05. The Android scanner slice is implemented and device-validated. Every other component described below remains planned.

T-007/S1 selects Java 21, Spring Boot 4.1.1 with Servlet Spring MVC, Maven 3.9.16 through Maven Wrapper 3.3.3, and one project under `backend/`. This accepted technical baseline is not implemented until T-007/S3; the deployment and ownership boundaries below remain unchanged.

The accepted product boundary is in [`MVP_SCOPE.md`](MVP_SCOPE.md). Canonical views are the [component/deployment source](diagrams/component-architecture.puml) and [scan-to-recommendation source](diagrams/scan-to-recommendation-flow.puml); the rendering and terminology contract is in [`diagrams/README.md`](diagrams/README.md).

## Selected MVP topology

ASAP has two application deployment boundaries:

1. One Android application runs on the user's device. It owns all user-facing state, launches Google Code Scanner, calls the backend, and—only in the extended MVP—owns a bounded local interaction history.
2. One backend application runs as a modular monolith. Its API, product-resolution, and recommendation modules execute in the same deployable unit and communicate through internal module contracts.

The recommendation logic is deliberately not a separately deployed service for the MVP. This minimizes build, deployment, network, and observability work while preserving an internal boundary that can be extracted later if measured load or independent evolution justifies it.

The external product source is outside ASAP's trust and availability boundary. A controlled fallback dataset is a backend-side input. The normalized product catalog and vector index are separate logical stores owned by backend modules; S2 does not require separate database products or processes.

## Implemented Android slice

- A single Gradle application module lives under `android/app` with namespace and application ID `rs.ac.ni.elfak.asap`.
- `MainActivity` is Java 17 code and renders a custom XML `ConstraintLayout` screen through AppCompat, with a scan action, current status, and decoded result.
- Google Code Scanner 16.1.0 handles EAN-13, EAN-8, UPC-A, and UPC-E with auto-zoom. Google Play services owns the scanner camera experience; ASAP declares no camera permission.
- Success, cancellation, empty value, module/download unavailability, and general failure have implemented user-visible states. Seven local unit tests and Android lint pass.
- The debug APK is installed on the verified phone. Two real-product scans and cancellation were confirmed.
- No API client, application persistence, product lookup, backend, metadata pipeline, vector search, or recommendation behavior exists yet.

The diagrams mark only scanner integration as implemented. All arrows beyond the decoded-barcode return are design intent.

## Component responsibilities and ownership

| Component | Responsibility | Persistent data ownership | Primary inputs | Primary outputs |
| --- | --- | --- | --- | --- |
| Android UI and flow coordinator | Starts scanning, requests resolution/ranking, renders product and independently labelled recommendation states | User-facing transient state only | User action, scanner outcome, backend outcome | Scan, product, generic/personalized, empty, and retryable states |
| Scanner integration | Adapts Google Code Scanner outcomes to ASAP's flow | None; barcode images are not retained | Scan request | Decoded EAN/UPC value or classified scanner outcome |
| Android API client | Crosses the device/backend boundary and preserves independent product/recommendation outcome classes | None | Barcode and optional bounded history context | Product outcome plus recommendation mode/results/status |
| Local bounded history | Supplies recent known-product context for the extended MVP and explicit cold start when insufficient | Android application on the device | Confirmed known-product interaction | Bounded recent product references |
| Backend API module | Validates and coordinates one application operation and combines module results without hiding partial success | None | Barcode and optional history context | Product outcome and separate recommendation outcome |
| Product-resolution module | Checks the normalized catalog, consults the external adapter when appropriate, applies controlled fallback data, normalizes records, and reports provenance | Owns writes to the normalized product catalog | Barcode, external/fallback records | Known product with provenance, unknown product, or temporarily unavailable |
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

Endpoint evolution, retries, timing, history context, and production guarantees remain deferred. No I1 contract behavior was implemented at the S2 checkpoint.

## Failure boundaries

| Failure location | Responsible component | Required visible behavior |
| --- | --- | --- |
| Scan cancelled, unreadable, empty, or scanner module unavailable | Android scanner integration/UI | Remain on-device and show the already defined scanner state; make no product claim |
| Device cannot reach the backend or receives no usable response | Android API client/UI | Show a retryable backend-unavailable state; do not invent cached product/recommendation data |
| External source missing a barcode | Product-resolution module | Use an applicable catalog/fallback record, otherwise return `unknown` |
| External source unavailable | Product-resolution module | Use an applicable catalog/fallback record, otherwise return `unavailable`, distinct from `unknown` |
| Product known but vector candidates empty or search unavailable | Recommendation module/API | Return product details with independent `empty` or `unavailable` recommendation status |
| History absent, insufficient, corrupt, or unsupported | Android history boundary and recommendation module | Ignore unusable context and return clearly labelled generic/cold-start results |

The exact retry policy and validation rules belong to later API/data-model work. Controlled fallback use must be visible through provenance; it must not masquerade as a live provider result.

## Personalization and privacy boundary

History-based ranking is a committed extended-MVP capability, but there is no account system or central user profile. The Android application owns a bounded history of known product references and supplies it as optional request context. The backend computes the ranking for that request and does not retain the user history.

A last-K window is the simplest candidate. K, event types, recency weighting, centroid/profile aggregation, deletion controls, persistence mechanism, and retention duration remain open. Before real user history is retained, the project must accept a privacy/retention decision; deterministic synthetic history may be used earlier for architecture and ranking tests.

## Trust boundaries and constraints

- Barcode images remain inside the Google Code Scanner experience; ASAP receives only the decoded value/outcome.
- Barcode values and optional history references cross from the device to the backend and must be treated as untrusted input.
- External metadata is untrusted and must pass normalization before entering the product catalog.
- The MVP has no account/authentication boundary, public-hosting commitment, or cross-device synchronization.
- Product metadata and vector data may share one physical storage technology later, but their logical ownership and consistency rules remain separate.

## Still open

- Hosting and concrete module/package layout within the accepted Java 21/Spring Boot 4.1.1/Maven baseline.
- Product API/provider, controlled fallback dataset, license, normalization fields, provenance representation, and caching policy.
- Product, interaction, and recommendation schemas and validation limits.
- Embedding model/version, text composition, vector dimensions, exact versus approximate search, and update strategy.
- Personalization K/window, events, weighting, aggregation, retention, deletion, and evaluation.
- Concrete resilience policy, timeouts, retries, observability, security hardening, and production operation.
- Whether later UX evidence justifies replacing Google Code Scanner with direct ML Kit Barcode Scanning and CameraX.

The historical image at `report/assets/asap-architecture.png` remains source material only. Canonical PlantUML renders replace it in formal deliverables.
