# AI MVP design — T-011/S1

Status: **Accepted 2026-10-06** (D-027, amended by D-028: English app, vector database after the MVP). Documentation only: no code, dataset download, model download or provider call has occurred. Values marked *initial* are starting configurations that the named notebook must confirm or replace; the notebook result, not this document, is authoritative for final parameters.

Inputs: [CURRENT_REQUIREMENTS.md](CURRENT_REQUIREMENTS.md), [NOTEBOOK_VALIDATION.md](NOTEBOOK_VALIDATION.md), [DOMAIN_MODEL.md](DOMAIN_MODEL.md), [PRODUCT_DATA_API_EVALUATION.md](PRODUCT_DATA_API_EVALUATION.md) (T-009/S1, accepted 2026-10-06).

## 1. End-to-end shape

```text
offline (Python, notebooks/ + ml/)            runtime (Java backend)                     Android (Java/XML)
OFF export → filtered catalog ─┐              scan query (+ history) ─► Product router ─► adapters (local, OFF family, UPCitemdb)
                               ├─► embeddings ─► artifact bundle ─► in-memory catalog + vectors
                               ├─► k-means clusters + labels        ├─► retrieval → profile blend → MMR → top-N
                               └─► PCA(2) map                       └─► 2-D map coordinates ─────────────► Material 3 UI, local history
```

Recommendations are always drawn from the **indexed local catalog**; live providers only resolve the scanned product. A scanned product missing from the catalog is embedded at request time and used as the query.

## 2. Product sources — adapter + router (user proposal)

Several sources are combined behind one interface so the best available record is returned and every field stays attributable.

- **`ProductSourceAdapter`**: `lookup(barcode, deadline)` returns `FOUND(partial normalized record)`, `NOT_FOUND`, `UNAVAILABLE` or `RATE_LIMITED`, plus latency. Adapters never invent fields.
- **Adapters (initial):**
  1. `LocalCatalogAdapter`: offline catalog bundle; the only adapter allowed for restricted `200…`–`299…` in-store codes and controlled fixtures.
  2. `OpenFactsAdapter`: Open Food Facts v3, configurable by `product_type` vertical (food, beauty, pet food, general products). One implementation, several configured sources.
  3. `UpcItemDbAdapter`: trial endpoint for non-food breadth; hard quota guard (100/day, ≥10 s spacing). **Transient only:** no caching or persistence while the trial terms remain unclear.
  4. Barcode Lookup: not implemented unless separately authorized paid access is provided.
- **Router policy:**
  1. Restricted prefixes go to the local catalog only.
  2. Local catalog hit with name, brand and category: return immediately.
  3. Otherwise query providers sequentially in priority order within a total budget (*initial* 3 s, 1.5 s per provider). Stop once name, brand and category are present; otherwise continue to enrich.
  4. **Field-level merge:** for each field, take the first non-blank value by a per-field priority list (*initial*: OFF before UPCitemdb for name/brand/category; the longest factual description wins). Conflicts are logged, never blended.
  5. Outcome: `KNOWN` if any source yields a name; `UNKNOWN` if every reachable source reports not found; `UNAVAILABLE` if nothing was found and at least one source failed or was rate-limited.
  6. A per-adapter circuit breaker and quota counter apply. OFF results may be cached with ODbL attribution; UPCitemdb results may not.
- **UPC-E:** expand UPC-E to UPC-A before calling UPCitemdb, which rejects UPC-E (S2 evidence).
- **Identity:** lookup may try GTIN aliases (UPC-A ↔ zero-prefixed EAN-13), but the scanned value/format stays the product identity (DOMAIN_MODEL rule).
- **Domain revision required (applied in S6):** single `provenance` becomes a non-empty source list plus per-field provenance, and licence attribution is shown in the UI. This revises the T-008/S1 provenance object.
- **S6d implementation (D-037):** `backend/.../sources/` (`OpenFactsSource`, `UpcItemDbSource`, `ProductRouter`, `JavaHttpFetcher`), wired into v2.
  - Live findings: providers resolve real products in 0.13–1.2 s.
  - Sparse records (e.g. no category) yield weak recommendations.
  - Wrong theme auto-labels are now very visible ("Mashed vegetables" for Nutella), so the label pass is mandatory before S7.
- **S2 evidence:** the bounded probes record per-provider and merged field completeness, so the "best quality" claim is measured rather than assumed. Final provider priority is selected from that evidence.

## 3. Offline dataset

