# Report completion register

Last updated: 2026-10-06 — T-010/S1.

The [Serbian report](../report/report.tex) is a complete **working draft**, not a claim that the full AI MVP is finished. Its [PDF](../report/report.pdf) visibly marks conditional future-result wording. Existing runtime evidence dates from September 2026; drafting does not rerun those checks. All markers remain open.

## Marker register

| Marker | Missing work/evidence | Roadmap | Accurate submission alternative if unfinished |
| --- | --- | --- | --- |
| R01 | Accepted provider/license and fallback, adapter, provenance, known/unknown/unavailable tests; actual source/version/coverage | T-009 followed by I2 | Describe the five fictional fixtures only; real data remains planned. |
| R02 | Model/version, dataset, embeddings/storage, integrated top-N, measured quality/latency with protocol | I3 | State that result order is deterministic, with no semantic scores or quality measurements. |
| R03 | Privacy/retention decision, bounded history, chosen algorithm, cold-start tests and example where history changes rank | I4 | State no history is recorded and no personalization is implemented. |
| R04 | Actual instructor/team presentation date, roles, notes and resulting change decisions | Instructor review | State no presentation/feedback is recorded; ask the author for actual notes. |
| R05 | Fresh build/test evidence and demo package, actual final-demo date/outcomes and comments; distinguish host checks from phone checks | T-010/S2; final demonstration | Retain dated September evidence and explicitly state what has not been reverified/presented. |
| R06 | Agreed user-test protocol, actual date and anonymous participants, attempt counts, observations and measurements | I5 | Describe a proposed protocol and explicitly state no user study has been conducted. |

## Fast submission path

- [x] Fill all five report sections with implementation, design, limits and conditional completion wording.
- [x] Preserve the original four canonical diagrams and distinguish design from runtime capabilities.
- [ ] Accept T-010/S1 after reviewing the report draft.
- [ ] Separately authorize T-010/S2: package and reverify the existing mock demo, without a new AI stack.
- [ ] Obtain any course-required real presentation/user-study evidence from the author; missing evidence is not created by rewriting prose.
- [ ] In T-010/S3, replace unresolved conditional blocks with accurate limitations for submission, or complete and verify the corresponding work first. Do not merely hide/delete labels while retaining hypothetical results.
- [ ] Finish the presentation **at the end**, as explicitly requested; it is untouched during S1.
- [ ] Remove the cover's draft notice only after the intended submission version is reconciled and reviewed.

## Return to the implementation plan / defense preparation

Scope amendment D-024 supersedes older exclusions: clustering, personalization, PCA, MMR and polished UI are mandatory MVP requirements under D-024/D-025. Every AI/statistical component needs reproducible notebook performance evidence under `NOTEBOOK_VALIDATION.md`; the report must trace real tables/plots to those runs. Finish that PoC/MVP before the required dataset-trained CNN/TFLite scanner replacement; preserve downstream components. The report now records this direction, but detailed training/clustering methods and diagram extensions remain pending approved design. Do not treat the six original markers as an exhaustive implementation checklist: clustering and scanner training additionally require dataset/provenance, method, integration and real evaluation evidence. Presentation remains untouched until the end.

T-009/S1 research is preserved for the authorized handoff commit and still awaits acceptance. S2 live probes are not authorized by T-010. First propose the D-024 revised plan, then continue separately approved source/fallback, semantic retrieval, mandatory clustering/personalization/PCA/MMR, polished UI, notebook evaluation and finally scanner replacement work. No commitment is made that all of these fit the remaining days. Neither MMR nor PCA is optional. Existing six draft markers remain stable; the notebook matrix covers all added components and must be reconciled before submission.

## Maintenance

Every `\draftblock{Rxx}` in the source must have exactly one row here. Resolve a marker only with a dated implementation/test record or genuine supplied feedback; update TODO, status, plan, handoff and affected report prose atomically. Keep numerical results blank until observed, and preserve distinctions among completed implementation, accepted design, simulation and proposed evaluation.
