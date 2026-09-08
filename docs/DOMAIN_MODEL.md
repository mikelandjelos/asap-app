# MVP domain model

Status: Accepted under completed T-008 on 2026-09-08. S1 defines the product model; S2 defines interaction, history, recommendation, and AI-derived-artifact boundaries.

This document defines source-neutral application concepts. It is not an HTTP payload, provider schema, database schema, or Java-class prescription. The accepted I1 wire contract remains frozen in [`I1_CONTRACT.md`](I1_CONTRACT.md); later iterations must map between that boundary and this model explicitly.

## Product aggregate

A `Product` is one scannable retail item or package variant known to ASAP.

| Field | Cardinality | Invariant and purpose |
| --- | --- | --- |
| `id` | exactly one | Opaque, stable ASAP identifier, 1–128 characters. It is never a product name or an external-provider identifier. Existing `fixture:*` identifiers remain valid for controlled data. |
| `barcode` | exactly one | Valid `Barcode` value and format. It is the lookup identity for this package record, while `id` is its internal identity. |
| `name` | exactly one | Trimmed, non-blank display name, at most 200 characters. |
| `brand` | zero or one | Trimmed display brand, at most 120 characters. Missing provider data stays absent rather than becoming “unknown”. |
| `category` | zero or one | Trimmed provider-neutral display category, at most 120 characters. A category taxonomy is deliberately not selected. |
| `description` | zero or one | Trimmed factual description, at most 2,000 characters. It must not be generated or embellished during normalization. |
| `tags` | zero to 32 | Ordered, exact-string-deduplicated, non-blank values of at most 64 characters each. Tags support later dataset preparation but do not imply an embedding algorithm. |
| `provenance` | exactly one | Source classification and stable source label defined below. It accompanies every known normalized record. |

Only `id`, `barcode`, `name`, and `provenance` are mandatory. This permits honest partial metadata from real sources while keeping a useful product identity. UI and semantic-processing eligibility must be evaluated separately: missing optional fields are not fabricated.

### Barcode value object

- `value` is the exact ASCII-digit value returned by the scanner after accepted GS1 validation.
- `format` is one of `EAN_13`, `EAN_8`, `UPC_A`, or `UPC_E`.
- Equality uses both value and format. UPC-A and a zero-prefixed EAN-13 remain distinct unless a later accepted normalization decision proves equivalence safely.
- ASAP stores no barcode image. Multiple package sizes or codes are separate product records for the MVP; cross-package grouping is deferred.

### Provenance value object

| Field | Cardinality | Invariant |
| --- | --- | --- |
| `type` | exactly one | `CONTROLLED_FIXTURE`, `EXTERNAL_PROVIDER`, or `FALLBACK_DATASET`. |
| `source` | exactly one | Stable, non-blank source/dataset label of at most 120 characters. It must be suitable for logs and user-facing attribution where licensing requires it. |

Retrieval timestamps, provider record IDs, licenses, cache metadata, and source precedence are intentionally not part of the product aggregate yet. The provider/fallback selection task must define them only if its evidence requires them. `CONTROLLED_FIXTURE` remains local-only and may never be queried against an external provider.

## Product resolution outcome

Resolution remains a discriminated outcome rather than a nullable product:

| Status | Product | Meaning |
| --- | --- | --- |
| `KNOWN` | required | A normalized product satisfying all mandatory invariants is available. |
| `UNKNOWN` | absent | The lookup completed, but no usable record exists for the validated barcode. |
| `UNAVAILABLE` | absent | Resolution could not complete because a required source or adapter is temporarily unavailable. |

An incomplete or untrusted source record is never silently promoted to `KNOWN`. Normalization may omit optional fields, but a missing/invalid mandatory field makes the record unusable and must be classified by the future resolver policy rather than fabricated.

## Normalization boundary

- External and fallback records enter the domain only through the product-resolution module.
- Normalize text to Unicode NFC, trim surrounding whitespace, convert blank optional text to absence, preserve Serbian Latin and other source-language characters, and enforce the limits above.
- Preserve factual source text; do not translate, infer, summarize, or generate missing metadata.
- Preserve tag order after trimming and exact duplicate removal so downstream dataset generation is repeatable.
- Reject invalid barcodes and conflicting effective records before catalog insertion. Source precedence and conflict resolution remain a later provider/fallback decision.
- API DTOs, Android UI models, provider responses, and persistence records map to this aggregate at their respective boundaries; none of them becomes the canonical domain type by coincidence.

## Compatibility with the accepted I1 slice

