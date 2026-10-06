# Decision log

Record accepted decisions here in chronological order. A decision is not a task: implementation may remain pending after a direction is accepted.

Latest scope authority: D-024/D-025 supersede earlier exclusions of clustering, PCA, MMR, personalization-as-only-an-extension, and the final scanner direction. Earlier entries remain historical decisions; their optionality and authorization statements apply to their original date/turn only.

## D-001 — Canonical software diagrams use PlantUML

- **Status:** Accepted
- **Date:** 2026-09-03
- **Context:** The handwritten project notes require a maintainable primary medium for software diagrams.
- **Decision:** Use PlantUML sources for canonical architecture and software-design diagrams.
- **Consequence:** The existing DOCX architecture image is historical input; it should eventually be replaced or accompanied by versioned PlantUML source.

## D-002 — Mermaid is allowed for small Markdown-native diagrams

- **Status:** Accepted
- **Date:** 2026-09-03
- **Context:** Some simple flows are most useful when visible directly in repository documentation.
- **Decision:** Use Mermaid for small explanatory diagrams in Markdown, while keeping detailed software diagrams in PlantUML.

## D-003 — Formal deliverables use Serbian Latin

- **Status:** Accepted
- **Date:** 2026-09-03
- **Context:** The source report and course materials are in Serbian.
- **Decision:** Maintain the report and presentation in Serbian Latin. Configure fonts explicitly so pdfLaTeX/LuaLaTeX do not drop accented characters.

## D-004 — Documentation is synchronized during implementation

- **Status:** Accepted
- **Date:** 2026-09-03
- **Context:** Work is expected to continue across interchangeable sessions.
- **Decision:** Treat implementation, verification, and every affected documentation update as one atomic subtask. Synchronize TODO, plan state, current status, decisions, architecture, handoff, report, and presentation wherever the change applies.
- **Consequence:** Documentation may never knowingly lag behind implementation. A subtask is not complete if repository state cannot be reconstructed accurately from documentation. After an interruption or discovered mismatch, reconciliation blocks new implementation.

## D-005 — Work advances through explicit task and subtask approval gates

- **Status:** Accepted
- **Date:** 2026-09-03
- **Context:** The user wants to review and iterate on plans and retain control over every transition in the work sequence.
- **Decision:** Plan every reasonable task decomposition before implementation. Execute only one explicitly approved subtask at a time, stop after reporting its evidence, and require explicit approval before the next subtask or task.
- **Consequence:** Overall plan approval freezes the agreed direction but does not authorize all subtasks. Verification and documentation updates belong to the active subtask; material scope expansion requires replanning.

## D-006 — Canonical diagram contract and file layout

- **Status:** Accepted
- **Date:** 2026-09-03
- **Context:** T-001 requires maintainable architecture and flow diagrams that remain honest about the absence of implementation and unresolved technology choices.
- **Decision:** Use the two-view PlantUML contract, canonical terminology, boundaries, headless rendering commands, and stable source/render layout defined in `docs/diagrams/README.md`.
- **Consequence:** S2 and S3 must implement that contract without choosing unresolved technologies. PNG renders are portable deliverables, while `.puml` files remain canonical.

## D-007 — Proposed logical component view

- **Status:** Accepted
- **Date:** 2026-09-03
- **Context:** T-001/S2 translated the proposal into a maintainable logical component view without selecting unresolved implementation technologies.
- **Decision:** Accept `docs/diagrams/component-architecture.puml` and its PNG render as the canonical proposed component architecture.
- **Consequence:** Future design changes must update the PlantUML source, render, architecture documentation, report, presentation, plan state, and handoff wherever affected. The view remains design intent, not evidence of implemented software.

## D-008 — Proposed scan-to-recommendation flow

- **Status:** Accepted
- **Date:** 2026-09-03
- **Context:** T-001/S3 documents the intended end-to-end exchange and important user-visible outcome classes without inventing unselected API or recovery details.
- **Decision:** Accept `docs/diagrams/scan-to-recommendation-flow.puml` and its PNG render as the canonical proposed scan-to-recommendation flow.
- **Consequence:** The flow distinguishes unreadable barcode, unavailable/missing metadata, and empty/unavailable recommendations. Concrete endpoints, payloads, retries, frameworks, and timing guarantees remain undecided.

## D-009 — Localized diagram variants share canonical sources

