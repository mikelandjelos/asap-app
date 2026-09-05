# Accepted deterministic I1 contract

Status: Accepted and frozen under T-007/S2. This contract is not implemented until T-007/S3 and is not connected to Android until S4.

The contract proves the Android-to-backend boundary with controlled data. It must not be presented as live product resolution, semantic similarity, or personalization.

## HTTP operation

`POST /api/v1/scan-queries`

- Request and successful domain-response media type: `application/json` encoded as UTF-8.
- Invalid request and unexpected server-error media type: `application/problem+json`, following RFC 9457.
- Every syntactically and semantically valid query returns HTTP `200`, including `UNKNOWN` and controlled upstream `UNAVAILABLE` product outcomes. These are domain outcomes, not malformed requests.
- Invalid JSON, missing/extra fields, unsupported format, invalid length, non-digit value, or invalid check digit returns HTTP `400`.
- A body above 2 KiB returns HTTP `413`; a non-JSON request returns HTTP `415`. An unexpected backend failure returns HTTP `500` without a stack trace or internal implementation detail.
- A device/network failure has no domain response. Android must classify it separately as backend unavailable.

No authentication, history context, language negotiation, request identifier, timestamp, cache contract, retry policy, or public-host guarantee belongs to I1.

## Request

```json
{
  "barcode": {
    "value": "2000000000015",
    "format": "EAN_13"
  }
}
```

Both objects are required and unknown fields are rejected. Enum values are case-sensitive.

| `format` | Required digit count | Check-digit rule |
| --- | ---: | --- |
| `EAN_13` | 13 | GS1 modulo-10 over the first 12 digits |
| `EAN_8` | 8 | GS1 modulo-10 over the first 7 digits |
| `UPC_A` | 12 | GS1 modulo-10 over the first 11 digits |
| `UPC_E` | 8 | Expand the number-system digit and six compressed digits to UPC-A, then verify the final digit |

`value` contains ASCII digits only. I1 preserves the scanner-provided format and value as the lookup key; it does not equate UPC-A with an EAN-13 value containing a leading zero. Request bodies are limited to 2 KiB. These accepted validation requirements justify adding `spring-boot-starter-validation` in S3, plus one custom GS1 check-digit validator shared by the API boundary and fixture loader.

## Successful response envelope

The response contains two independent discriminated outcomes:

```json
{
  "product": {
    "status": "KNOWN",
    "data": {
      "id": "fixture:oat-drink",
      "barcode": { "value": "2000000000015", "format": "EAN_13" },
      "name": "ASAP ovseni napitak",
      "brand": "ASAP Demo",
      "category": "Biljni napici",
      "description": "Kontrolisani proizvod za I1 demonstraciju.",
      "tags": ["ovseni", "napitak", "demo"],
      "provenance": {
        "type": "CONTROLLED_FIXTURE",
        "source": "asap-i1-fixtures-v1"
      }
    }
  },
  "recommendations": {
    "status": "RESULTS",
    "mode": "DETERMINISTIC_FIXTURE",
    "placeholder": true,
    "items": [
      {
        "rank": 1,
        "product": {
          "id": "fixture:almond-drink",
          "name": "ASAP bademov napitak",
          "brand": "ASAP Demo",
          "category": "Biljni napici"
        }
      },
      {
        "rank": 2,
        "product": {
          "id": "fixture:soy-drink",
          "name": "ASAP sojin napitak",
          "brand": "ASAP Demo",
          "category": "Biljni napici"
        }
      }
    ]
  }
}
```

### Product outcome invariants

- `KNOWN`: `data` is required and has exactly the normalized fields shown above.
- `UNKNOWN`: `data` is absent; the barcode is valid but has no controlled record.
- `UNAVAILABLE`: `data` is absent; the controlled resolver simulates a temporarily unavailable source.
- `provenance.type` is always `CONTROLLED_FIXTURE` in I1. No fixture may claim live or cached-provider provenance.

### Recommendation outcome invariants

- For a `KNOWN` product, status is exactly `RESULTS`, `EMPTY`, or `UNAVAILABLE`; `mode` is `DETERMINISTIC_FIXTURE` and `placeholder` is `true` in every case.
- `RESULTS` has one or more items with contiguous one-based ranks. `EMPTY` and `UNAVAILABLE` have an empty `items` array.
- The primary fixture response has exactly two items, almond then soy, matching the canonical JSON fixture order.
- Recommendation items contain display summaries, not scores. I1 has no embedding, similarity, relevance, or personalization score to report.
- For an `UNKNOWN` or `UNAVAILABLE` product, status is `NOT_APPLICABLE`, `mode` is absent, `placeholder` is `false`, and `items` is empty.
- Android S4 must render the Serbian label “Deterministički demo rezultat — nije AI preporuka” whenever `placeholder` is `true`.
- Recommendation `EMPTY` or `UNAVAILABLE` never removes a `KNOWN` product. This is the required partial-success behavior.

