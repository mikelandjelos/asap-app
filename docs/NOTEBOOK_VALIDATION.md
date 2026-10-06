# AI/statistical notebook verification contract

Status: Required by D-025 (2026-10-06). Executed so far: `00_source_probes` (T-011/S2, provider statistics only). No AI-model experiment has run yet, and no model/algorithm selected by this document.

Every AI/statistical component must have reproducible notebook evidence before its performance is described as verified in the report. Cover existing candidate ideas and any component added later. A component may share a notebook with related stages if its tests and results are separately identifiable.

## Evidence matrix

These are planning criteria, not measured values or finalized thresholds.

| Component | Minimum experiment questions and evidence to plan |
| --- | --- |
| CNN detector and TFLite export | Dataset source/license, annotations and leakage-safe train/validation/test split; held-out detection precision/recall and suitable localization metric; difficult EAN/UPC cases; training versus exported-model agreement, model size and inference latency. |
| Decoder / end-to-end scanner | Exact barcode value and format correctness, decode success/failure rate, invalid/check-digit cases; isolate localization and decoding errors; phone timing separately from notebook timing. |
| Embedding model | Model/version, text preparation and missing-field handling, dimensions/finite vectors, labelled retrieval examples and baseline, retrieval quality and embedding time. |
| Similarity retrieval/ranking | Known-answer tests, top-N relevance against judgements, exclusions/ties/empty cases and query latency with corpus size. |
| Clustering | Chosen representation and rationale, cluster count/parameters, a suitable quality/stability assessment, visual/qualitative checks, degenerate cases and runtime. Do not infer semantic truth from a single internal score. |
| Personalization / history aggregation | Generic versus personalized baseline, cold-start cases, controlled histories that change ranking, quality where relevance judgements exist, privacy-safe/synthetic history provenance and runtime. |
| PCA | Scaling/centering choices, explained variance versus dimensions, reconstruction or task-preservation checks, projection plots and runtime. A plot alone is not a quality benchmark. |
| MMR | Relevance-only baseline versus diversified ranking, parameter sweep, relevance/diversity trade-off, duplicate/short-list/edge cases and runtime. |
| Any additional AI/statistical stage | Register its purpose, inputs/outputs, baseline, correctness/quality checks, timing and limitations before claiming completion. |

## Reproducibility and report linkage

- Future notebooks belong in `notebooks/`; create actual experiments only in approved implementation work, not empty files presented as completed experiments.
- Record environment/package versions, seeds, dataset version/checksum and lawful acquisition steps, split construction, configuration, hardware, sample sizes and run date. Separate training/tuning data from evaluation; fit preprocessing on the proper training split.
- Clean-kernel run-all must succeed. Preserve meaningful outputs plus portable tables/plots and a machine-readable result summary where appropriate. Do not commit secrets, private histories or unlicensed datasets.
- Distinguish correctness, quality, runtime and resource use. Give units, repetition count and cold/warm conditions; report uncertainty/limitations where warranted. Never equate desktop timing to phone timing.
- Map every report figure/table/claim to notebook path, component, configuration, dataset and run. Record unsuccessful runs and limitations; no invented measurements or user feedback.
- Production implementations must match the tested method/configuration, with parity checks where notebook and runtime languages differ. Automated API tests and device tests remain required alongside notebooks.
- Concrete datasets, metrics, thresholds and notebook decomposition require the revised plan and per-subtask approval. There are currently **no measured AI performance results** to insert into the report.