The implemented backend `Product` already supplies all fields above, and every controlled fixture populates the optional text fields. I1 additionally requires non-blank `brand`, `category`, and `description` at its frozen wire boundary; S1 does not weaken or version that accepted contract. The I1 provenance pair maps directly to `CONTROLLED_FIXTURE` plus its source label.

The future I2 boundary may preserve the I1 response shape for complete records or introduce an explicitly reviewed version if optional real metadata requires wire-level changes. That choice belongs to the provider integration plan, not this model proposal.

## Explicitly deferred from S1

- Interaction events, bounded history, recommendation context, cold-start, semantic-result fields, ranking modes, and scores: addressed by the accepted S2 contract below.
- External provider and fallback dataset selection, licenses, attribution details, caching, refresh, precedence, and rate limits.
- Database schema, catalog storage, migrations, vector storage, and embedding input construction.
- Images, ingredients, nutrition, allergens, package quantity, price, retailer inventory, localization, and cross-barcode product-family grouping.
- Java refactoring or changes to the accepted I1 request/response behavior.

## S1 acceptance evidence

- Every field is classified as mandatory or optional and has a bounded normalization rule.
- Internal identity, barcode lookup identity, provenance, and product-resolution states are distinct.
- Sparse real metadata can remain truthful without weakening the frozen I1 contract.
- Provider, persistence, AI, interaction, and recommendation decisions remain explicitly deferred.

## Interaction event

An `Interaction` is a minimal anonymous record that a known product was displayed to the user. It is owned by the Android application and is not a backend account, analytics event, or claim that the user bought or liked the product.

| Field | Cardinality | Invariant and purpose |
| --- | --- | --- |
| `id` | exactly one | Opaque client-generated event identifier, 1–128 characters; unique inside the retained history window. |
| `productId` | exactly one | Stable ASAP ID of a product that resolved as `KNOWN`, 1–128 characters. No product metadata is duplicated into the event. |
| `kind` | exactly one | `PRODUCT_VIEWED` for the MVP. New meanings require an explicit model revision rather than overloading this value. |
| `occurredAt` | exactly one | UTC instant supplied by the device and used for recency ordering. It is not evidence of server receipt time or a trusted audit timestamp. |

The client records the event only after it has displayed a valid known product. Unknown/unavailable resolution, scanner cancellation/failure, and backend failure create no interaction. The event contains no barcode, account/device identifier, location, free text, image, product snapshot, preference label, or inferred profile.

## Bounded history context

`HistoryContext` is an optional, newest-first list of `Interaction` values sent by Android for one future recommendation request. The Android application owns and can delete the underlying history; the backend may use only the supplied request context and does not persist it as a user profile.

- Absence or an empty list is valid and produces `COLD_START` for a real semantic request.
- Repeated products are allowed because separate views can carry recency/frequency signal. Event IDs must remain unique.
- Before sending, the client removes exact duplicate event IDs, orders events by descending `occurredAt` with `id` as a deterministic tie-breaker, and keeps only the newest policy-bounded entries.
- A future API contract must define a numeric safety ceiling. A client exceeding it or sending out-of-order interactions, a malformed/duplicate event, an unknown kind, an invalid product ID, or an invalid timestamp receives an invalid-request outcome; the backend does not silently sort or reinterpret malformed context.
- The effective K/window, minimum sufficient-history threshold, maximum age, clock-skew tolerance, weighting, and aggregation method remain evaluation-driven configuration decisions, not fields of this domain model.
- `HistoryReadiness` is derived per request as `COLD_START` or `SUFFICIENT`. It is never asserted by the client.
- Before real history is persisted, the project must approve retention duration, clear-history UX, storage protection, and consent/notice wording. Synthetic history may be used for design and tests before that decision.

## Recommendation request and outcome

A `RecommendationRequest` references exactly one current known `Product` and optionally one `HistoryContext`. It does not contain product metadata, embeddings, account identity, or client-selected personalization state.

`RecommendationOutcome` remains independent from product resolution:

| Field | Cardinality | Invariant |
| --- | --- | --- |
| `status` | exactly one | `RESULTS`, `EMPTY`, `UNAVAILABLE`, or `NOT_APPLICABLE`. |
| `mode` | conditional | Required for `RESULTS`, `EMPTY`, and recommendation `UNAVAILABLE`; absent for `NOT_APPLICABLE`. Values are defined below. |
| `historyState` | exactly one | `NOT_USED`, `COLD_START`, or `APPLIED`, constrained by mode. |
| `items` | zero or more | Non-empty only for `RESULTS`; ranks are contiguous and one-based. Candidate product IDs are unique and exclude the current product. |

### Recommendation modes

