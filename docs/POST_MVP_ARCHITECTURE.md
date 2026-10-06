# Post-MVP data platform — architecture note

Status: vision agreed in discussion on 2026-10-06; **not scheduled**. These items start only after the MVP, the report and the presentation are finished (D-026, D-028, D-033), each under its own approved plan. TODO lists them under "Posle MVP-a".

## Target flow

```text
 sources                       staging            consolidation          model maintenance          serving
 ───────                       ───────            ─────────────          ─────────────────          ───────
 provider APIs (OFF, UPCitemdb) ┐
 open-data exports (OFF family) ├─► raw records ─► consolidator ─► golden ─► refit pipeline ─► versioned ─► backend
 scraper framework (per-site    │   (immutable,    (entity          records   (notebooks +       bundle       (router,
   adapters, AI extraction)     ┘    provenance)    resolution)      + DB      export + parity)   (D-034)      ranking)
```

Each stage has one responsibility and its own tests. Data flows forward, and nothing downstream rewrites raw records.

## 1. Sources and the scraper framework

- **One framework, one adapter per site**, not a swarm of independent scrapers.
  - **Scheduler:** a single scheduler runs adapters as jobs from a shared queue.
  - **Politeness:** per-domain rate limits, `robots.txt`/terms checks and an identifying User-Agent.
  - **Data handling:** licence metadata travels with every record; raw pages are not retained unless their terms allow it.
- **Shared contract:** adapters emit the same partial record shape as `SourceTypes.SourceRecord` (name, brand, category, tags, labels, description), plus source, URL, fetch time and confidence. API sources and scrapers therefore plug into the same consolidator.
- **AI extraction** happens inside an adapter. A model turns unstructured pages into structured fields with a confidence score. It never invents values: missing stays missing, as in DOMAIN_MODEL.
- **Scaling:** add workers of the same framework. This gives swarm-like throughput while politeness rules stay in one place.

## 2. Consolidator: data quality and identity

- **Purpose:** turn many partial, possibly conflicting records into one **golden record** per product, with per-field provenance. It is an entity-resolution / master-data component, not a drift fixer.
- **Identity:**
  - GTIN aliases (UPC-A ↔ zero-padded EAN-13, UPC-E expansion) group records;
  - fuzzy matching handles near-duplicates (name + brand variants, cf. D-036);
  - the scanned `(value, format)` remains the lookup identity.
- **Conflicts:** fields are resolved by source trust, freshness and confidence, generalizing the router's request-time merge (D-037), and every decision is logged.
- **Consistency:** eventual. A fresh scan may first show the router's live merge; the golden record catches up in the background.
- **Prerequisite:** a real database (PostgreSQL), with pgvector or Qdrant for vectors (D-028). The in-memory search remains the verified baseline and parity reference.

## 3. Drift and the refit pipeline: model maintenance

The consolidator makes the catalog grow, and that growth causes drift. A separate refit pipeline rebuilds everything fitted on the catalog:

| Artifact | Why it drifts | Refresh |
| --- | --- | --- |
| Catalog embeddings | new products | encode new items (cheap; the model is fixed) |
| Char-TF-IDF vocabulary/idf | new words, brands, languages | refit; re-check the hybrid α |
| Type clusters, k, labels | new product types | recluster, review labels, map old→new theme ids for history continuity |
| PCA map basis | the space shifts | refit, then Procrustes-align to the previous basis so the map does not jump for users |
| α, β, λ | tuned on older data | rerun notebooks 01–03 with their pre-declared rules |
| Embedding model | better models appear | full re-embed as a new bundle major version |
| Evaluation sets | stale judgements | refresh, plus human judgements |

**Drift signals:**
- share of scans missing the catalog;
- new items' distance to the nearest centroid;
- explained variance / reconstruction error of new items in the current PCA basis;
- out-of-vocabulary n-gram rate;
- score distribution shift.

**Orchestration (user idea, 2026-10-06):** run the refit pipeline as a workflow DAG in **Apache Airflow** or a comparable orchestrator (e.g. Prefect or Dagster):
- **Tasks:** `snapshot_catalog → embed_new → refit_tfidf → recluster_and_relabel_review → refit_pca_procrustes → rerun_notebooks_01_03 → export_bundle → verify_bundle + java_parity → canary_swap → monitor`.
- **Triggers:** drift sensors or a schedule.
- **Safety:** retries, artifact lineage, and a manual approval gate before the swap (theme labels are reviewed by a human).

**Process:** a signal crosses a threshold; the notebooks run on the new snapshot; the export produces a new bundle version; parity and contract tests run; the new bundle is swapped in atomically, with the previous version kept for rollback. The bundle versioning (D-034, D-036) already supports this.

## 4. Also post-MVP

- "You vs other users (friends)" map view, which needs accounts and social data (D-033).
- Deployment to a budget VPS (Hetzner/DigitalOcean), with HTTPS and a release build. Optionally int8 model quantization, re-verified for parity.
- Persisting the router cache and including externally resolved products in history profiles once the database exists.
