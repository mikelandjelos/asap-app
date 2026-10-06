# MVP verification log (T-011/S8, 2026-10-06)

Desktop: Ubuntu 24.04, Java 21, Python 3.13; phone: Samsung SM-A566B, Android 16, USB with `adb reverse`. Runtime bundle `20261006-1f6d112c`.

## Automated suites

| Suite | Result |
| --- | --- |
| Backend `./mvnw verify` (unit, Java↔Python parity, v2 HTTP contract, sources/router) | **55/55** pass, 0 skipped |
| Android clean build: `testDebugUnitTest`, `lintDebug`, `assembleDebug` | **41/41** pass, lint **0** issues, debug APK 8.6 MB |
| `ml/verify_bundle.py` (independent, sklearn-free) | **10/10** checks |

## Notebook reproducibility

Notebooks 00–03 were re-executed from scratch with clean kernels: 00 in 5 s, 01 in 499 s, 02 in 231 s, 03 in 75 s.
- **Deterministic fields:** for 00, 01 and 03, every deterministic summary field (metrics, selections, parameters, CIs) is **identical** to the committed results.
- **Artifacts:** embeddings, cluster labels, PCA and type embeddings are byte-identical.
- **One float-noise difference:** `cluster_centroids.npy` differs by at most **1.5×10⁻⁸** (min cosine 0.99999988). This is float32 rounding from multithreaded k-means summation; every cluster assignment and metric is identical.
- **Restored state:** the committed notebook outputs and artifacts were restored afterwards, so the repository stays consistent with the deployed bundle.

## Phone pass (instrumented `AsapTiming` log: request start → validated response)

| Path | Outcome | Phone latency |
| --- | --- | --- |
| Catalog product (10 opens, from Recent and by tapping a recommendation) | KNOWN, catalog | **45–170 ms**, median ≈ 56 ms; 170 ms on the first, cold request |
| Live product (user scan `5060466511552`) | KNOWN via Open Food Facts | 548 ms |
| Not in any source (3 user scans) | UNKNOWN; OFF and UPCitemdb both asked | 359 ms, 1,115 ms, 1,989 ms |
| Backend stopped | "Cannot reach ASAP" + Try again | 26 ms |
| Backend restarted, Try again | KNOWN | 167 ms |

The following were also checked on the phone during S7:
- every Product state;
- personalization switching to "For you" after 3 distinct products;
- the Analytics and History screens;
- light/dark themes and 200 % font;
- the accessibility audit (labels, 48 dp targets, contrast ≥ 5.5:1).

## Findings

- **Coverage is the main gap:** 3 of 4 real products scanned by the user in this pass were unknown to the catalog, Open Food Facts and UPCitemdb. This supports D-041 (larger catalog in a vector database before the report).
- **Live lookups** dominate latency (0.4–2 s), not the AI pipeline (≈ 10 ms on the backend).
- **Not done here:** a manual TalkBack walkthrough and multi-user testing, which are part of the post-report user testing (D-040).
