"""Independently verify an exported bundle using only its own files (T-011/S6a).

Re-implements the char_wb TF-IDF analyzer without sklearn (the reference for the Java port), reloads the raw
float32 matrices, recomputes every parity fixture except ONNX embeddings and checks checksums and tolerances.
"""
import hashlib
import json
import math
import re
import sys
from collections import Counter
from pathlib import Path

import numpy as np

WS = re.compile(r"\s\s+")


def char_wb_ngrams(text, lo=3, hi=5):
    """Mirror of sklearn's _char_wb_ngrams after str.lower(); indexes by code point."""
    text = WS.sub(" ", text.lower())
    grams = []
    for w in text.split():
        w = " " + w + " "
        for n in range(lo, hi + 1):
            offset = 0
            grams.append(w[offset:offset + n])
            while offset + n < len(w):
                offset += 1
                grams.append(w[offset:offset + n])
            if offset == 0:
                break
    return grams


class Tfidf:
    def __init__(self, spec):
        self.index = {t: i for i, t in enumerate(spec["vocabulary"])}
        self.idf = np.array(spec["idf"], dtype=np.float64)

    def transform(self, text):
        counts = Counter(self.index[g] for g in char_wb_ngrams(text) if g in self.index)
        if not counts:
            return np.array([], int), np.array([])
        idx = np.array(sorted(counts))
        val = np.array([(1 + math.log(counts[i])) * self.idf[i] for i in idx])
        return idx, val / np.linalg.norm(val)


def load_f32(path, shape):
    return np.fromfile(path, dtype="<f4").reshape(shape)


def candidates(scores, exclude, pool):
    s = scores.copy()
    s[list(exclude)] = -np.inf
    order = np.lexsort((np.arange(len(s)), -s))
    return order[:pool][np.isfinite(s[order[:pool]])]


def mmr(cand, scores, E, lam, n):
    cand = list(cand)
    sim = E[cand] @ E[cand].T
    max_sim = np.full(len(cand), -np.inf)
    remaining = np.ones(len(cand), bool)
    out = []
    for _ in range(min(n, len(cand))):
        pen = np.where(np.isfinite(max_sim), max_sim, 0.0)
        val = np.where(remaining, lam * scores[cand] - (1 - lam) * pen, -np.inf)
        j = int(np.argmax(val))
        out.append(cand[j])
        remaining[j] = False
        max_sim = np.maximum(max_sim, sim[j])
    return out


def same_ranking(got, want, scores, tol):
    """Exact match, tolerating swaps only between items whose scores differ by < tol."""
    if list(got) == list(want):
        return True
    return len(got) == len(want) and all(g == w or abs(scores[g] - scores[w]) < tol for g, w in zip(got, want))


