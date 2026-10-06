# Diagram contract

D-024 amendment (2026-10-06): existing canonical sources/renders represent the earlier design, not a complete model of the revised scope. Mandatory clustering, PCA, MMR and the final dataset-trained CNN/TFLite scanner replacement need an approved design extension under D-024/D-025. Notebook-to-report evidence links and polished UI are also required; see `../CURRENT_REQUIREMENTS.md`. Personalization is mandatory MVP functionality. Do not infer optionality from missing diagram elements; do not regenerate/edit presentation assets until the user authorizes the final presentation phase.

Status: T-001's diagram contract, the T-006/S2 concrete MVP-topology refinement, and the T-008/S2 canonical domain-model and AI-enrichment views are accepted.

## Purpose and audiences

The diagrams describe the **earlier proposed MVP design, pending D-024/D-025 extensions** and visibly annotate the implemented scanner and deterministic backend slices. They serve three audiences:

- project design and implementation sessions, which need maintainable technical detail;
- the formal Serbian report, which needs readable architecture evidence;
- the Beamer presentation, which needs a simplified visual explanation.

Every architecture diagram must visibly use the phrase **Proposed architecture** or **Planirana arhitektura**. Diagram presence alone is not implementation evidence; explicit status annotations must agree with `docs/PROJECT_STATUS.md`.

## Required diagrams

### Component architecture

- **Source:** `component-architecture.puml`
- **Notation:** PlantUML component diagram.
- **Purpose:** show logical system boundaries, responsibilities, dependencies, and data stores.
- **Required elements:** Android application boundary, Google Code Scanner integration, local bounded history, one backend deployment with API/product-resolution/recommendation modules, normalized product catalog, vector index, controlled fallback dataset, and external product source boundary.
- **Boundary rule:** combine logical ownership with the two selected deployment boundaries: one Android application and one backend modular monolith. Logical stores do not imply separate database products, and no protocol, framework, vendor, or hosting platform is selected.
- **Planned behavior:** history-based personalization is a mandatory MVP capability. The diagram locates history on the device but does not select K, persistence APIs, weighting, profile aggregation, or retention duration.

### Scan-to-recommendation flow

- **Source:** `scan-to-recommendation-flow.puml`
- **Notation:** PlantUML sequence diagram with data labels.
- **Purpose:** trace the end-to-end exchange from barcode capture to product details and recommendations.
- **Success path:** capture/decode barcode, read optional bounded local history, resolve normalized product metadata, request generic or personalized ranking, query the vector index, return product details plus a separately classified recommendation outcome, and record a known interaction locally.
- **Outcome boundaries:** distinguish scan cancellation/unreadable input, backend/API unavailability, unknown product, temporarily unavailable resolution, personalized results, generic cold-start results, and empty/unavailable recommendations. Do not prescribe retries, error types, or recovery algorithms before those decisions are made.
- **Boundary rule:** show exchanged information and responsibility, not classes, endpoints, payload schemas, or timing guarantees that do not exist yet.

### Domain model

- **Source:** `domain-model.puml`
- **Notation:** PlantUML UML class diagram.
- **Purpose:** show the product, interaction/history, request/outcome, ranked-item, and AI-derived concepts with ownership, cardinalities, and references.
- **Boundary rule:** blue source-backed facts, yellow device-owned context, green outcomes, and purple planned AI-derived artifacts are structurally distinct. AI artifacts may reference factual product data but never replace it.
- **Detail rule:** the technical/report render contains fields and status invariants; the presentation render may omit attributes but retains every class, relationship, cardinality, ownership stereotype, and AI marker.

### AI enrichment and data lineage

- **Source:** `ai-enrichment-flow.puml`
- **Notation:** PlantUML data-lineage/component flow.
- **Purpose:** identify where source records undergo deterministic normalization and where planned embedding, semantic retrieval, history profiling, and ranking derive new artifacts.
- **Boundary rule:** retain the factual product-details path, the explicitly non-AI I1 fixture branch, generic cold-start behavior, optional sufficient-history enrichment, and the final separation between facts and AI results.
- **Status rule:** embeddings, vector storage, semantic retrieval, history profiles, and AI ranking are labelled as planned until implementation evidence exists.

