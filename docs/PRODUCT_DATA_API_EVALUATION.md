# Product-data API evaluation

Handoff note (D-025): this dated, unexecuted provider protocol is preserved; its S2 remains unapproved. Product data evaluation is not a substitute for the separate CNN image-training dataset or notebook evaluation of every AI/statistical component. See `CURRENT_REQUIREMENTS.md` and `NOTEBOOK_VALIDATION.md`; no provider/licensing claims were re-researched during this handoff.

Last researched: 2026-09-08

Task: T-009

Current state: S1 accepted by the user on 2026-10-06. S2 probes are folded into T-011/S2 (not yet approved); per the user's proposal, several providers may be combined through an adapter + router with field-level merge (see `AI_MVP_DESIGN.md` §2), so S2 also measures merged completeness. No live product endpoint has been called.

## Purpose and boundary

This document is the dated source of truth for selecting the I2 barcode product-data source. It evaluates providers against the accepted provider-neutral model in [`DOMAIN_MODEL.md`](DOMAIN_MODEL.md); it does not select a provider, integrate one, choose the fallback dataset, or redefine ASAP as a food-only application.

The local `200…` restricted-circulation fixtures in [`fixtures/`](fixtures/) are permanently excluded from every external request. They exist only for the deterministic I1 demonstration.

## Evaluation gates and criteria

A candidate must pass every mandatory gate before an aggregate comparison is useful.

| ID | Criterion | How it will be assessed | Gate |
| --- | --- | --- | --- |
| G1 | Permitted use | Official access rules and terms allow the controlled read-only experiment. Before a recommendation or integration, application use, attribution, caching, retention, and redistribution obligations must also be actionable. Ambiguity affecting the experiment stops S2; production-use ambiguity remains a visible gate failure for S3 until clarified. | Mandatory |
| G2 | Exact GTIN identity | Accepts the relevant EAN-13, EAN-8, UPC-A, and, where supported, UPC-E input without losing the scanner-provided value/format; any canonicalization or alias must be observable. | Mandatory |
| G3 | Minimal product mapping | A found record supplies a product name and a barcode identity that can be reconciled with the request. ASAP creates its own internal ID and provenance; it never treats a provider ID as its domain ID. | Mandatory |
| G4 | Outcome safety | Not-found/unknown, invalid input, authentication/rate-limit failure, malformed response, and provider unavailability can be distinguished or conservatively mapped to the accepted `UNKNOWN`/`UNAVAILABLE` boundary. | Mandatory |
| G5 | Obtainable MVP access | Access, authentication, quota, rate, and cost permit a small academic MVP without hidden account, payment, or operational prerequisites. | Mandatory |
| G6 | Scope honesty | Coverage is measured across relevant product categories. A category-specific provider may have a bounded role, but cannot silently narrow ASAP's accepted general-product model. | Mandatory |
| C1 | Optional field completeness | Observe brand, category, description, and tags without inventing absent data. Images, nutrition, offers, and prices are outside the accepted minimal model and are recorded only as provider extras. | Comparative |
| C2 | Provenance and freshness | Observe provider identity, retrieval time, source/revision metadata, localization/language, and record freshness where exposed. Provider-level provenance supplied by an adapter is not mistaken for field-level provenance. | Comparative |
| C3 | Operational fit | Compare latency, response stability, documentation quality, request limits, payload size/field selection, and a realistic development-to-production path. A sample is evidence, not an SLA. | Comparative |

### Required model mapping

| ASAP concept | Open Food Facts | Barcode Lookup | UPCitemdb |
| --- | --- | --- | --- |
| Barcode identity | `code`; normalization/redirect behavior must be observed | `barcode_number`, `barcode_formats` | `ean`, optional `upc`/`gtin`; returned EAN-13 canonicalization must be observed |
| Name | `product_name` | `title` | `title` |
| Brand | `brands` (optional) | `brand` or cautiously `manufacturer` | `brand` (optional) |
| Category/tags | `categories_tags` and related taxonomy fields | `category`; `features` are not automatically domain tags | `category`; no documented direct tag field |
| Description | No safe direct mapping assumed in the minimal probe | `description` | `description` |
| ASAP internal ID | Generated internally; never a provider field | Generated internally; never a provider field | Generated internally; never a provider field |
| Provenance | Adapter records provider, endpoint/API version, retrieval time, and exposed source/revision data | Adapter records provider, endpoint/API version, retrieval time, and `last_update` where present | Adapter records provider, endpoint/API version, retrieval time; core-record freshness is not documented |

