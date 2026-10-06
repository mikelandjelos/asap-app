# Session handoff

Last updated: 2026-10-06

## Start here — current authority and next action

Read `AGENTS.md` and its startup files, then `CURRENT_REQUIREMENTS.md`, `NOTEBOOK_VALIDATION.md` and `DOCUMENTATION_AUDIT.md`. D-024/D-025 override all earlier scope exclusions.

- **Required MVP:** semantic retrieval, clustering, history personalization, PCA, MMR and a polished Java/XML UI. No optional/extended-only interpretation for these features.
- **Notebook gate:** every AI/statistical component needs reproducible correctness/quality/performance evidence, including CNN/TFLite, embeddings, retrieval, clustering, history/profile aggregation, PCA, MMR and later additions. Preserve real tables/plots and connect report claims to exact runs. No such experiments have run yet.
- **Scanner last:** finish the PoC/MVP with Google Code Scanner, then replace only scanner-related parts with a CNN trained on a documented EAN/UPC dataset and deployed via TFLite. ZXing/another decoder or learned end-to-end decoding is still a choice to evaluate. Preserve downstream barcode/API/product/recommendation flow.
- **Presentation last:** finalization remains deferred. Existing deck and diagrams are earlier snapshots, explicitly inventoried in the audit; they are not current complete-scope specifications.
- **Next action:** S6 and the theme label pass are done and committed (bundle `20261006-07904a7f`; the user may still amend labels). Next: write the S7 plan and get approval.
  - **S7 scope:** Android v2 client, local history, Material 3 single-Activity UI (Scan / Product / Map "you vs themes" / History), attribution, accessibility and phone verification.
  - Report work is deferred. Commit at the end of every subtask.
- **User preferences:** fast, minimal useful increments; concise responses and targeted inspection; docs always synchronized; explicit approval for every subtask; short, clear one-line commits.
- **Current authorization:** documentation reconciliation, verification, commit and push of the accumulated handoff work only. Committing work does not accept pending tasks. Check `git status`/`git log` and remote state for actual publication outcome rather than inferring it from this pre-commit document.

## Preserved work and verification boundaries

The accumulated changes include T-009/S1 provider research, T-010/S1 report draft and D-024/D-025 handoff changes. Earlier presentation edits/PDFs are preserved, not newly finished. The report includes the repository link at its beginning, mandatory scope and notebook protocol. Marker R01–R06 requirements remain open.

Current report verification: 19 pages; three successful passes under both pdfLaTeX and LuaLaTeX, clean final logs, cover/repository URL and notebook protocol visually inspected. Local Markdown links and all six marker mappings pass; report PDF copies match and whitespace checks pass. Commit/push of this accumulated work is explicitly authorized; consult Git for the resulting hash and remote state.

Runtime is unchanged: deterministic Android/backend I1 with September evidence (32 Android tests, clean lint, 23 backend tests and representative phone cases). No live provider, genuine AI recommendations, clustering/PCA/MMR/personalization, custom CNN, polished final UI or executed notebooks exist. Current report-build/audit evidence is recorded in `DOCUMENTATION_AUDIT.md`; historical page counts and claims below refer only to their original stages.

## Historical evidence (preserved, not current scope/authorization)

Accepted prior milestones: T-007 closed on 2026-09-08 and committed as `6bd2682` (`Validate I1 vertical slice`); T-008/S1 accepted and committed as `5d28d23` (`Define product domain model`); T-008/S2 accepted and T-008 closed on 2026-09-08, with model/diagram work in `872e842`. T-009 plan/S1 were explicitly authorized on 2026-09-08, but S1 acceptance and S2 execution remain pending. T-010 plan/S1 were authorized on 2026-10-06; S1 acceptance remains pending. The earlier D-024-only handoff built an 18-page report with pdfLaTeX and did not commit; D-025 now explicitly authorizes committing/pushing accumulated work.