- **Recommended:** a filtered subset of the official Open Food Facts data export (ODbL database / DbCL contents). It is the only shortlisted source with redistribution-compatible open licensing, rich taxonomy and multilingual names.
- **Filter (*initial*):** has barcode, name and at least one category; prefer products sold in Serbia and neighbouring/EU countries; deduplicate by barcode; stratified cap of **5,000–10,000** products across top-level categories.
- **Built (S2):** `ml/asap_ml/catalog.py` runs one streaming pass over the OFF, Open Beauty Facts and Open Pet Food Facts CSV exports.
  - Filters: valid public GTIN, Latin-script name, English taxonomy categories with OFF meta-tags excluded.
  - Selection: region-first, then by completeness. Strata are the top 30/12/8 top-level categories, with √-size quotas.
  - Result: **10,000 products** (8,000 food, 1,200 beauty, 800 pet food). 98 % have a brand, 32 % a description, and 65 % are region products.
  - The manifest with checksums and config is in `data/catalog_manifest.json`.
- **Text for embedding:** `name | brand | categories (deepest 3, English taxonomy labels) | short description/labels` with missing parts omitted.
- **Storage/licence:** raw exports and generated bundles are git-ignored and regenerated by a documented script. A committed manifest records export date, URL, checksum, filter config and counts. The UI and report show the ODbL attribution.
- **Alternatives:** UPCitemdb (no clear redistribution right, quota too small); a hand-built catalog (unrepresentative); Kaggle mirrors (unclear provenance). Rejected.
- **Limitation:** food-heavy coverage. Non-food scans can resolve via UPCitemdb but get recommendations from the nearest catalog items. This is reported, not hidden.

## 4. Embedding model and serving

- **Recommended:** `intfloat/multilingual-e5-small` (384-d, MIT licence, multilingual including Serbian/Croatian text; uses `query:`/`passage:` prefixes).
- **Compared in notebook 01:** TF-IDF + cosine (lexical baseline), `sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2` (384-d, Apache-2.0). The winner by nDCG@10 and latency is selected. Licences and versions are re-verified at download.
- **Serving:** export to ONNX and run inside the Java backend with ONNX Runtime Java and the DJL HuggingFace tokenizer. This keeps the accepted single modular-monolith topology. **Parity gate:** Java vs Python embedding cosine ≥ 0.999 on a fixed sample.
- **Alternative:** a Python sidecar (FastAPI) is faster to build but adds a second service and deployment. Use it only if the ONNX/Java route fails its parity gate.
- **S3 result (notebook 01, D-030):** e5-small beat MiniLM on the leak-free benchmark (nDCG@10 0.288 vs 0.206). It only **tied** character TF-IDF (0.291), so the "beats TF-IDF" gate failed for e5 alone.
  - A tuned **hybrid** `0.9·cos_e5 + 0.1·cos_tfidf_char` scored 0.307. It significantly beats both, by +0.018 and +0.016 (paired-bootstrap 95 % CIs exclude 0), and is the adopted retrieval method.
  - ONNX parity is effectively exact (min cosine 0.9999999 on 500 texts). The ONNX file is 470 MB (fp32).
  - Latency: single uncatalogued-product encoding takes 15 ms (ONNX) / 20 ms (torch) median. Exact top-50 search takes 0.19 ms at 10k and 9 ms at 100k products.
- Catalog vectors are precomputed offline. Only an uncatalogued scanned product is embedded online.

## 5. Vector storage and retrieval

- **Recommended:** in-memory float32 matrix of L2-normalized vectors loaded from the artifact bundle, with exact brute-force cosine (dot product) search. At ≤ 10k × 384 this is ~15 MB and requires no extra service.
- **Rejected for MVP:** pgvector, Qdrant, FAISS/ANN. These add services or native dependencies without need at this scale.
- **Post-MVP (user-requested, D-028):** move catalog and vectors into a real database with vector search (candidates: PostgreSQL + pgvector, Qdrant). The in-memory search stays as the notebook-verified baseline and parity reference; a retrieval interface introduced in S6 keeps the swap local.
- **Hybrid scoring (D-030):** relevance `cos_e5` is replaced by `r(x) = 0.9·cos_e5 + 0.1·cos_tfidf_char` (sklearn `char_wb` 3–5-grams, sublinear TF, fitted on the catalog). S6 must reimplement this analyzer in Java from the exported vocabulary/idf and pass a parity test. §7–§9 use `r(x)` wherever they use `cos(q,x)`.
- **Rules:** exclude the query product, deduplicate by barcode and by normalized name+brand, and break ties by product ID.

## 6. Clustering