- **Status:** Accepted
- **Date:** 2026-09-04
- **Context:** The same proposed architecture must support English technical review and readable Serbian report and presentation outputs without creating divergent diagram definitions.
- **Decision:** Generate English technical, Serbian formal, and compact Serbian presentation renders from the same two canonical PlantUML sources through render-time flags.
- **Consequence:** Localization and presentation simplification may change labels and secondary diagram furniture only. Components, participants, messages, and outcomes must remain structurally consistent across variants.

## D-010 — Android MVP starts with Java, XML Views, and Google Code Scanner

- **Status:** Accepted
- **Date:** 2026-09-04
- **Context:** The primary goal is the fastest route to a working MVP. The developer already knows Java, while the initial barcode flow does not require a custom camera interface.
- **Decision:** Use Java for Android application code, XML-based Android Views for the customizable application UI, and Google Code Scanner for the initial barcode-scanning implementation.
- **Consequence:** Google Code Scanner owns only the launched scanning experience; the rest of the ASAP interface remains custom. Direct ML Kit Barcode Scanning with CameraX is the upgrade path if custom preview, overlays, or continuous scanning become necessary. Exact SDK levels, dependency versions, and project structure remain undecided.

## D-011 — Android tooling is installed user-locally from official distributions

- **Status:** Accepted
- **Date:** 2026-09-04
- **Context:** The host already has a suitable system JDK, while Android Studio and the Android SDK were absent. The setup should avoid unnecessary system-package changes and remain easy to inspect.
- **Decision:** Retain Ubuntu OpenJDK 21 and install Google's verified stable Android Studio and SDK distributions under the user's home directory. Expose stable commands through `/home/mih/.local/bin` instead of editing shell startup files.
- **Consequence:** Android Studio uses its bundled runtime, SDK commands use `/home/mih/Android/Sdk`, and Android projects carry their own Gradle Wrapper and Gradle JDK configuration. Application SDK levels and dependency versions are governed separately by D-012.

## D-012 — Android PoC uses a stable Android 16 build and minimal dependency baseline

- **Status:** Accepted
- **Date:** 2026-09-04
- **Context:** T-004 researched a minimal mutually compatible baseline for the fastest Java/XML Google Code Scanner PoC. The workstation currently has API 37 tooling, but Android 17 remains a preview target and the verified phone runs stable Android 16/API 36.
- **Decision:** Use AGP 9.3.2 with Gradle Wrapper 9.5.0; run Gradle on OpenJDK 21 while compiling Java source/target 17; set `compileSdk 36`, `targetSdk 36`, `minSdk 23`, and Build Tools 36.0.0. Use exact production dependencies Google Code Scanner 16.1.0, AppCompat 1.8.0, and ConstraintLayout 2.2.2, plus JUnit 4.13.2, AndroidX JUnit 1.3.0, and Espresso 3.7.0 for the initial test baseline. Limit repositories to `google()` and `mavenCentral()` and prohibit dynamic version selectors.
- **Consequence:** T-005/S1 installed Android Platform 36 and Build Tools 36.0.0, created the wrapper/project, and resolved the S1 dependencies. Material Views 1.14.0 is deferred unless a concrete widget/theme requires it; Android 17/API 37, AGP 9.4/Gradle 9.6, direct ML Kit Barcode Scanning 18.3.1, and CameraX are not part of the initial PoC.

## D-013 — Android PoC uses a repository-contained single application module

- **Status:** Accepted
- **Date:** 2026-09-04
- **Context:** T-005/S1 needs a minimal shell that can grow into the scanner experiment without turning the documentation repository root into a Gradle project.
- **Decision:** Place a Groovy-DSL Android project in `android/`, with one `app` module and namespace/application ID `rs.ac.ni.elfak.asap`. Commit the checksum-pinned Gradle Wrapper and keep generated builds, local SDK paths, and Gradle caches ignored.
- **Consequence:** The PoC has one launcher activity and XML screen. New modules or a different application ID require an explicit later decision; scanner integration remains T-005/S2.

## D-014 — MVP delivery has core, extended, and full-scope checkpoints

- **Status:** Accepted
- **Date:** 2026-09-05
- **Context:** The fastest useful MVP must remain small without dropping history-based recommendation from the intended project.
- **Decision:** Treat the complete scan-to-semantic-similarity path as the mandatory 80-point core MVP, history-based personalized ranking as the committed 15-point extended MVP, and broader evaluation/feedback as the final 5 points. Use bounded recent history as the baseline personalization concept, but defer the exact window, weighting, aggregation, and retention choices until they can be evaluated.
- **Consequence:** Core-MVP acceptance does not close the personalization scope. The extended MVP must demonstrate that controlled history changes ranking and must preserve an explicit generic cold-start fallback.

