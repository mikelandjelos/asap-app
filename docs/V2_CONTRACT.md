# V2 HTTP contract — AI scan query and catalog map

Status: implemented in T-011/S6c (2026-10-06), awaiting acceptance. Frozen v1 (`I1_CONTRACT.md`) is unchanged.

Registration: the v2 endpoints exist only when the backend runs with `asap.bundle.dir`. Without it, they return 404 problem responses. The value is a runtime bundle directory (D-034), or a bundle root containing `CURRENT`.

Errors: RFC 9457 `application/problem+json` with `errors[{field, code}]`, as in v1. The request body limit is 16 KiB (413 `BODY_TOO_LARGE`).

## `POST /api/v2/scan-queries`

### Request

```json
{
  "barcode": {"value": "3560070737048", "format": "EAN_13"},
  "history": [
    {"id": "e2", "productId": "off:5900259128591", "kind": "PRODUCT_VIEWED", "occurredAt": "2026-10-06T12:00:00Z"},
    {"id": "e1", "productId": "off:8000500310427", "kind": "PRODUCT_VIEWED", "occurredAt": "2026-10-06T11:58:00Z"}
  ]
}
```

- `barcode`: same validation rules and error codes as v1.
- `history`: optional, at most 50 events, newest first: descending `occurredAt`, ties broken by ascending `id`.
  - Error codes: `HISTORY_TOO_LONG`, `INVALID_EVENT_ID`, `DUPLICATE_EVENT_ID`, `INVALID_PRODUCT_ID`, `UNSUPPORTED_KIND` (only `PRODUCT_VIEWED`), `INVALID_TIMESTAMP` (ISO-8601 instant), `OUT_OF_ORDER`, and `UNKNOWN_FIELD`. Malformed context is rejected, never reordered.
  - Product IDs missing from the catalog are ignored. Only the newest 20 catalogued events are used.
  - The backend stores nothing.

### Response (200)

- `product.status`:
  - `KNOWN`: `data` holds the normalized product with `provenance`, `theme {id, label}` (type cluster, D-031) and `mapPosition {x, y}` (PCA(2), D-033).
  - `UNKNOWN`: not in the catalog. S6d adds live providers.
- `recommendations`:
  - `status`: `RESULTS`, `EMPTY` or `NOT_APPLICABLE`. `NOT_APPLICABLE` is returned when the product is unknown; it has no `mode` and `historyState` is `NOT_USED`.
  - `mode`: `GENERIC_SEMANTIC` (`historyState: COLD_START`; fewer than 3 distinct catalogued products in history) or `PERSONALIZED_HISTORY` (`APPLIED`).
  - `pipelineVersion` is the bundle version. `diversification` is `{method: "MMR", lambda}`.
  - `items` holds up to 10 results; the query and history products are never included, and neither are variants sharing the query's or another result's normalized name + brand (S6c.1). Each item has:
    - `rank` (1-based);
    - `product {id, barcode, name, brand, category}` and `theme`;
    - `evidence {score, scoreType, modelVersion}`, where `scoreType` is `HYBRID_RELEVANCE` or `PERSONALIZED_HYBRID_RELEVANCE`. The score is raw, comparable only within one response, and is not a percentage (DOMAIN_MODEL rule).
- `you`: present when history contains catalogued products. `{historyUsed, mapPosition}` is the user's type-space centroid for the "you vs themes" map.
- `attribution`: the ODbL notice. Clients must display it.

## `GET /api/v2/catalog-map`

`{pipelineVersion, attribution, themes[{id, label, size, x, y}], points[{id, theme, x, y}]}`: the 60 theme centroids and a 1,500-product sample (25 per theme) in PCA(2) coordinates.

## Verification

`V2ControllerTest` (8 HTTP tests) covers:
- generic results;
- personalized results with the "you" map position;
- cold start with repeats;
- an unknown product;
- every history error class plus an unknown field;
- barcode rules;
- the catalog map;
- v1 coexistence.

`RankingParityTest` matches the Python fixtures for users, MMR, clusters and map positions. Packaged-JAR smoke test: 5.4 s startup and about 9 ms per request over HTTP on the desktop.