- **Method (recommended):** spherical k-means (k-means on L2-normalized embeddings). k is swept (*initial* 10–60) and chosen using cosine silhouette, Davies–Bouldin, stability (ARI across 10 seeds/bootstraps) and NMI/ARI against top-level OFF categories as an external check. HDBSCAN on PCA-reduced vectors is the comparator.
- **S4 result (notebook 02, D-031):** clustering runs on a **product-type space**: e5 embedding of the English OFF taxonomy text (`product_text(..., 'type')`). It does not use the retrieval embedding.
  - On the full-text embedding, clusters split by language/country (median top-level purity 0.23).
  - The type space at **k = 60** gives silhouette 0.28 (vs 0.05), Davies–Bouldin 2.2 (vs 4.5), NMI 0.72 and median purity 0.78.
  - Seed stability is moderate (ARI 0.62). The original pre-declared rule (stability ≥ 0.7 first) would have picked the language clusters; the user approved a revised, documented rule.
  - HDBSCAN marks 31 % of products as noise, so it is rejected because every product needs a theme.
  - An uncatalogued product needs one extra encoding of its type text, or its name if it has no category; nearest-centroid assignment then takes ~7 µs.
- **Labels (label pass, 2026-10-06):** human-readable labels live in `ml/theme_labels.json` and replace the auto-labels in the bundle (`autoLabel` is kept). Mixed clusters get honest broad labels ("Mixed plant-based foods", "Creams (body & dairy)").
- **Original labeling method:** each cluster gets a short human-readable label from its most frequent categories and c-TF-IDF terms, reviewed manually.
- **Runtime roles:**
  1. A **theme chip** on the product and each recommendation.
  2. **Multi-interest personalization**: history is grouped by cluster (§7).
  3. **Cluster colouring** in the PCA map (§8).
  4. **Cluster coverage** as a reported diversity metric for MMR (§9).

## 7. History and personalization

- **Android storage:** JSON file in app-private storage written with plain Java/`org.json`, so no new dependency. Retain the newest 50 `PRODUCT_VIEWED` events, max age 90 days. A clear-history action and a privacy notice are required.
- **Request:** newest **K = 20** events (*initial*). The backend persists nothing.
- **Readiness:** `SUFFICIENT` when there are ≥ 3 distinct catalogued products with vectors; otherwise `COLD_START` → `GENERIC_SEMANTIC`.
- **Profile (*initial*):** recency-weighted mean of history vectors, wᵢ = 0.5^(i/H) with half-life H = 5 events (i = 0 newest).
- **Score:** `s(x) = (1−β)·cos(q,x) + β·cos(p,x)`, *initial* β = 0.3.
- **Notebook 03 compares:** single centroid vs **multi-interest** (per-cluster centroids, p = closest interest to q). The variant with better nDCG@10 is chosen.
- **S5 result (notebook 03, D-032):** **multi-interest** profile (interest centroids per type cluster; the one closest to the query is used), β = 0.4, uniform weights (half-life ∞), K = 20 window, readiness at ≥ 3 distinct products.
  - On 1,000 synthetic test users, graded nDCG@10 is 0.726 vs generic 0.719 (+0.007, CI 0.002–0.013), and interest share@10 is 0.81 vs 0.78. The top-10 changes for 99 % of users.
  - Out-of-interest scans lose 0.007 (n.s.). The single centroid did not significantly beat generic and significantly hurt out-of-interest scans (−0.013).
  - Recency weighting did not help: the per-query interest selection already captures the current mission.
  - The effect is modest but real; real-user value needs the user study.
- **Evaluation data:** synthetic, seeded histories built from 1–3 category/cluster interests. Relevance = same leaf category as the query and inside the user's interests. Measured: generic vs personalized nDCG@10, the share of rankings changed, and cold-start correctness. Synthetic data is labelled as such in the report.

## 8. PCA

- **Visual analytics (runtime):** PCA(2) is fitted offline on centered catalog embeddings; its mean and components ship in the bundle. The backend projects the query, recommendations and history, and the app draws a **product map**: a catalog sample coloured by cluster, plus the scanned product, recommendations and history. It is drawn in a custom Android `View`/Canvas with no chart library.
- **S4 result:** reduction is **not adopted**. d = 128 keeps only 0.74 top-10 overlap, though nDCG@10 drops just 0.004; 169 dimensions are needed for 80 % of the variance. Production keeps the full 384-d retrieval vectors.
  - The map uses PCA(2) of the type space: PC1 + PC2 = 10.8 % of variance, with a readable sweets/drinks → dairy/grains → meat/pet-food gradient but heavy overlap. It is an orientation aid only.
  - Exported: `pca.npz`, plus `map_sample.json` with 25 products per cluster.