## T-010/S1 result

- `report/report.tex` and `report/report.pdf` now contain a complete 17-page Serbian working draft with all five sections populated, existing canonical diagrams, technical evidence and limitations.
- R01–R06 visibly mark conditional future completion prose for real data, semantic retrieval, history, instructor review, final demo and user evaluation. No measurements or feedback were fabricated. `docs/REPORT_COMPLETION.md` is the marker/evidence register and submission/defense checklist.
- D-023 distinguishes the deadline mock milestone from full AI MVP acceptance; TODO, status, plan, scope, README/hub and AGENTS reflect actual state. No Android/backend implementation changed or runtime tests rerun.
- Both pdfLaTeX and LuaLaTeX compile repeatedly to 17 pages with clean final logs. Cover and pages 13, 14, 16 and 17 were visually checked; all marker blocks and Serbian glyphs are readable. Marker/register correspondence and local links/paths checked; `git diff --check` passes. Built report copied to its deliverable path.
- No commit was requested or created. Earlier dirty T-009 and presentation work remains preserved.

## T-009/S1 result

- `docs/PRODUCT_DATA_API_EVALUATION.md` is the dated source of truth for the provider investigation. It defines mandatory legal/access/identity/model/outcome/scope gates and comparative completeness/provenance/operational criteria.
- The official-source shortlist is Open Food Facts, Barcode Lookup, and UPCitemdb. Open Food Facts and UPCitemdb may receive bounded read-only S2 probes after separate approval. Barcode Lookup remains documentation-only because it needs an account/key and prohibits automated use of a free test account.
- The reproducible corpus contains 12 public product-bound values across EAN-8, UPC-E, EAN-13, UPC-A and food, beverage, regional food, personal-care, cosmetics, hardware, and media categories. Check digits and the UPC-E expansion were verified locally.
- The defined S2 protocol would make 13 sequential product/control lookups per accessible provider, 26 total, paced at least 11 seconds apart. It forbids retries, search, image downloads, raw-payload retention without permission, and every local `200…` restricted-circulation fixture.
- UPCitemdb's trial caching/redistribution terms remain unclear; raw responses therefore cannot be retained. No provider has been selected. No live product endpoint, account, credential, dependency, application code, or I1 contract changed.
- Official links and local paths were checked; all 12 corpus check digits and the UPC-E expansion were independently recomputed. `git diff --check` and scans for secrets/restricted fixtures pass. The 12-page report builds twice under pdfLaTeX and LuaLaTeX, and the 19-slide presentation builds twice under the ELFak LuaLaTeX workflow. Final logs are warning-free; report page 11 and presentation slides 1 and 12 were visually inspected without clipping.

## T-008/S2 result

- `docs/DOMAIN_MODEL.md` now defines the accepted anonymous device-owned `PRODUCT_VIEWED` event, optional newest-first bounded `HistoryContext`, server-derived cold-start/sufficient readiness, and explicit data-minimization/privacy gates.
- Recommendation outcomes distinguish non-AI `DETERMINISTIC_FIXTURE`, AI-derived cold-start `GENERIC_SEMANTIC`, and history-applied `PERSONALIZED_HISTORY`, while retaining independent result/empty/unavailable/not-applicable states.
- Ranked AI items require typed finite score evidence and model/pipeline versioning, but scores are response-local and may not be presented as percentages without calibration.
- Factual product data, device observations, embeddings, request-scoped history profiles, and AI ranking evidence are structurally separate. AI-derived data never overwrites product facts.
- `docs/diagrams/domain-model.puml` and `docs/diagrams/ai-enrichment-flow.puml` are canonical sources for English technical, Serbian formal, and compact Serbian presentation renders.
- All four canonical sources pass default, Serbian, and Serbian-presentation syntax checks, and all 12 PNG variants regenerate successfully. The 12-page report builds twice under both pdfLaTeX and LuaLaTeX; the 18-slide presentation builds twice under LuaLaTeX. Final logs are warning-free, and report pages 8–10 plus presentation slides 1, 13, and 14 were visually inspected without clipping.
- S2 changes no Android/backend code, I1 payload, provider, dataset, persistence technology, embedding model, vector store, similarity metric, K/window, or ranking algorithm.