## D-015 — MVP uses device-owned history and one backend modular monolith

- **Status:** Accepted
- **Date:** 2026-09-05
- **Context:** The MVP needs explicit deployment, data-ownership, and failure boundaries without incurring premature distributed-system or account-management work.
- **Decision:** Deploy one Android application and one backend modular monolith. Keep API coordination, product resolution, and recommendation as internal backend modules. Store bounded interaction history on the device and supply it only as optional request context; do not retain a central user profile. Give the product-resolution module ownership of the normalized catalog and the recommendation module ownership of the vector index as logical stores.
- **Consequence:** The first backend implementation remains operationally simple while module boundaries permit later extraction if evidence justifies it. Product and recommendation outcomes stay independent, generic similarity is the cold-start fallback, and provider, framework, schema, model, physical storage, and retention details remain later decisions.

## D-016 — Backend baseline uses Java 21, Spring Boot MVC, and Maven Wrapper

- **Status:** Accepted
- **Date:** 2026-09-05
- **Context:** T-007 needs the fastest maintainable path to a tested deterministic REST/JSON slice, the developer knows Java, and OpenJDK 21 is already installed.
- **Decision:** Use OpenJDK 21, Spring Boot 4.1.1 with `spring-boot-starter-webmvc`, and Maven 3.9.16 through Maven Wrapper 3.3.3. Place one Maven project under `backend/` with group `rs.ac.ni.elfak.asap`, artifact `asap-backend`, and base package `rs.ac.ni.elfak.asap.backend`. Use Maven Central, Spring-managed transitive versions, and the minimal dependency policy in `docs/BACKEND_BASELINE.md`.
- **Consequence:** T-007/S2 may define the wire contract without scaffolding. T-007/S3 will create the project and download the accepted build/runtime/test graph. Javalin and Quarkus remain deferred, as do persistence, Actuator, Lombok, Spring Modulith, Spring AI, external-provider, and vector-store dependencies.

## D-017 — Deterministic I1 uses an explicit scan-query contract and controlled barcodes

- **Status:** Accepted
- **Date:** 2026-09-05
- **Context:** The first vertical slice must prove Android-to-backend behavior reproducibly without making claims about live metadata, semantic similarity, or personalization.
- **Decision:** Use `POST /api/v1/scan-queries` with scanner-provided barcode value/format, exact GS1 validation, independent product and recommendation outcomes, and RFC 9457 request errors. Use the versioned local fixture set and restricted-circulation EAN-13 codes in `docs/fixtures/`. Every known-product recommendation outcome is marked `DETERMINISTIC_FIXTURE` and `placeholder: true`, carries no AI score, and requires an explicit non-AI demo label.
- **Consequence:** S3 must implement all nine contract cases while preserving known-product details when recommendation results are empty or unavailable. Fixture codes may never be sent to an external product provider. History, providers, embeddings, vector search, scores, and Android integration remain deferred.

## D-018 — Android I1 boundary uses Retrofit, Moshi, and debug-only ADB transport

- **Status:** Accepted
- **Date:** 2026-09-07
- **Context:** Android needs a small type-safe Java boundary for the frozen HTTP/JSON operation, complete outcome validation, asynchronous/cancellable calls, and a low-friction physical-phone development path without prematurely connecting the scanner UI.
- **Decision:** Use Retrofit 3.0.0 with converter-moshi 3.0.0, resolved Moshi 1.15.2, and an explicit OkHttp 5.3.2 override behind the app-owned `ScanQueryClient` interface. OkHttp 5.5.0 was evaluated but rejected because its Android artifact requires compile SDK 37, conflicting with the accepted API-36 baseline. Use `http://127.0.0.1:8080/` plus `adb reverse tcp:8080 tcp:8080` only in debug builds; release builds deny cleartext and use a non-routable HTTPS placeholder until hosting is selected.
- **Consequence:** S4.2 can coordinate scans without binding UI code directly to Retrofit and can cancel stale calls. Response conversion/validation occurs off the UI thread, while S4.2 must explicitly marshal visible state changes to the main thread. No production endpoint, authentication, retry policy, scanner invocation, or result UI is selected by this decision.

## D-019 — A coordinator owns Android scan-to-request lifecycle