def main(bundle_dir):
    b = Path(bundle_dir)
    man = json.loads((b / "manifest.json").read_text())
    checks = {}
    checks["checksums"] = all(hashlib.sha256((b / f).read_bytes()).hexdigest() == m["sha256"] for f, m in man["files"].items())
    cat = [json.loads(line) for line in (b / "catalog.jsonl").read_text(encoding="utf-8").splitlines()]
    mats = {f: load_f32(b / f, s) for f, s in man["matrices"].items()}
    E, TE, C = mats["embeddings.f32"], mats["type_embeddings.f32"], mats["cluster_centroids.f32"]
    mean, comps = mats["pca_mean.f32"], mats["pca_components.f32"]
    prm = man["params"]
    fx = json.loads((b / "fixtures/parity.json").read_text())
    tol = fx["tolerances"]
    checks["catalog_rows"] = len(cat) == man["products"] == len(E) and [c["index"] for c in cat] == list(range(len(cat)))
    checks["unit_norm_embeddings"] = bool(np.allclose(np.linalg.norm(E, axis=1), 1, atol=1e-4))
    checks["cluster_labels_match_nearest_centroid"] = all(int(np.argmax(C @ TE[c["index"]])) == c["cluster"] for c in cat)

    tf = Tfidf(json.loads((b / "tfidf.json").read_text()))
    worst = 0.0
    for case in fx["tfidf"]:
        idx, val = tf.transform(case["text"])
        if list(idx) != case["indices"]:
            worst = math.inf
            break
        worst = max(worst, float(np.abs(val - np.array(case["values"])).max()) if len(val) else 0.0)
    checks["tfidf_pure_python_parity"] = worst <= tol["tfidfAbs"]
    rows = [tf.transform(c["fullText"]) for c in cat]

    def lexical(q_idx, q_val):
        qd = dict(zip(q_idx.tolist(), q_val))
        return np.array([sum(qd.get(i, 0.0) * v for i, v in zip(ix, vl)) for ix, vl in rows])

    def relevance(qv, q_tf):
        return prm["alpha"] * (E @ qv) + (1 - prm["alpha"]) * lexical(*q_tf)

    ok = True
    for case in fx["retrieval"]:
        q = case["queryIndex"]
        r = relevance(E[q], rows[q])
        cand = candidates(r, {q}, prm["candidate_pool"])
        ok &= same_ranking(cand, case["candidates"], r, tol["scoreAbs"]) and np.allclose(r[case["candidates"]], case["scores"], atol=tol["scoreAbs"])
        ok &= same_ranking(mmr(case["candidates"], r, E, prm["mmr_lambda"], prm["results"]), case["mmrTop"], r, tol["scoreAbs"])
    checks["retrieval_and_mmr_parity"] = bool(ok)

    ok = True
    for u in fx["users"]:
        h, q = u["historyIndices"], u["queryIndex"]
        r = relevance(E[q], rows[q])
        ready = "SUFFICIENT" if len(set(h)) >= prm["min_distinct_history"] else "COLD_START"
        s = r
        if ready == "SUFFICIENT":
            labels = np.array([cat[i]["cluster"] for i in h])
            cents = []
            for c in np.unique(labels):
                v = E[np.array(h)[labels == c]].sum(0)
                cents.append(v / np.linalg.norm(v))
            p = max(cents, key=lambda v: float(v @ E[q]))
            s = (1 - prm["beta"]) * r + prm["beta"] * (E @ p)
        cand = candidates(s, {q, *h}, prm["candidate_pool"])
        ok &= ready == u["readiness"] and same_ranking(cand, u["candidates"], s, tol["scoreAbs"])
        ok &= same_ranking(mmr(u["candidates"], s, E, prm["mmr_lambda"], prm["results"]), u["mmrTop"], s, tol["scoreAbs"])
        if h:
            you = TE[h].mean(0)
            you = you / np.linalg.norm(you)
            ok &= bool(np.allclose((you - mean) @ comps.T, u["youXY"], atol=tol["xyAbs"]))
    checks["personalization_parity"] = bool(ok)
    checks["assignment_and_pca_parity"] = all(
        int(np.argmax(C @ TE[a["index"]])) == a["cluster"] and np.allclose((TE[a["index"]] - mean) @ comps.T, a["xy"], atol=tol["xyAbs"])
        for a in fx["assignment"])
    themes = json.loads((b / "themes.json").read_text())
    checks["theme_xy"] = len(themes) == prm["k"] and all(np.allclose((C[t["cluster"]] - mean) @ comps.T, [t["x"], t["y"]], atol=tol["xyAbs"]) for t in themes)
    checks["embedding_fixtures_unit_norm"] = all(abs(np.linalg.norm(c["vector"]) - 1) < 1e-4 for c in fx["embedding"])
    print(json.dumps({"version": man["version"], "checks": checks, "tfidf_max_abs_err": worst}, indent=1))
    return all(checks.values())


if __name__ == "__main__":
    root = Path(__file__).resolve().parents[1] / "data/processed/bundle"
    d = sys.argv[1] if len(sys.argv) > 1 else root / (root / "CURRENT").read_text().strip()
    sys.exit(0 if main(d) else 1)
