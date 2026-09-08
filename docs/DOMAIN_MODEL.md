# MVP domain model

Status: Product model accepted under T-008/S1 on 2026-09-08. Interaction and recommendation sections remain T-008/S2 work.

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

- Interaction events, bounded history, recommendation context, cold-start, semantic-result fields, ranking modes, and scores: T-008/S2.
- External provider and fallback dataset selection, licenses, attribution details, caching, refresh, precedence, and rate limits.
- Database schema, catalog storage, migrations, vector storage, and embedding input construction.
- Images, ingredients, nutrition, allergens, package quantity, price, retailer inventory, localization, and cross-barcode product-family grouping.
- Java refactoring or changes to the accepted I1 request/response behavior.

## S1 acceptance evidence

- Every field is classified as mandatory or optional and has a bounded normalization rule.
- Internal identity, barcode lookup identity, provenance, and product-resolution states are distinct.
- Sparse real metadata can remain truthful without weakening the frozen I1 contract.
- Provider, persistence, AI, interaction, and recommendation decisions remain explicitly deferred.