- **Status:** Accepted
- **Date:** 2026-09-07
- **Context:** Scanner callbacks originate in `MainActivity`, while I1 callbacks complete asynchronously and may arrive after a newer scan or activity destruction.
- **Decision:** Route successful supported scans through a pure-Java `ScanQueryCoordinator`. It owns the active cancellable call, rejects callbacks that no longer belong to the current request, and uses an injected executor before delivering API state to the view. Scanner cancellation and failures remain local and never trigger the client.
- **Consequence:** Coordination is unit-testable without an Android runtime, visible state is changed on the main thread, and S4.3 can add outcome rendering without moving transport/lifecycle policy into UI widgets. S4.2 does not add retries, preserve requests across activity recreation, or render product/recommendation content.

## D-020 — I1 product and recommendation outcomes render independently

- **Status:** Accepted
- **Date:** 2026-09-07
- **Context:** The frozen response carries an independently useful product outcome and recommendation outcome; partial recommendation failure must not hide known product data.
- **Decision:** Use one scrollable XML outcome area with separate product and recommendation sections. Map the validated union into a pure-Java UI model, render every state explicitly, and show the exact deterministic-demo label whenever `placeholder` is true. Recommendation rows contain rank and product summary only.
- **Consequence:** Known product data remains visible for empty or unavailable recommendations, unknown and unavailable products remain distinguishable, and the I1 UI cannot imply an AI similarity score. S4.3 does not add richer styling, navigation, persistence, or physical integration evidence.

## D-021 — MVP product model is source-neutral and permits sparse metadata

- **Status:** Accepted
- **Date:** 2026-09-08
- **Context:** I2 needs a stable product concept before selecting an external provider or fallback dataset. The complete I1 fixture shape cannot be assumed for every real record, and transport/provider/persistence representations must not become the domain model accidentally.
- **Decision:** Model one scannable item or package variant with a stable opaque ASAP ID, exact validated barcode value/format, name, and provenance as required fields. Brand, category, description, and bounded ordered tags are optional. Keep internal identity distinct from barcode and provider identity; normalize source text without translating, generating, or fabricating missing facts; preserve `KNOWN`, `UNKNOWN`, and `UNAVAILABLE` resolution outcomes.
- **Consequence:** The complete I1 records map to the model without changing the frozen HTTP contract. Future providers may supply sparse but honest records. Provider/fallback precedence, storage, images/nutrition, cross-package grouping, embeddings, interactions, and recommendations remain separate decisions.

## D-022 — MVP recommendation context is anonymous, bounded, and factually separated from AI output

- **Status:** Accepted
- **Date:** 2026-09-08
- **Context:** I3 and I4 need stable interaction and recommendation concepts before selecting persistence, embedding, retrieval, or ranking technologies. The model must support generic cold start and history-aware ranking without creating accounts, a durable backend profile, or misleading AI claims.
- **Decision:** Record only device-owned `PRODUCT_VIEWED` events for displayed known products and send an optional newest-first bounded `HistoryContext` per request. Derive cold-start/sufficient readiness on the backend. Distinguish non-AI `DETERMINISTIC_FIXTURE`, AI-derived `GENERIC_SEMANTIC`, and history-applied `PERSONALIZED_HISTORY` modes; keep recommendation status independent from product resolution. Treat embeddings, request-scoped history profiles, scores, and AI-ranked order as derived artifacts that never overwrite source-backed product facts. Require typed finite score evidence and model/pipeline versioning for future AI modes, without interpreting uncalibrated scores as percentages.
- **Consequence:** T-008 closes with canonical class and AI-lineage diagrams and leaves I1 unchanged. K/window, sufficiency threshold, retention and deletion policy, persistence, provider/dataset, embedding model, vector store, similarity metric, ranking algorithm, calibration, and API evolution remain separate approved decisions before implementation.

## D-023 — Deadline draft and mock demo preserve the full implementation roadmap

- **Status:** Accepted
- **Date:** 2026-10-06
- **Context:** The user needs a report quickly, permits simulation for the near-term demo, and wants to return to the original plan afterward.
- **Decision:** Execute T-010 as a bounded deadline detour using existing I1 code. Complete report prose now; hypothetical completed-tense passages must remain inside visible conditional draft blocks with evidence requirements. Do not fabricate measurements, meetings or user feedback. Keep the full AI scope and T-009 work intact. Finish the presentation only at the end, per the user's explicit follow-up.
- **Consequence:** T-010/S1 is authorized; later subtasks still require separate approval. The mock milestone is not acceptance of the 80/95-point AI MVP. Submission must resolve markers through evidence or replace unsupported passages with limitations. No new provider/model/runtime technology is selected.

## D-024 — Mandatory clustering/personalization; trained CNN/TFLite scanner replacement last

