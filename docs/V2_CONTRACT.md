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
  - Products outside the catalog are resolved by the router (S6d, D-037): Open Food Facts v3 then UPCitemdb, field-level merge.
    - `KNOWN` external products get `id: "gtin:<value>"`, `provenance {type: EXTERNAL_PROVIDER, source: "open_food_facts[+upcitemdb]"}`, `fieldSources {field: source}`, a theme and a map position (from their type text), and recommendations. If UPCitemdb contributed, `attribution` adds a UPCitemdb notice.
  - `UNKNOWN`: every reachable source reports not found, or the code is a restricted in-store code (never sent out).
  - `UNAVAILABLE`: nothing was found and at least one source failed (transport, 5xx/HTML, rate limit, local quota/spacing, open circuit). The product is not declared unknown.
- `recommendations`:
  - `status`: `RESULTS`, `EMPTY` or `NOT_APPLICABLE`. `NOT_APPLICABLE` is returned when the product is unknown; it has no `mode` and `historyState` is `NOT_USED`.
  - `mode`: `GENERIC_SEMANTIC` (`historyState: COLD_START`; fewer than 3 distinct catalogued products in history) or `PERSONALIZED_HISTORY` (`APPLIED`).
  - `pipelineVersion` is the bundle version. `diversification` is `{method: "MMR", lambda}`.
  - `items` holds up to 10 results; the query and history products are never included, and neither are variants sharing the query's or another result's normalized name + brand (S6c.1). Each item has:
    - `rank` (1-based);
    - `product {id, barcode, name, brand, category}` and `theme`;
    - `evidence {score, scoreType, modelVersion}`, where `scoreType` is `HYBRID_RELEVANCE` or `PERSONALIZED_HYBRID_RELEVANCE`. The score is raw, comparable only within one response, and is not a percentage (DOMAIN_MODEL rule).
- `imageUrl` (optional) on `product.data` and on each item's `product`: a small front image, served only from HTTPS Open Food Facts-family image servers. Catalog images come from the exports (97.4 % of the catalog); live lookups use OFF's `image_front_small_url`. UPCitemdb images are never used (third-party rights). When any image is present, `attribution` adds "Images: Open Food Facts contributors (CC BY-SA)." Clients load images directly from those servers (S7c.1).
- `you`: present when history contains catalogued products. `{historyUsed, mapPosition}` is the user's type-space centroid for the "you vs themes" map.
- `attribution`: the ODbL notice. Clients must display it.

**Router configuration:**
- `asap.sources.enabled` (default `true`);
- `asap.sources.off.base-url` and `asap.sources.upcitemdb.base-url`;
- `asap.sources.upcitemdb.daily-quota` (default 90, below the provider's 100);
- `asap.sources.upcitemdb.burst` (default 5 lookups per rolling minute, below the provider's documented 6/min; replaced the original 11 s fixed spacing, which made quick consecutive scans `UNAVAILABLE`);
- `asap.sources.user-agent`.

Timeouts: `asap.sources.per-source-timeout` (default 8 s) and `asap.sources.total-budget` (default 12 s), with a 4 s connect timeout.
- The first 1.5 s / 3 s limits turned every UPCitemdb-dependent scan into `UNAVAILABLE`: the provider's latency varies from about 0.8 s to more than 4 s.
- Each source lookup is logged as `source=… barcode=… outcome=… ms=…`. OFF results are cached (found 24 h, not found 1 h); UPCitemdb results are never cached. A source's circuit opens for 60 s after 3 consecutive failures.

## `GET /api/v2/catalog-map`

`{pipelineVersion, attribution, themes[{id, label, size, x, y}], points[{id, theme, x, y}]}`: the 60 theme centroids and a 1,500-product sample (25 per theme) in PCA(2) coordinates.

## Verification

`V2ControllerTest` (10 HTTP tests, with a scripted fetcher and no network) covers:
- generic results;
- personalized results with the "you" map position;
- cold start with repeats;
- an unknown product;
- every history error class plus an unknown field;
- barcode rules;
- the catalog map;
- v1 coexistence;
- router resolution of an external product, and unavailable providers.

`SourcesTest` (6) covers UPC-E expansion, restricted codes, OFF/UPCitemdb mapping and failure classes, quota/spacing, early stop, caching, field merge with provenance, and the circuit breaker.

Live check (2026-10-06): Braun via UPCitemdb in 1.16 s (OFF miss first), Nutella via OFF in 0.17 s (0.07 s cached), Mlinci via OFF in 0.24 s (sparse: no category), Coke UPC-E via OFF in 0.13 s.

`RankingParityTest` matches the Python fixtures for users, MMR, clusters and map positions. Packaged-JAR smoke test: 5.4 s startup and about 9 ms per request over HTTP on the desktop.