### Recommendation pipeline (T-011/S1, proposed)

- **Source:** `recommendation-pipeline.puml`
- **Notation:** PlantUML activity diagram.
- **Purpose:** show the proposed runtime stages: product resolution, query vector, candidate retrieval, cold-start vs personalized scoring, MMR, cluster/PCA enrichment and response.
- **Status rule:** entirely planned; the title says so. Parameters are confirmed by notebooks.

T-011/S1 also revised `component-architecture.puml` (source router and adapters, recommendation sub-components, offline ML pipeline, future CNN scanner). Default and `SERBIAN` renders were regenerated; `PRESENTATION` renders were deliberately not regenerated because presentation work is deferred.

## Canonical terminology

PlantUML identifiers and operational documentation use the English canonical term. The report and presentation use the corresponding Serbian Latin label.

| Canonical term | Serbian deliverable label | Existing-source variants | Contract note |
| --- | --- | --- | --- |
| Android client | Mobilna aplikacija | Mobile application | Java/XML scanner-to-API coordination and all I1 outcome states are implemented and physically validated with the controlled backend. |
| Google Code Scanner | Google Code Scanner | Kamera/skeniranje; Lokalni skener barkoda; CNN; TFLite | Temporary implemented scanner; required dataset-trained CNN/TFLite replacement follows the PoC/MVP under D-024. Decoder/camera choices remain open. |
| Local bounded history | Lokalna ograničena istorija | User profile; interaction history | Android-owned; S2 accepts anonymous newest-first view events while K, persistence, weighting, and retention remain undecided. |
| Backend API | Backend API / API servis | Backend; API service | Implemented Spring MVC I1 boundary inside one backend deployment; the controlled Android connection is physically validated. |
| Product resolution | Razrešavanje proizvoda | Metadata adapter | Owns lookup, fallback selection, normalization, provenance, and product outcome classification. |
| Product metadata store | Katalog metapodataka o proizvodima | Baza proizvoda; barcode → metadata | I1 uses a packaged controlled fixture; durable storage and an external provider remain undecided. |
| Semantic search and recommendation component | Semantička pretraga i preporuke | Semantic search/recommendations | I1 boundary returns labelled deterministic fixture results; embedding, ranking, and MMR remain unimplemented. |
| Vector index | Vektorski indeks | Embeddings store | Owned by the recommendation module; exact versus approximate search and storage technology remain undecided. |
| Product details | Podaci o proizvodu | Product; metadata | T-008/S1 accepts the source-neutral aggregate and limits in `../DOMAIN_MODEL.md`; it does not change the I1 wire contract. |
| Interaction | Interakcija | Scan/view event | T-008/S2 accepts one anonymous device-owned `PRODUCT_VIEWED` event for a known displayed product. |
| History context | Kontekst istorije | User profile; recent activity | T-008/S2 accepts optional newest-first bounded interactions; K, persistence, weighting, and retention remain undecided. |
| Recommendations | Preporuke | Top-N similar products | T-008/S2 separates deterministic fixture, generic semantic, and personalized-history modes plus independent result states. |
| AI-derived artifact | AI izvedeni artefakt | Embedding; profile; score | Planned model output with version evidence; it never overwrites factual product metadata. |

## Source and rendered-file layout

```text
docs/diagrams/
├── README.md
├── includes/
│   └── theme.puml
├── component-architecture.puml
├── domain-model.puml
├── ai-enrichment-flow.puml
├── scan-to-recommendation-flow.puml
└── rendered/
    ├── ai-enrichment-flow.png
    ├── component-architecture.png
    ├── domain-model.png
    ├── scan-to-recommendation-flow.png
    └── sr/
        ├── ai-enrichment-flow.png
        ├── component-architecture.png
        ├── domain-model.png
        ├── scan-to-recommendation-flow.png
        └── presentation/
            ├── ai-enrichment-flow.png
            ├── component-architecture.png
            ├── domain-model.png
            └── scan-to-recommendation-flow.png
```