- **Status:** Accepted scope direction; implementation not authorized
- **Date:** 2026-10-06
- **Decision:** Clustering and history-based personalization are essential MVP requirements, not optional or merely extended-MVP features. Keep Google Code Scanner temporarily to finish the PoC/MVP. After that, replace only scanner-related parts with a CNN trained using a documented dataset for EAN/UPC barcode detection and deployed with TFLite. ZXing or another decoder may decode detected regions; learned end-to-end decoding is an alternative to evaluate, not a selected implementation.
- **Boundaries:** Preserve the product/API/recommendation flow and custom Java/XML app when replacing scanning. Dataset/license, annotations/splits, training method/model, detector-versus-end-to-end choice, decoder, camera integration and evaluation must be planned and approved later. No training or replacement has happened. Clustering target, algorithm and user-visible purpose also remain to be defined.
- **Supersedes:** D-010's eventual scanner direction and D-014's acceptance rule allowing an MVP without personalization; any clustering exclusion in prior scope/diagrams. The old 80/95-point scorecard is historical and must not certify the revised MVP. MMR and PCA remain optional.
- **Handoff:** Stop after recording this change. No new implementation, provider calls, training, downloads or commits are authorized. Presentation remains deferred until the end. Propose a concise revised plan next and obtain explicit per-subtask approval. Keep responses and inspection output economical.

## D-025 — Required PCA, MMR, polished UI and notebook evidence

- **Status:** Accepted scope and documentation/publishing direction; feature implementation not authorized
- **Date:** 2026-10-06
- **Decision:** PCA, MMR and a polished UI join clustering and personalization as mandatory MVP capabilities. Every AI/statistical component (CNN/TFLite, embedding, retrieval, clustering, history aggregation/personalization, PCA, MMR and any later addition) must be tested and verified in reproducible notebooks, with actual performance results traceable in the report.
- **Evidence contract:** `CURRENT_REQUIREMENTS.md` consolidates current scope; `NOTEBOOK_VALIDATION.md` records the component matrix, reproducibility, datasets/splits, correctness/quality/timing distinctions and report linkage. Detailed methods/metrics/thresholds remain subject to approved planning; no experiment is claimed complete.
- **Supersedes:** All prior optional PCA/MMR language, including D-024's last scope sentence. The CNN/TFLite scanner is still developed last, after the working PoC/MVP; replace only scanner-related components. Presentation finalization remains deferred.
- **Authorization:** Audit and synchronize documentation, preserve earlier uncommitted work, add the repository link at the beginning of the report, verify, commit with a short one-line message, and push to the configured origin/current branch. This does not accept pending S1 results or authorize subsequent implementation.

## D-026 — Full AI MVP first; report rewrite and T-010 remainder deferred

- **Date:** 2026-10-06
- **Decision:** The user accepted T-010/S1 as a working draft and approved the revised T-011 AI MVP delivery plan. Finishing the full MVP is the top priority. T-010/S2 (mock demo) and T-010/S3 (submission package) are deferred; the report will be substantially rewritten after the MVP using real results, followed by the presentation.
- **Expansion:** If the MVP, report and slides finish with time remaining, the work may be extended and polished further under new approved plans.
- **Authorization:** Plan approval only. Each T-011 subtask, including S1, requires separate explicit approval. T-009/S1 acceptance remains pending user review.

## D-027 — Proposed AI MVP design (T-011/S1)

- **Date:** 2026-10-06
- **Status:** Accepted 2026-10-06 (“I agree with pretty much everything from the design”), amended by D-028.
- **Decision:** Adopt the design in `AI_MVP_DESIGN.md`:
  - multi-provider `ProductSourceAdapter` + router with field-level merge and per-field provenance (user proposal; local catalog, Open Facts family, transient UPCitemdb);
  - a filtered Open Food Facts export as the offline recommendation catalog;
  - `multilingual-e5-small`, compared against TF-IDF and MiniLM, served via ONNX Runtime in the Java backend with a parity gate;
  - in-memory exact cosine search;
  - spherical k-means clusters used for themes, the multi-interest profile, map colouring and coverage;
  - PCA(2) map plus a dimensionality study;
  - recency-weighted history profile blended with β;
  - MMR over the top 50 with a λ sweep;
  - four notebooks backed by a shared `ml/asap_ml` package;
  - API v2 alongside the frozen v1;
  - a Material 3 single-Activity UI with a JSON-file history.