Blank or missing optional fields remain absent. Text is normalized only under the accepted NFC/trim rules; the probe does not translate, infer, merge, or generate metadata.

## Shortlist from official documentation

All provider facts below were checked on 2026-09-08. Prices, quotas, API versions, and terms are time-sensitive and must be rechecked before S2 and before integration.

### 1. Open Food Facts — test

Official evidence: [API introduction](https://openfoodfacts.github.io/documentation/docs/Product-Opener/api/), [v3 product operation](https://openfoodfacts.github.io/documentation/docs/Product-Opener/v3/products/get-api-v3-product-code/), [barcode-scan tutorial](https://openfoodfacts.github.io/documentation/docs/Product-Opener/api/tutorials/scanning-barcodes/), and [licence guide](https://openfoodfacts.github.io/documentation/docs/Product-Opener/api/tutorials/license-be-on-the-legal-side/).

- The current recommended API is v3 (documented as v3.6). Product reads require a custom identifying user agent but no authentication; current documentation publishes 15 product-read requests per minute per IP.
- The database is crowdsourced and explicitly carries no accuracy, completeness, or reliability assurance.
- Database, individual contents, and images have separate ODbL, Database Contents Licence, and CC BY-SA terms. Attribution/share-alike and any combined-database implications must be designed before caching or redistribution.
- The service is centered on food. Its v3 product operation exposes `product_type` routing across food, beauty, pet-food, and general-product verticals, but that does not establish comparable cross-category coverage.
- Localized fields and taxonomies are strong comparative features. Barcode normalization is also a risk: short codes and UPC/EAN aliases must be recorded without overwriting the exact scanned identity.

Reason to test: open licensing, no-key reads, rich food metadata, localization, and explicit not-found responses make it a credible bounded food source. Main risk: it may be too category-specific and incomplete to serve alone as ASAP's general source.

S2 access note: the production read-only catalog is the coverage target, with `ASAP-evaluation/0.1 (https://github.com/mikelandjelos/asap-app)` as the identifying user agent, minimal requested fields, and the budget below. The official recommendation to use staging while testing is a real caveat: recheck it immediately before S2, record why the bounded read-only production sample is compatible with the current guidance, and stop or use staging with a qualified coverage result if it is not. No account or write operation is authorized.

### 2. Barcode Lookup — retain for documentation comparison; blocked for S2 probes

Official evidence: [API overview and plans](https://www.barcodelookup.com/api), [API documentation](https://www.barcodelookup.com/api-documentation), and [terms](https://www.barcodelookup.com/terms-and-conditions).

- The documented API accepts barcode values of 7, 8, 10, 11, 12, 13, or 14 digits and exposes broad general-product fields including title, category, brand/manufacturer, description, features, images, and `last_update`.
- Every call requires an account API key. The advertised free test requires signup, contact details, phone verification, and acceptance of the terms. Current paid plans begin at USD 99/month for 5,000 calls; the service documents a 100-request/minute ceiling.
- The terms describe Product Data as proprietary, provide only a limited revocable subscription, disclaim any guaranteed data response, require cached/backed-up Product Data to be deleted at termination, and do not grant rights to third-party content.
- The terms also prohibit automated access to a free account. Therefore an automated S2 corpus run cannot use a newly created free test account.

Reason to retain: its documented category breadth and direct model fields make it a valuable comparator. Reason not to probe in S2: no authorized key exists, account/terms acceptance is outside scope, and automated free-account probing is expressly unsuitable. It remains documentation-only unless the user later supplies separately authorized paid access consistent with the terms.

### 3. UPCitemdb — test, subject to terms caveat

Official evidence: [API overview](https://www.upcitemdb.com/api/), [plan comparison](https://www.upcitemdb.com/wp/docs/main/development/plan/), [response schema](https://www.upcitemdb.com/wp/docs/main/development/responses/), and [terms](https://devs.upcitemdb.com/termsofservice).

- The general-product lookup accepts UPC, EAN, GTIN, and ISBN and returns JSON with EAN/UPC/GTIN identities, title, brand, category, description, images, and offers.
- The free Explorer endpoint requires no signup and advertises full-database access with 100 combined requests/day. Its documented sustainable rate is one request per 10 seconds, burst limit six lookups/minute, and one connection. DEV is currently USD 99/month and PRO USD 699/month.
- The response contract distinguishes invalid input, not found, authentication failure, daily/burst rate limits, and server errors. The gateway may return HTML on a 5xx, which must map to `UNAVAILABLE` rather than `UNKNOWN`.
- The official plan pages are internally awkward: the free table separately lists “100 combined requests” and “20 search requests.” S2 uses lookup only and stays below both values.
- The published terms focus on purchased/customer services, grant a limited operational-use licence, preserve third-party rights, and do not clearly state caching/retention/redistribution rights for the no-signup trial. This ambiguity blocks payload retention and any integration decision until clarified.

Reason to test: broad claimed category scope, no-signup read access, useful error taxonomy, and fields close to the accepted model. Main risks: licence/retention ambiguity, canonicalization to EAN-13, US-oriented category/language data, and no core-record freshness field.

S2 access note: an explicit S2 authorization is required before use of the trial endpoint. Retain only minimal measurements and response hashes—not raw payloads—unless the provider clarifies redistribution rights.

## Reproducible S2 corpus

The 12 product-bound values below are public examples or catalog entries with a product name. Their GS1 modulo-10 checks, and the UPC-E expansion where applicable, were validated locally during S1. The cited name/category is a corpus label, not a claim that any shortlisted provider will return it.

| ID | Exact scan value | Format | Publicly verifiable product | Coverage category | Evidence |
| --- | --- | --- | --- | --- | --- |
| P01 | `42070047` | EAN-8 | Airwaves Cool Cassis chewing gum | Food/confectionery | [retailer product page](https://zakupy-eleclerc.pl/airwaves-cool-cassis-guma-do-zucia-bez-cukru-10-drazetek) |
| P02 | `04963406` | UPC-E | Coca-Cola Classic Coke Soft Drink | Beverage | [Open Food Facts tutorial](https://openfoodfacts.github.io/openfoodfacts-server/api/tutorials/comparing-sodas/) |
| P03 | `3017620422003` | EAN-13 | Nutella Ferrero 400 g | Food/spread | [Open Food Facts reference](https://openfoodfacts.github.io/documentation/docs/Product-Opener/v2/products/get-product-by-code/) |
| P04 | `3850334341389` | EAN-13 | Mlinci | Food/Serbian-region product | [official Open Food Facts SDK example](https://openfoodfacts.github.io/openfoodfacts-python/usage/) |
| P05 | `5000112519945` | EAN-13 | Coca-Cola Zero | Beverage | [Open Food Facts tutorial](https://openfoodfacts.github.io/openfoodfacts-server/api/tutorials/comparing-sodas/) |
| P06 | `069000019832` | UPC-A | Diet Pepsi | Beverage | [Open Food Facts tutorial](https://openfoodfacts.github.io/openfoodfacts-server/api/tutorials/comparing-sodas/) |
| P07 | `069055838150` | UPC-A | Braun Clean and Renew shaver refills | Personal care/appliance consumable | [UPCitemdb public catalog](https://www.upcitemdb.com/upc) |
| P08 | `033287135141` | UPC-A | Ryobi 18 V trim router P600 | Hardware/tool | [UPCitemdb public catalog](https://www.upcitemdb.com/upc) |
| P09 | `883929540969` | UPC-A | *Sully* Blu-ray/DVD | Media | [UPCitemdb public catalog](https://www.upcitemdb.com/upc) |
| P10 | `792692000115` | UPC-A | California Baby shampoo and bodywash | Personal care | [UPCitemdb public catalog](https://www.upcitemdb.com/upc) |
| P11 | `717489740753` | UPC-A | Milani Matte Color Statement lipstick | Cosmetics | [UPCitemdb public catalog](https://www.upcitemdb.com/upc) |
| P12 | `842885098716` | UPC-A | Xenergy Mango Guava energy drink | Beverage | [UPCitemdb public catalog](https://www.upcitemdb.com/upc) |

P02 is a legitimate public UPC-E example; it expands to valid UPC-A `049000006346`. S2 must send the exact eight-digit scanner value and record whether a provider rejects it, preserves it, or returns an expanded/canonical alias. P01 is an EAN-8 and must not be left-padded or conflated with UPC-E solely because both contain eight digits.

Two controls are outside product-coverage scoring:

- E01 invalid checksum: `1234567890123` as EAN-13. This should be rejected locally before a provider call; if a provider-error observation is specifically approved, it consumes one request.
- U01 syntactically valid candidate: `9999999999994` as EAN-13. No claim is made that it is unassigned. Its observed result tests whether “not found” can be represented; if any provider returns a product, record that fact and treat it as an additional found sample rather than forcing `UNKNOWN`.

All values beginning with the local restricted-circulation prefix `200` are forbidden. No synthetic local fixture may be substituted into this corpus.

## S2 protocol and request budget

S2 may start only after S1 is accepted and S2 is explicitly approved.

### Calls

- Accessible providers: Open Food Facts and UPCitemdb. Barcode Lookup receives zero live calls unless separately authorized compatible access is supplied.
- Send one lookup per P01–P12 plus U01: 13 requests per accessible provider, 26 total. E01 is validated locally by default and does not reach a provider.
- Hard ceiling: 14 requests per accessible provider and 28 overall for the approved protocol, stricter than the task-level limit of 50 per provider.
- Run sequentially with one connection and at least 11 seconds between calls. Do not batch, retry automatically, search, download images, trigger a rate limit deliberately, or follow a response into another provider.
- Request only the fields needed for barcode identity, name, brand, category/tags, description, record/revision time, and response status. Use the identifying user agent above where supported.
- Before the run, recheck official version, terms, endpoint, and limits. A changed term or unclear permission stops that provider's run; it does not justify guessing.

### Observation row

Each attempted request records:

1. corpus ID, exact input value, declared format, provider, endpoint/API version, and UTC timestamp;
2. HTTP status, provider result/error code, found/not-found/unavailable classification, response content type, and elapsed milliseconds;
3. exact returned primary barcode plus any aliases/canonical forms;
4. presence—not copied prose—of name, brand, category, description, tags, language/localization, provider/source metadata, and freshness metadata;
5. mapping result for required and optional ASAP fields, including every omission or ambiguity;
6. rate-limit headers where exposed, redirect behavior, malformed-response notes, and retryability classification;
7. SHA-256 of the response bytes when evidence may not be redistributed. Raw payloads are retained only if current terms explicitly permit it.

### Outcome rules

- `KNOWN`: the provider positively identifies the requested product and supplies a usable name plus a reconcilable barcode identity.
- `UNKNOWN`: the provider positively reports no matching product for a syntactically valid identifier.
- `UNAVAILABLE`: timeout, DNS/TLS/transport failure, 5xx, unexpected HTML, authentication failure, rate limit, malformed schema, or an ambiguous response. These never become `UNKNOWN`.
- `INVALID`: rejected before the provider adapter and outside the accepted product-resolution union. It must not consume normal coverage requests.

Do not average away a gate failure. S3 will compare mandatory gates first, then documented coverage/completeness and operational observations. No S1 text is a provider selection or an integration claim.

## Open questions carried into S2/S3

- Does either accessible provider preserve EAN-8 versus UPC-E identity and expose an unambiguous canonical alias?
- Can Open Food Facts provide meaningful non-food coverage through its product-type routing, or should it be considered only a food-specific component?
- Does UPCitemdb permit production caching of normalized factual fields and, if so, under what retention/attribution terms?
- Can a general-source candidate provide field-level provenance, or only provider-level provenance added by ASAP?
- Are Serbian or regional products discoverable with usable names/categories, and in which language?
- Is Barcode Lookup's paid-only, deletion-bound model justifiable for the academic MVP after comparison with accessible candidates?