## Invalid-request response

Errors use RFC 9457 problem details and stable machine-readable field codes:

```json
{
  "type": "urn:asap:problem:invalid-request",
  "title": "Invalid request",
  "status": 400,
  "detail": "The request contains invalid fields.",
  "errors": [
    { "field": "barcode.value", "code": "INVALID_CHECK_DIGIT" }
  ]
}
```

Allowed validation codes are `REQUIRED`, `UNKNOWN_FIELD`, `UNSUPPORTED_FORMAT`, `INVALID_LENGTH`, `DIGITS_ONLY`, `INVALID_CHECK_DIGIT`, `MALFORMED_JSON`, and `BODY_TOO_LARGE`. Clients branch on `status` and `code`, never on human-readable `title` or `detail`.

## Controlled fixture set

[`fixtures/i1-products.json`](fixtures/i1-products.json) is the canonical design-time fixture specification for S3. All codes use GS1 prefix `200`, reserved for restricted-circulation numbering rather than open global supply chains. They are for local ASAP tests/demos only, must never be sent to an external product provider, and make no claim of ownership or real product identity.

| Barcode | Controlled outcome | Purpose |
| --- | --- | --- |
| `2000000000015` | known + two deterministic results | Primary physical-phone happy path; scannable asset below |
| `2000000000022` | known + two deterministic results | Ranked fixture candidate and alternate known query |
| `2000000000039` | known + two deterministic results | Ranked fixture candidate and alternate known query |
| `2000000000046` | known + empty recommendations | Empty-result behavior while preserving product details |
| `2000000000053` | known + unavailable recommendations | Partial-success behavior |
| `2000000000985` | product unavailable + recommendations not applicable | Controlled resolver-unavailability behavior |
| `2000000000992` | unknown + recommendations not applicable | Valid but deliberately absent lookup key |

The primary [EAN-13 SVG](fixtures/i1-known-product-ean13.svg) encodes `2000000000015`, includes quiet zones, and can be displayed or printed for S4. The human-readable digits and SVG metadata are not the source of truth; the encoded 95-module EAN-13 pattern is.

## Contract acceptance cases

S3 must automate the backend cases; S4 must automate Android mapping where practical and physically exercise the first case.

| ID | Input/scenario | Required evidence |
| --- | --- | --- |
| I1-C01 | Primary barcode | `200`; known oat product; exactly almond then soy; placeholder label required |
| I1-C02 | Unknown barcode | `200`; `UNKNOWN`; recommendations `NOT_APPLICABLE` |
| I1-C03 | Product-unavailable barcode | `200`; `UNAVAILABLE`; recommendations `NOT_APPLICABLE` |
| I1-C04 | Empty-recommendation barcode | `200`; product remains `KNOWN`; recommendations `EMPTY` |
| I1-C05 | Recommendation-unavailable barcode | `200`; product remains `KNOWN`; recommendations `UNAVAILABLE` |
| I1-C06 | Repeat any valid fixture query | Semantically identical response and ranking on every run |
| I1-C07 | Invalid check digit | `400` problem detail with `INVALID_CHECK_DIGIT` |
| I1-C08 | Missing, extra, malformed, non-JSON, or oversized input | Corresponding `400`/`413`/`415` problem response; no internal detail leakage |
| I1-C09 | Physical scan of the supplied SVG | Google Code Scanner returns `EAN_13`/`2000000000015`; Android shows the known product and explicit demo-only results |

## Explicitly deferred

- External product APIs, licenses, normalization adapters, caching, and real product claims.
- Embeddings, similarity metrics, vector indexes, scores, semantic results, and history-based personalization.
- Optional history request context and its retention/window policy.
- Public deployment, authentication, rate limiting, observability, localization protocol, and production resilience.

## Standards source

- [GS1 prefix allocation table](https://www.gs1.org/standards/id-keys/company-prefix)
- [RFC 9457: Problem Details for HTTP APIs](https://www.rfc-editor.org/rfc/rfc9457.html)