- **Rationale:** This keeps the accepted single-backend topology, adds no external services, uses openly licensed data, and makes every AI/statistical stage notebook-verifiable. Values marked *initial* are fixed by notebook evidence.
- **Consequences:**
  - new dependencies are introduced only in their implementing subtasks (ONNX Runtime/DJL tokenizers in S6, Material Components in S7);
  - the T-008/S1 provenance object is revised in S6;
  - presentation renders stay deferred.

## D-028 — English app text; vector database after the MVP

- **Date:** 2026-10-06
- **Decision:**
  - All Android app text and the controlled fixture data are in English. The user asked to correct existing Serbian text first and to continue in English. The formal report and presentation remain Serbian Latin (course deliverables); the report quotes the English UI label.
  - A real database with vector search is wanted after the MVP: PostgreSQL + pgvector or Qdrant, to be chosen then. The MVP keeps in-memory exact search behind a retrieval interface so the swap stays local.
- **Effect on I1:** the wire schema is unchanged; only fixture strings and the placeholder label text changed. September physical evidence was recorded with the earlier Serbian label.

## D-029 — Provider priority and offline catalog source (T-011/S2)

- **Date:** 2026-10-06
- **Decision:** The product router queries the local catalog first, then Open Food Facts v3 (`product_type=all`, cacheable with ODbL attribution), then UPCitemdb trial (transient, quota-guarded, UPC-E expanded to UPC-A). Barcode Lookup is not used. The offline recommendation catalog consists of 10,000 products from the official OFF, Open Beauty Facts and Open Pet Food Facts CSV exports, selected by `ml/asap_ml/catalog.py` (seed 20261006).
- **Evidence:** In the 12-product probe, field-level merge reached 100 % name/brand/category coverage, against 58–67 % for either provider alone. Details are in `PRODUCT_DATA_API_EVALUATION.md` (S2 results) and `notebooks/00_source_probes.ipynb`.
- **Constraints:** Raw exports and processed bundles are git-ignored and reproducible; `data/catalog_manifest.json` records checksums and config. The attribution "Contains data from Open Food Facts, available under the Open Database License" must appear in the app.

## D-030 — Hybrid retrieval: e5-small + character TF-IDF (T-011/S3)

- **Date:** 2026-10-06
- **Status:** Proposed with the S3 results; accepted when the user accepts S3.
- **Decision:**
  - The embedding model is `intfloat/multilingual-e5-small` at revision `614241f…`, served through the ONNX path verified for parity.
  - Retrieval relevance is `r(x) = 0.9·cos_e5 + 0.1·cos_tfidf_char` (char_wb 3–5-grams, sublinear TF, catalog-fitted). It replaces pure embedding cosine in the design.
  - Production text is the `full` variant. α was tuned on leak-free text, because category words make full-text tuning degenerate (α = 0).
- **Evidence:** On 500 leak-free test queries (`notebooks/01_data_embeddings_retrieval.ipynb`, α tuned on a disjoint 500-query set):
  - nDCG@10: hybrid 0.307, e5 0.288, char TF-IDF 0.291, MiniLM 0.206.
  - Hybrid minus e5 is +0.018 (95 % CI 0.012–0.025); hybrid minus TF-IDF is +0.016 (95 % CI 0.008–0.025).
  - e5 alone failed the provisional "beats TF-IDF" gate.
- **Consequences:**
  - S6 adds a Java char-TF-IDF analyzer, with the vocabulary and idf exported from Python, and a parity test.
  - The ONNX model is 470 MB fp32; quantization can be evaluated later if size matters.
  - Category agreement remains a proxy; human judgements are a recommended addition before the final report.

## D-031 — Clustering in product-type space; revised k rule; no PCA reduction (T-011/S4)

- **Date:** 2026-10-06
- **Decision:**
  - Spherical k-means (`n_init` = 20, seed 20261006) runs on e5 embeddings of the English OFF taxonomy text, with **k = 60**. It does not use the full-text retrieval embedding.
  - The revised selection rule picks k ∈ [20, 60] by the best average rank of silhouette, Davies–Bouldin, NMI vs top-level categories and stability.
  - PCA(2) of this space drives the product map. Retrieval keeps full 384-d vectors (no PCA reduction).
- **Rationale and process:**
  - The pre-declared rule (stability ≥ 0.7 first) selected full-text k = 15, whose clusters grouped products by language/country (median purity 0.23).
  - The user chose the product-type option for user experience (“yeah”, 2026-10-06), after seeing both results.
  - The original rule's choice is kept in `notebooks/02_clustering_pca.ipynb`.