- `.puml` files are canonical and must be reviewed as source.
- `includes/theme.puml` contains shared deterministic styling only; it must not contain architectural elements.
- PNG renders are committed because pdfLaTeX, the report, and the presentation need portable raster assets.
- Default renders use English canonical terminology for technical review. `SERBIAN` renders use Serbian Latin for formal deliverables.
- `PRESENTATION` renders remove secondary furniture and may group sequential internal stages or collapse repeated response messages and nested branch detail to remain legible on a 16:9 slide. The technical/report render remains authoritative for every individual component; presentation grouping must retain every responsibility, branch, ownership boundary, and named outcome class.
- All variants come from the same four canonical `.puml` sources. Do not duplicate architecture structure to localize or simplify a render.
- Rendered files must be regenerated whenever their source or shared theme changes.
- Diagram filenames remain stable so documentation references do not require churn.

## Styling and content rules

- Use UTF-8 source and Serbian Latin text only where a deliverable-facing label needs it.
- Keep colors and layout deterministic through the shared theme include.
- Visually distinguish device/application, single backend deployment, external dependency, data-store, factual-source, device-context, outcome, and AI-derived boundaries as applicable to the view.
- Add a legend only when notation is not self-explanatory.
- Avoid decorative icons, vendor branding, speculative technology badges, and implementation-status colors.
- Prefer readable labels over dense detail; move unresolved detail into documentation questions.

## Reproducible validation and rendering contract

The current environment has PlantUML 1.2020.02, Java 21, and Graphviz 2.43.0. Because its inherited `DISPLAY` may not be usable, commands explicitly select headless operation by unsetting `DISPLAY` for the process.

From the repository root, future diagram subtasks must use:

```sh
env -u DISPLAY plantuml -checkonly docs/diagrams/*.puml
env -u DISPLAY plantuml -charset UTF-8 -tpng \
  -o rendered docs/diagrams/*.puml
env -u DISPLAY plantuml -DSERBIAN -charset UTF-8 -tpng \
  -o rendered/sr docs/diagrams/*.puml
env -u DISPLAY plantuml -DSERBIAN -DPRESENTATION -charset UTF-8 -tpng \
  -o rendered/sr/presentation docs/diagrams/*.puml
```

Run the syntax check for each enabled variant when conditional content changes. S2 and S3 verify their individual source. S4 verifies all variants, regenerates every PNG, and compiles the report and presentation after integration.

## Resolved by T-006/S2

- Android and backend are separate deployment boundaries.
- The MVP backend is one deployable modular monolith; product resolution and recommendation are internal modules, not separately operated services.
- Bounded interaction history is owned and retained by the Android application and is supplied only as optional request context; the backend does not own a durable user profile in this architecture.
- The backend owns the normalized product catalog and vector index as distinct logical stores. Their physical storage products may later be shared or separate.
- Product resolution owns external-provider/fallback handling and provenance. Recommendation failure is independent of product resolution, so known product details remain displayable.

## Explicitly unresolved

- Android SDK levels, dependency versions, and project structure.
- Whether UX evidence later justifies upgrading from Google Code Scanner to direct ML Kit Barcode Scanning with CameraX.
- Backend hosting/deployment environment and any post-I1 transport evolution.
- Product metadata provider, catalog storage product, caching policy, and fallback dataset.
- Embedding model, vector-index technology, exact/ANN search, and MMR use.
- K/window, weighting, aggregation, retention duration, and consent wording for the accepted interaction/recommendation model remain undecided. Generic cold-start behavior itself is required.
- Persistence schemas, retry policies, and service-level targets. The deterministic I1 payload is frozen in `docs/I1_CONTRACT.md`.

Resolving any of these requires its own approved task or subtask and a recorded decision.
