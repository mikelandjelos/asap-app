# Diagram contract

Status: T-001 and all four subtasks were accepted by the user; the canonical sources, render variants, and formal-deliverable integrations are complete.

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
- **Required elements:** Android client, on-device barcode scanner, backend API, semantic search and recommendation component, product metadata store, and vector index.
- **Boundary rule:** this is a logical component view, not a deployment view. It must not imply separate deployable services, protocols, frameworks, databases, or cloud providers that have not been selected.
- **Optional/planned behavior:** personalization may appear only as a clearly marked planned extension; its internal design is out of scope for T-001.

### Scan-to-recommendation flow

- **Source:** `scan-to-recommendation-flow.puml`
- **Notation:** PlantUML sequence diagram with data labels.
- **Purpose:** trace the end-to-end exchange from barcode capture to product details and recommendations.
- **Success path:** capture/decode barcode, request product metadata, retrieve metadata, request semantically similar products, query the vector index, and return product details plus recommendations to the user interface.
- **Outcome boundaries:** include high-level alternatives for an unreadable barcode, unavailable/missing product metadata, and empty/unavailable recommendations. Do not prescribe retries, error types, or recovery algorithms before those decisions are made.
- **Boundary rule:** show exchanged information and responsibility, not classes, endpoints, payload schemas, or timing guarantees that do not exist yet.

## Canonical terminology

PlantUML identifiers and operational documentation use the English canonical term. The report and presentation use the corresponding Serbian Latin label.

| Canonical term | Serbian deliverable label | Existing-source variants | Contract note |
| --- | --- | --- | --- |
| Android client | Mobilna aplikacija | Mobile application | Platform intent is Android; language and SDK remain undecided. |
| On-device barcode scanner | Lokalni skener barkoda | Kamera/skeniranje; Detekcija/čitanje barkoda (TFLite); CNN | Do not claim TFLite, CNN, or ML Kit as selected implementation. Use “technology TBD” where needed. |
| Backend API | Backend API / API servis | Backend; API service | Do not imply framework, protocol, or deployment topology. |
| Product metadata store | Skladište metapodataka o proizvodima | Baza proizvoda; barcode → metadata | External provider versus owned database remains undecided. |
| Semantic search and recommendation component | Semantička pretraga i preporuke | Semantic search/recommendations | Embedding model, MMR use, and service boundary remain undecided. |
| Vector index | Vektorski indeks | Embeddings store | Exact versus approximate search and storage technology remain undecided. |
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
- `PRESENTATION` renders remove secondary diagram furniture to remain legible on a 16:9 slide; they do not change components, participants, messages, or outcomes.
- All variants come from the same two canonical `.puml` sources. Do not duplicate architecture structure to localize or simplify a render.
- Rendered files must be regenerated whenever their source or shared theme changes.
- Diagram filenames remain stable so documentation references do not require churn.

## Styling and content rules

- Use UTF-8 source and Serbian Latin text only where a deliverable-facing label needs it.
- Keep colors and layout deterministic through the shared theme include.
- Visually distinguish client, backend, external dependency, and data-store boundaries.
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

## Explicitly unresolved

- Java versus Kotlin and the Android SDK baseline.
- ML Kit versus another barcode-scanning implementation.
- Backend topology, framework, transport, and deployment environment.
- Product metadata provider, ownership, caching, and fallback dataset.
- Embedding model, vector-index technology, exact/ANN search, and MMR use.
- Personalization event model, privacy policy, and cold-start behavior.
- Concrete API payloads, persistence schemas, retry policies, and service-level targets.

Resolving any of these requires its own approved task or subtask and a recorded decision.