- **Evidence (k = 60):** silhouette 0.279, Davies–Bouldin 2.20, NMI(top) 0.724, median purity 0.78, seed ARI 0.618. HDBSCAN marks 31 % of products as noise.
- **Limitations:**
  - Stability is moderate, so a fixed versioned clustering is shipped.
  - Agreement with categories is partly by construction.
  - The auto-labels need a human pass.
  - k sits at the edge of the searched range.

## D-032 — Multi-interest personalization and MMR parameters (T-011/S5)

- **Date:** 2026-10-06
- **Decision:**
  - The profile is the history interest centroid (per type cluster, D-031) closest to the query.
  - Weights are uniform (half-life ∞), with a 20-event window and `SUFFICIENT` readiness at ≥ 3 distinct catalogued products.
  - Scoring is `s = 0.6·r(q,x) + 0.4·cos_e5(p,x)` over the hybrid relevance r (D-030).
  - MMR runs over the top 50 to 10 results with λ = 0.6, using e5 cosine for similarity.
- **Evidence (`notebooks/03_personalization_mmr.ipynb`; 500 tuning and 1,000 disjoint test synthetic users):**
  - Multi-interest beat generic with nDCG@10 +0.007 (95 % CI 0.002–0.013), and interest share@10 rose from 0.78 to 0.81. The single centroid was not significant and hurt out-of-interest scans (−0.013).
  - With fewer than 3 distinct products, ranking equals generic exactly.
  - MMR λ = 0.6 met the pre-declared ≤ 5 % nDCG-loss rule: −4.9 % nDCG, +21 % ILD, cluster coverage 1.9 → 2.6.
- **Limitations:**
  - Users and relevance are synthetic and category-derived, so the effect size is modest and not evidence of real-user satisfaction.
  - Multi-interest grouping and interest definitions are correlated.

## D-033 — PCA "you vs themes" view in the MVP; friends view and analytics dashboard later

- **Date:** 2026-10-06
- **Decision (user):**
  - The MVP map shows the user's profile centroid against the product-theme centroids (PCA(2), type space). Details are to be discussed before S7.
  - "You vs other users (friends)" is deferred to after the MVP, because it needs accounts and social data.
  - A personal-analytics dashboard, next to the history and PCA view, is built in the end phase of the MVP, before the report; its contents are still to be agreed.
- **Consequence:** S6a exports projected theme centroids and labels. The profile for the map uses type-space embeddings so that it shares the space of the clusters and the PCA.

## D-034 — Versioned runtime bundle with parity fixtures (T-011/S6a)

- **Date:** 2026-10-06
- **Decision:**
  - The backend loads one immutable, versioned bundle exported by `ml/export_bundle.py`: a JSON manifest with SHA-256 per file, catalog JSONL, raw little-endian float32 matrices, TF-IDF vocabulary/idf, themes, the map sample and the ONNX model + tokenizer.
  - Java correctness is defined by `fixtures/parity.json` and its tolerances: embedding cosine ≥ 0.999, TF-IDF 1e-6, scores 1e-4, x/y 1e-4.
  - The backend must refuse to start on a checksum mismatch. A refit (drift task) produces a new bundle version.
- **Rationale:** simple formats every language reads without extra libraries, an explicit link between the notebook evidence and runtime artifacts, and reproducible parity testing.
- **Note:** the bundle is git-ignored (the model alone is 470 MB). Regenerate it with the WORKFLOW commands.

## D-035 — ONNX Runtime Java and DJL tokenizers in the backend (T-011/S6b)

- **Date:** 2026-10-06
- **Decision:** The backend runs the e5-small encoder in-process with `com.microsoft.onnxruntime:onnxruntime` 1.30.0 and `ai.djl.huggingface:tokenizers` 0.38.0. Char-TF-IDF, hybrid scoring and top-k are plain Java ports of the Python reference.
- **Evidence:** `BundleParityTest` matches every fixture: TF-IDF within 1e-6 including Unicode edge cases, embedding cosine ≥ 0.999, and identical top-50 rankings and scores within 1e-4. Desktop timings: encode 9.2 ms; hybrid top-50 over 10k products 5.8 ms; startup indexing 4.8 s.
- **Consequences:** The JAR grows to ~101 MB because of the bundled natives. No Python sidecar is needed (the design fallback in §4 is not used). Parity tests skip, with an explicit reason, when the git-ignored bundle is absent.

## D-036 — Collapse same name+brand variants; MMR λ re-tuned to 0.7 (T-011/S6c.1)