## T-008/S1 result

- `docs/DOMAIN_MODEL.md` defines the accepted provider-neutral product aggregate rather than treating API, fixture, Android, or future persistence shapes as the domain model.
- Stable internal product identity is separate from exact barcode value/format lookup identity. The MVP keeps one package barcode per product record and defers cross-package grouping.
- `id`, barcode, name, and provenance are mandatory. Bounded brand, category, description, and tags are optional so sparse source data remains honest.
- Provenance distinguishes `CONTROLLED_FIXTURE`, `EXTERNAL_PROVIDER`, and `FALLBACK_DATASET`; resolution remains `KNOWN`, `UNKNOWN`, or `UNAVAILABLE`.
- Normalization uses Unicode NFC, trims text, omits blank optional fields, preserves source language, and never fabricates missing metadata.
- The complete I1 fixture maps to the proposal, but its accepted wire contract and all Java/Android code remain unchanged.
- `git diff --check` and documentation path checks pass. The 9-page report builds twice under pdfLaTeX and LuaLaTeX, and the 16-slide presentation builds twice under LuaLaTeX; logs are clean, and the changed report page and product-model slide were visually inspected without clipping.

## S4.4 result

- Clean Android verification passed 32/32 unit tests, lint with zero findings, debug APK assembly, and release-manifest processing.
- Clean backend verification passed 23/23 tests and produced the executable JAR. The running JAR returned the exact primary controlled fixture in a localhost smoke test.
- A fresh debug APK was installed on the authorized Samsung SM-A566B running Android 16/API 36. `adb reverse tcp:8080 tcp:8080` connected device loopback to the local backend.
- Scanning `docs/fixtures/i1-known-product-ean13.svg` displayed barcode `2000000000015`, the known oat product, two ranked almond/soy results, and the exact label “Deterministički demo rezultat — nije AI preporuka”.
- Physical UI-hierarchy checks also confirmed: unknown product with recommendations not applicable; known product with empty recommendations; known product retained when recommendations are unavailable; and backend unavailability with previous outcome content cleared.
- Product-unavailable and malformed-request branches remain covered by automated tests rather than physical scanning, as allowed by the approved representative-case plan.

## Cleanup and reproducibility

- The backend was stopped gracefully; port 8080 was confirmed free.
- ADB reverse forwarding and the phone-side UI hierarchy dump were removed.
- The temporary local barcode-display page was deleted. The debug APK intentionally remains installed.
- No device serial number, UI dump, or temporary barcode file was persisted in the repository.
- Repeatable build, backend-run, ADB-forwarding, installation, and physical-validation notes are in `docs/WORKFLOW.md`.

## Current repository state

- The controlled I1 vertical slice is implemented and physically validated from Google Code Scanner through the Android client and local Spring Boot backend to rendered product/result states.
- Android has 32 passing local tests and zero lint findings; backend has 23 passing tests.
- The I1 data and ranked results remain deterministic local fixtures, not live metadata, semantic similarity, or personalized recommendations.
- External product providers, durable catalog/vector storage, embeddings, semantic ranking, bounded interaction history, and personalization remain unimplemented.

## Next concrete action

Get T-011/S1 accepted, then obtain explicit approval for T-011/S2. S2 involves the network: at most 26 read-only probe calls and an OFF export download, so recheck the provider terms first. No live-provider probe, download or code change is authorized yet. Report rewrite and presentation come after the MVP.

## Environment caveat

PlantUML must be invoked with `env -u DISPLAY` in this environment. The physical phone is the preferred runtime target because emulator acceleration remains unavailable without KVM.