- **Map view requirement (user, 2026-10-06, D-033), to be discussed further before S7:**
  - **MVP view, "You vs product themes":** your profile centroid sits among the 60 theme centroids in PCA(2).
  - **Consistent space:** the profile is computed in the same type space as the clusters/PCA (mean of the history items' type embeddings), not the retrieval space.
  - **S6a export:** the bundle includes projected theme centroids and their labels.
  - **Deferred:** "You vs other users (friends)" needs accounts and social data.
- **Dimensionality study (notebook 02):** explained-variance curve, then recall@10 overlap and nDCG@10 at d ∈ {16, 32, 64, 128} vs full. Production keeps full vectors unless some d keeps overlap ≥ 0.95 with nDCG@10 loss ≤ 0.01.

## 9. MMR diversification

- Candidate pool: top **50** by `s(x)`. Select N = 10 greedily by `λ·s(x) − (1−λ)·max_{y∈S} cos(x,y)`.
- **Notebook 03:** λ is swept over 0–1 (step 0.1) and plotted as relevance (nDCG@10) against diversity (ILD@10 = 1 − mean pairwise cosine, plus cluster/category coverage). Edge cases are tested: short lists, duplicates, λ = 1 ≡ relevance-only.
- **S5 result:** **λ = 0.6** by the pre-declared rule.
  - Test nDCG@10 is 0.690 vs 0.726 (−4.9 %), ILD@10 is 0.127 vs 0.104 (+21 %), and type-cluster coverage rises from 1.9 to 2.6 of 10 results.
  - Edge cases pass: λ = 1 equals relevance order, short and empty pools, and an exact duplicate is pushed out of the top 10.
  - A full personalized request takes 12 ms median on the desktop CPU (catalog 10k).
- **Default λ (*initial* 0.7):** the knee where ILD rises with ≤ 5 % relative nDCG loss.

## 10. Notebook ↔ component matrix

All notebooks live in `notebooks/` and use a shared importable package `ml/asap_ml/`, so notebook and export code are identical. Each has a pinned Python 3.12 environment, seeds, dataset checksum, clean run-all, and results exported to `notebooks/results/<nb>/` as JSON summaries plus PNG/CSV figures and tables.

| Notebook | Subtask | Components | Key evidence |
| --- | --- | --- | --- |
| `00_source_probes` | S2 | provider router (statistical summary only) | per-provider/merged field completeness, outcome classes, latency |
| `01_data_embeddings_retrieval` | S3 | dataset, embedding, similarity retrieval, ONNX parity | dataset stats; TF-IDF vs MiniLM vs e5 P@10/nDCG@10 on category proxy and ~30 manually judged queries; encode/search latency vs corpus size; parity ≥ 0.999 |
| `02_clustering_pca` | S4 | clustering, PCA | k sweep metrics, stability, NMI vs categories, HDBSCAN comparison, cluster labels; explained variance, retrieval preservation, 2-D map plots, runtime |
| `03_personalization_mmr` | S5 | history profile, personalization, MMR | generic vs centroid vs multi-interest nDCG@10, rank-change rate, cold start; λ sweep trade-off curve, coverage, edge cases, runtime |
| `04_scanner_cnn` | later | CNN detector, TFLite, decoder | per NOTEBOOK_VALIDATION matrix |

**Provisional acceptance thresholds (revisable with evidence):**
- the selected embedding beats TF-IDF on nDCG@10;
- personalized beats generic nDCG@10 on synthetic interest sessions;
- MMR default meets the ≤ 5 % nDCG-loss rule;
- backend recommendation latency p95 < 300 ms (desktop, catalog ≤ 10k);
- phone scan-to-result < 2 s excluding provider time.

Desktop and phone timings are reported separately.

## 11. Backend contract (detailed in S6)

New `POST /api/v2/scan-queries`; v1/I1 stays frozen. The request adds an optional `history`. The response adds:
- source list, per-field provenance and attribution;
- recommendation mode/historyState;
- per item: score, scoreType, modelVersion, clusterId/label;
- MMR λ and the pipeline version;
- 2-D map coordinates for the query, items and history.

**Runtime bundle (S6a, D-034).** `ml/export_bundle.py` writes `data/processed/bundle/<version>/` (git-ignored; `CURRENT` names the active version, currently `20261006-d7293828`, 526 MB):
- `manifest.json`: parameters, model id/revision/pooling, text rules, matrix shapes, source notebook-summary hashes and per-file SHA-256;
- `catalog.jsonl`: products with `fullText`, `typeText` and `cluster`;
- raw little-endian float32 matrices: `embeddings.f32` (10000×384), `type_embeddings.f32`, `cluster_centroids.f32` (60×384), `pca_mean.f32`, `pca_components.f32` (2×384);
- `tfidf.json`: 69,747-term vocabulary with idf and analyzer params;
- `themes.json`: labels, terms and projected x/y of the 60 theme centroids;
- `map_sample.json`;
- `model/`: `model.onnx` and `tokenizer.json`;
- `fixtures/parity.json`: expected outputs for embeddings, TF-IDF (including Unicode edge cases), retrieval + MMR, 8 users (cold start, a repeated product, sufficient history, "you" map position), 3 uncatalogued products and cluster/PCA assignment, with tolerances.

`ml/verify_bundle.py` re-checks a bundle from its own files only. It includes a sklearn-free char-TF-IDF reference implementation, which is the blueprint for the Java port.

**Java port (S6b, D-035):** the `rs.ac.ni.elfak.asap.backend.ai` package holds:
- `RuntimeBundle` (checksum-verified loader);
- `CharTfidf` (port of `ml/verify_bundle.py`), plus `LexicalIndex` (inverted index);
- `OnnxTextEncoder` (DJL tokenizer + ONNX Runtime + mean pooling);
- `HybridRetriever` (exact top-k, ties to the lower index);
- `ProductText`.

`BundleParityTest` passes every TF-IDF (including Unicode edge cases), embedding (cosine ≥ 0.999), retrieval and text-rule fixture.
- Desktop timings: load + index 4.8 s at startup; encode 9.2 ms; hybrid top-50 over 10k products 5.8 ms.
- These classes are not yet wired into Spring or an endpoint (S6c).

**Ranking and API (S6c):** `PersonalRanker` (readiness, multi-interest profile, MMR, cluster, PCA, "you"), `RecommendationEngine` and the conditional `AiConfiguration`/`V2Controller` implement [V2_CONTRACT.md](V2_CONTRACT.md). A personalized recommendation takes 7.7 ms in-process and about 9 ms over HTTP.

**Variant collapse (S6c.1, D-036):** same normalized name + brand variants (412 products, 4.1 %) used to fill result lists, e.g. 5× Carrefour "Petits pains grilles".
- **Rule:** inspect the top 200, keep one candidate per (name, brand) at its best score, drop the query's own variants, then pool 50 → MMR.
- **Effect (notebook 03):** test lists containing variants fall from 5.2 % to 0 %, at a cost of −0.0015 nDCG@10.
- **MMR re-tuned:** with the same pre-declared rule MMR now selects λ = 0.7, costing −1.3 % nDCG for +13 % ILD.
- **Open:** theme auto-labels remain visibly imperfect in results and need the human pass before S7.

`GET /api/v2/catalog-map` returns the cluster-coloured catalog sample and cluster labels. The artifact bundle is versioned, and the backend refuses to start on a checksum mismatch.

## 12. UI direction (built in S7)

- **Library:** Material Components for Android (Material 3, XML themes). This is a new dependency, justified as the standard Android Views design system. The seed palette derives from the project diagram colours (#164D7D primary, #A51003 accent), with light and dark themes.
- **Structure:** one Activity with a `BottomNavigationView` and fragments: **Skeniraj** (scan CTA plus recent items), **Proizvod** (product card, source/licence chips; "Za tebe"/"Slični proizvodi" list with mode label, theme chip and a diversity note), **Mapa** (PCA: you vs theme centroids, D-033), **Istorija** (list, clear history, privacy notice).
- **States:** loading, empty, unknown, unavailable and error with retry on every screen. All app text is in **English** (D-028); the formal report and presentation stay Serbian.
- **Acceptance checklist:**
  - TalkBack labels on all controls and points of interest;
  - touch targets ≥ 48 dp and contrast ≥ 4.5:1;
  - layouts hold at 200 % font scale, in dark mode and in portrait on the Samsung test phone.

## 13. Scanner replacement (after MVP, unchanged)

The CNN detector plus a ZXing decode step replaces Google Code Scanner behind the existing scanner-integration boundary. It produces the same `(value, format)` output, so the router and recommendation flow are untouched. Dataset, architecture and decoder are chosen in that later task.

## 14. Risks

| Risk | Mitigation |
| --- | --- |
| OFF is food-heavy | measure coverage in S2; label non-food recommendations as nearest-catalog matches |
| Proxy relevance (categories) overstates quality | add a manually judged query set and report both |
| ONNX tokenizer parity in Java | parity gate; Python sidecar fallback |
| Synthetic histories ≠ real users | state it explicitly; user test in evaluation phase |
| Artifacts too large for git | git-ignored bundle plus checksum manifest and regeneration script |
| UPCitemdb terms | transient use only, never cached; drop the adapter if the terms forbid it |