- **Date:** 2026-10-06
- **Decision:**
  - Before MMR, keep one candidate per normalized (name, brand) variant group (best score, among the top 200) and drop the query's own variants.
  - Re-running the pre-declared λ rule then selects **λ = 0.7**, which supersedes D-032's λ = 0.6.
  - Bundle versions hash the parameters and notebook summaries as well as the matrices.
- **Evidence:**
  - In `notebooks/03_personalization_mmr.ipynb` (section 5b), test top-10 lists with variants fall from 5.2 % to 0 %, at a cost of −0.0015 nDCG@10.
  - MMR at λ = 0.7 costs −1.3 % nDCG for +13 % ILD.
  - Personalization is unchanged: +0.0072 nDCG@10 vs generic (CI 0.0017–0.0124).
  - Bundle `20261006-ee94fdb0` passes the verifier, and the Java suite passes 46/46.
- **Rationale:** User-visible duplicates (e.g. 5× the same Carrefour product) were the main quality defect found in the S6c smoke test. Different brands with the same name stay, because they are genuine alternatives.

## D-037 — Live product source router in the backend (T-011/S6d)

- **Date:** 2026-10-06
- **Decision:** Products missing from the catalog are resolved by `ProductRouter` and then ranked like catalogued products.
  - **Order and budget:** OFF v3 (`product_type=all`) first, then UPCitemdb trial. Total budget 3 s, 1.5 s per source; the router stops once name, brand and category are known.
  - **Merge:** field-level, first non-blank value in source order; the longest description wins. `fieldSources` provenance is exposed in v2.
  - **Caching and limits:** OFF results are cached in memory (24 h found / 1 h not found). UPCitemdb is never cached and runs under a local daily quota of 90 and, after the 2026-10-06 phone-test fix, a rolling burst window of 5 per minute (the documented limit is 6/min). The original 11 s fixed spacing made a second scan within 11 s `UNAVAILABLE`. UPC-E is expanded to UPC-A.
  - **Failure handling:** restricted codes are never sent out. A source's circuit opens for 60 s after 3 failures. Source failures yield `UNAVAILABLE`, never `UNKNOWN`.
- **Evidence:** `SourcesTest` and `V2ControllerTest` (no network) pass. A live check resolved Braun (UPCitemdb, 1.16 s), Nutella (OFF, 0.17 s; 0.07 s cached), Mlinci (OFF, 0.24 s) and Coke UPC-E (OFF, 0.13 s).
- **Limitations:**
  - The cache is in-memory only; persistence belongs to the post-MVP database/consolidator.
  - Externally resolved products are not in the catalog, so they do not contribute to history profiles.
  - Sparse provider records give weak recommendations.

## D-038 — Android UI direction and device history store (T-011/S7)

- **Date:** 2026-10-06
- **Decision (user choices):**
  - **Visual style:** fresh grocery-green Material 3, with light and dark themes.
  - **Navigation:** bottom tabs (Scan, Product, Map, History).
  - **Results:** one ranked list labelled "For you" (personalized) or "Similar products" (cold start), with a theme chip per row.
  - **Map:** 60 theme bubbles (size = theme size), a "You" marker, the last scanned product and the history as dots.
  - **History:** device-only JSON (newest 50, 90 days, clear action); the newest 20 events are sent. Local display fields stay on the device.
- **Rationale:** easiest to demo and discover; matches the v2 contract exactly (no extra backend computation); keeps the DOMAIN_MODEL privacy boundary.

## D-039 — "Analytics" tab replaces "Map"; PCA is its main chart

- **Date:** 2026-10-06
- **Decision (user):** The third bottom tab is **Analytics**, with a chart icon. The PCA "you vs themes" map (D-033) is its main chart. Further personal-analytics charts sit below it, which brings the end-phase personal analytics dashboard (D-033) into S7d. The exact secondary charts are agreed before S7d.
- **Supersedes:** the "Map" tab naming in D-038.

## D-040 — Delivery order: MVP → CNN scanner → new report → testing and demo

- **Date:** 2026-10-06
- **Decision (user):**
  1. Finish the MVP (S7c–S7e, S8).
  2. Implement the CNN/TFLite barcode scanner replacement **before** the report, because deep learning for barcode reading is a stated project component.
  3. Discard the current report draft and write a new report from scratch, following instructions the user will give.
  4. Only after the report: thorough user-perspective testing, demo preparation, presentation, deployment, and post-MVP work. Post-MVP work includes the refit pipeline, designed with Airflow-style orchestration.
- **Supersedes:** D-026/D-040 ordering details where they conflict, the T-010/S1 draft as the basis of the final report, and the plan to finish the presentation before testing.
