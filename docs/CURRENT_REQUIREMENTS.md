# Current requirements — handoff authority

Updated 2026-10-06. Accepted user directions D-024 and D-025 override earlier exclusions and the historical 80/95-point scorecard. This is scope approval, not permission to implement unapproved subtasks.

## Required outcome

- Supported EAN/UPC scanning, product metadata, semantic embeddings and similarity retrieval.
- History-based personalization with a genuine effect on ranking and explicit cold start.
- Clustering, PCA and MMR as actual MVP components, not optional future work. Their precise roles, parameters and integration must be selected in the revised plan.
- A polished, coherent Java/XML UI, not merely the existing technical test screen. Agree screens and visual direction; verify readable hierarchy, loading/empty/error states, accessibility and phone usability. No redesign or UI library is selected yet.
- Reproducible notebook testing of **every AI/statistical component**, including CNN, embeddings, similarity/ranking, clustering, personalization/profile aggregation, PCA, MMR and any later-added component. Use actual exported performance results in the report; see [NOTEBOOK_VALIDATION.md](NOTEBOOK_VALIDATION.md).

## Delivery order and scanner boundary

1. Finish the PoC/MVP, including the required AI/statistical features and UI, using Google Code Scanner temporarily.
2. Then replace only scanner-related parts with a CNN trained on a documented EAN/UPC dataset and deployed through TFLite. Detection plus ZXing/another decoder is allowed; learned end-to-end decoding is an alternative, not a selected approach. Dataset/license, training/splits, camera, decoder and model choices remain open.
3. Preserve barcode identity and downstream product/API/recommendation behavior. Verify notebook evidence plus real-device integration, conversion correctness and latency. A notebook does not substitute for a phone test.
4. Finish the presentation at the end. Interim mock demonstrations must remain labelled and do not satisfy full MVP acceptance.

## What exists, and what does not

Implemented: Java/XML Google Code Scanner client, deterministic Spring Boot fixture backend, integrated product/result UI and failure handling. September evidence: 32 Android tests, clean lint, 23 backend tests and representative physical-phone scenarios.

Not implemented: live provider/fallback, embeddings/vector storage, semantic retrieval, clustering, personalization/history, PCA, MMR, polished final UI, custom CNN/TFLite scanner, or executed AI/statistical notebooks. No performance results may be inferred from fixture order or unit-test counts.

## Process and unresolved choices

- Propose a short revised delivery plan and first subtask; obtain explicit approval for each subtask. No automatic T-009/S2 or T-010/S2 execution.
- T-009/S1 provider research and T-010/S1 report draft await acceptance. Committing them preserves work; it does not imply acceptance.
- Update implementation, verification and affected docs atomically. Formal report/presentation remain Serbian Latin. Keep future-result wording visibly conditional until verified.
- Keep responses and inspection output concise. Commit messages are short, clear one-liners. Do not introduce dependencies, external services or training downloads without an approved plan and any required permissions.
- T-011/S1 proposes answers to the following open choices in [AI_MVP_DESIGN.md](AI_MVP_DESIGN.md) (pending acceptance; CNN choices stay open): source/license/fallback, embedding model/store, clustering target/method, PCA purpose/dimension, MMR relevance/diversity trade-off, history/privacy/window, UI direction, CNN dataset/architecture/decoder and concrete metric thresholds.
