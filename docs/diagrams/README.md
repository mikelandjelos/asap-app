# Diagram contract

Status: T-001's diagram contract and the T-006/S2 concrete MVP-topology refinement are accepted.

## Purpose and audiences

The diagrams describe the **proposed MVP design**, not deployed or implemented software. They serve three audiences:

- project design and implementation sessions, which need maintainable technical detail;
- the formal Serbian report, which needs readable architecture evidence;
- the Beamer presentation, which needs a simplified visual explanation.

Every diagram must visibly use the phrase **Proposed architecture** or **Planirana arhitektura**. Diagram presence must never be presented as implementation evidence.

## Required diagrams

### Component architecture

- **Source:** `component-architecture.puml`
- **Notation:** PlantUML component diagram.
- **Purpose:** show logical system boundaries, responsibilities, dependencies, and data stores.
- **Required elements:** Android application boundary, Google Code Scanner integration, local bounded history, one backend deployment with API/product-resolution/recommendation modules, normalized product catalog, vector index, controlled fallback dataset, and external product source boundary.
- **Boundary rule:** combine logical ownership with the two selected deployment boundaries: one Android application and one backend modular monolith. Logical stores do not imply separate database products, and no protocol, framework, vendor, or hosting platform is selected.
- **Planned behavior:** history-based personalization is a committed extended-MVP capability. The diagram locates history on the device but does not select K, persistence APIs, weighting, profile aggregation, or retention duration.

### Scan-to-recommendation flow

- **Source:** `scan-to-recommendation-flow.puml`
- **Notation:** PlantUML sequence diagram with data labels.
- **Purpose:** trace the end-to-end exchange from barcode capture to product details and recommendations.
- **Success path:** capture/decode barcode, read optional bounded local history, resolve normalized product metadata, request generic or personalized ranking, query the vector index, return product details plus a separately classified recommendation outcome, and record a known interaction locally.
- **Outcome boundaries:** distinguish scan cancellation/unreadable input, backend/API unavailability, unknown product, temporarily unavailable resolution, personalized results, generic cold-start results, and empty/unavailable recommendations. Do not prescribe retries, error types, or recovery algorithms before those decisions are made.
- **Boundary rule:** show exchanged information and responsibility, not classes, endpoints, payload schemas, or timing guarantees that do not exist yet.

## Canonical terminology

PlantUML identifiers and operational documentation use the English canonical term. The report and presentation use the corresponding Serbian Latin label.

| Canonical term | Serbian deliverable label | Existing-source variants | Contract note |
| --- | --- | --- | --- |
| Android client | Mobilna aplikacija | Mobile application | Java/XML application exists; only the scanner slice is implemented. |
| Google Code Scanner | Google Code Scanner | Kamera/skeniranje; Lokalni skener barkoda; CNN; TFLite | Accepted for the initial MVP. Direct ML Kit Barcode Scanning with CameraX is only a possible custom-UI upgrade. |
| Local bounded history | Lokalna ograničena istorija | User profile; interaction history | Owned by the Android application; exact retention and representation remain undecided. |
| Backend API | Backend API / API servis | Backend; API service | Application-facing module inside one planned backend deployment; framework and transport remain undecided. |
| Product resolution | Razrešavanje proizvoda | Metadata adapter | Owns lookup, fallback selection, normalization, provenance, and product outcome classification. |
| Product metadata store | Katalog metapodataka o proizvodima | Baza proizvoda; barcode → metadata | Backend-owned logical catalog; storage product and external provider remain undecided. |
| Semantic search and recommendation component | Semantička pretraga i preporuke | Semantic search/recommendations | Internal backend module; embedding model, exact ranking method, and MMR remain undecided. |
| Vector index | Vektorski indeks | Embeddings store | Owned by the recommendation module; exact versus approximate search and storage technology remain undecided. |
| Product details | Podaci o proizvodu | Product; metadata | Concrete fields and schema remain undecided. |
| Recommendations | Preporuke | Top-N similar products | Ranking, personalization, and fallback behavior remain undecided. |

## Source and rendered-file layout

```text
docs/diagrams/
├── README.md
├── includes/
│   └── theme.puml
├── component-architecture.puml
├── scan-to-recommendation-flow.puml
└── rendered/
    ├── component-architecture.png
    ├── scan-to-recommendation-flow.png
    └── sr/
        ├── component-architecture.png
        ├── scan-to-recommendation-flow.png
        └── presentation/
            ├── component-architecture.png
            └── scan-to-recommendation-flow.png
```

- `.puml` files are canonical and must be reviewed as source.
- `includes/theme.puml` contains shared deterministic styling only; it must not contain architectural elements.
- PNG renders are committed because pdfLaTeX, the report, and the presentation need portable raster assets.
- Default renders use English canonical terminology for technical review. `SERBIAN` renders use Serbian Latin for formal deliverables.
- `PRESENTATION` renders remove secondary furniture and may collapse repeated response messages or nested branch detail to remain legible on a 16:9 slide. They retain every component/participant, responsibility, and named outcome class.
- All variants come from the same two canonical `.puml` sources. Do not duplicate architecture structure to localize or simplify a render.
- Rendered files must be regenerated whenever their source or shared theme changes.
- Diagram filenames remain stable so documentation references do not require churn.

## Styling and content rules

- Use UTF-8 source and Serbian Latin text only where a deliverable-facing label needs it.
- Keep colors and layout deterministic through the shared theme include.
- Visually distinguish device/application, single backend deployment, external dependency, and data-store boundaries.
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
- Backend framework, transport, and deployment environment.
- Product metadata provider, catalog storage product, caching policy, and fallback dataset.
- Embedding model, vector-index technology, exact/ANN search, and MMR use.
- Personalization event model, K/window, weighting, aggregation, retention duration, and consent wording. Generic cold-start behavior itself is required.
- Concrete API payloads, persistence schemas, retry policies, and service-level targets.

Resolving any of these requires its own approved task or subtask and a recorded decision.