| Mode | AI-derived | History state | Meaning |
| --- | --- | --- | --- |
| `DETERMINISTIC_FIXTURE` | no | `NOT_USED` | I1 integration scaffolding. It remains explicitly labelled as not an AI recommendation and has no score. |
| `GENERIC_SEMANTIC` | yes | `COLD_START` | Semantic similarity for absent or policy-insufficient history. It is similar-product output, not personalization. |
| `PERSONALIZED_HISTORY` | yes | `APPLIED` | Sufficient bounded history materially participates in ranking. The label must state that history influenced the result. |

`NOT_APPLICABLE` uses no mode and `NOT_USED` when product resolution is not `KNOWN`. An unavailable semantic/personalized computation retains the attempted real mode and corresponding history state so the UI can explain the failure honestly. A known product remains displayable for `EMPTY` or `UNAVAILABLE` recommendations.

### Ranked product and score evidence

Each `RankedProduct` contains a contiguous one-based `rank`, one candidate `productId`, and optional `RankingEvidence`.

- `DETERMINISTIC_FIXTURE` forbids ranking evidence.
- AI-derived modes require one finite raw `score`, a non-blank `scoreType`, and a non-blank `modelVersion`/pipeline identifier.
- Score range and meaning belong to the later selected model/metric. Scores are comparable only within one response produced by the same mode, `scoreType`, and model/pipeline version.
- The UI must not convert an uncalibrated score into a percentage, probability, quality guarantee, or explanation of causality.
- Rank is the user-facing ordering contract. A score may remain hidden until evaluation proves that displaying it helps users.

## AI-derived artifacts and factual boundary

The model distinguishes source-backed facts from derived computational artifacts:

- `Product`, `Barcode`, and `Provenance` are factual, source-backed domain data after deterministic normalization.
- `Interaction` and `HistoryContext` are device-observed context, not AI inference and not proof of preference.
- `ProductEmbedding` is a planned model-derived representation linked to a product ID and a model/pipeline version. Its vector dimension, storage, and input composition remain undecided.
- `HistoryProfile` is a planned request-scoped derived representation of sufficient history. It is not a durable backend user profile.
- `RankingEvidence` and the order of generic/personalized results are AI-derived outputs.
- No embedding, profile, score, inferred label, or generated text may overwrite or masquerade as factual `Product` metadata.

The canonical [domain-model class diagram](diagrams/domain-model.puml) shows structure, ownership, and cardinality. The [AI-enrichment/data-lineage diagram](diagrams/ai-enrichment-flow.puml) shows where deterministic normalization ends and planned AI derivation begins.

## I1 compatibility and deterministic examples

S2 does not change the accepted I1 payload or implementation. I1 maps to `DETERMINISTIC_FIXTURE`, `NOT_USED`, no `RankingEvidence`, and its existing independent statuses. A later versioned boundary may add history and real recommendation fields after separate approval.

| Example | Required model outcome |
| --- | --- |
| Known product, no history, real semantic engine available | `GENERIC_SEMANTIC`, `COLD_START`, results/empty/unavailable as actually observed. |
| Known product, policy-sufficient synthetic history | `PERSONALIZED_HISTORY`, `APPLIED`; evidence identifies the ranking pipeline. |
| Known product, insufficient non-empty history | `GENERIC_SEMANTIC`, `COLD_START`; do not claim personalization. |
| Unknown/unavailable product | `NOT_APPLICABLE`, no mode, `NOT_USED`, empty items. |
| Known product, recommendation unavailable | Product remains visible; outcome retains the attempted real mode/history state. |
| Out-of-order, malformed, duplicate, or over-limit history | Future boundary rejects the request with stable validation detail; it does not silently sort or personalize from ambiguous input. |
| I1 controlled request | Existing deterministic placeholder behavior and explicit non-AI label remain unchanged. |

## Explicitly deferred after S2

- Numeric K/window, sufficiency threshold, retention duration, clock tolerance, weighting, aggregation, and final event expansion.
- Android persistence/API serialization and clear-history UI; backend request parsing, validation, profile construction, and ranking code.
- Product provider/fallback source, database/vector product, embedding model, text composition, vector dimension, similarity metric, reranking algorithm, score calibration, and evaluation thresholds.
- Authentication, cross-device synchronization, durable backend profiles, analytics, or behavioral preference claims.

## S2 acceptance evidence

- Every interaction/history/recommendation field has ownership, cardinality, and validity semantics without selecting implementation technologies.
- Cold start, sufficient history, deterministic fixture, generic semantic, and personalized output are distinguishable.
- Factual product metadata and AI-derived artifacts cannot be confused structurally or in the UI contract.
- I1 behavior remains compatible and unchanged while later versioning points are explicit.
- Both canonical diagrams derive English technical, Serbian formal, and compact Serbian presentation views from one source each.
