"""Export the versioned runtime bundle the Java backend loads (T-011/S6a).

Everything is recomputed from the committed code and git-ignored artifacts produced by notebooks 01-03, then
checked against the notebook summaries. Binary matrices are little-endian float32/int32, row-major.
Contains data from Open Food Facts, available under the Open Database License (ODbL 1.0).
"""
import hashlib
import json
import shutil
import sys
import time
from pathlib import Path

import numpy as np
from sklearn.feature_extraction.text import TfidfVectorizer

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from ml.asap_ml import ranking as K, retrieval as R, structure as S  # noqa: E402

ROOT = Path(__file__).resolve().parents[1]
P = ROOT / "data/processed"
RES = ROOT / "notebooks/results"
SEED = 20261006
TFIDF_PARAMS = dict(analyzer="char_wb", ngram_range=(3, 5), min_df=2, sublinear_tf=True)
TRICKY_TEXTS = ["Café Crème  BIO\tÖko-Müsli", "İstanbul ŞEKER ß straße", "a b cd", "", "   ",
                "Ljubičasto Ćevapi Đurđevak Žito", "香ばしい aa", "Coca-Cola Zero 0,5 l"]


def sha(path):
    return hashlib.sha256(Path(path).read_bytes()).hexdigest()


def write_f32(path, m):
    np.ascontiguousarray(m, dtype="<f4").tofile(path)


def sparse_row(X, i):
    row = X[i].tocoo()
    order = np.argsort(row.col)
    return {"indices": row.col[order].tolist(), "values": [round(float(v), 8) for v in row.data[order]]}


def main(out_root=P / "bundle"):
    nb = {n: json.loads((RES / d / "summary.json").read_text()) for n, d in
          [("01", "01_data_embeddings_retrieval"), ("02", "02_clustering_pca"), ("03", "03_personalization_mmr")]}
    cat = R.load_catalog()
    N = len(cat)
    E = np.load(P / "embeddings_e5-small.npy")
    TE = np.load(P / "embeddings_cluster_B_type.npy")
    clusters = np.load(P / "cluster_labels.npy")
    centroids = np.load(P / "cluster_centroids.npy")
    pca = np.load(P / "pca.npz")
    themes = json.loads((P / "cluster_themes.json").read_text())
    # consistency with the notebooks that produced these artifacts
    assert hashlib.sha256(E.tobytes()).hexdigest() == nb["01"]["embeddings_sha256"]
    for f in ("cluster_centroids.npy", "cluster_labels.npy", "pca.npz"):
        assert sha(P / f) == nb["02"]["artifact_sha256"][f], f
    assert nb["02"]["representation"] == "B_type" and centroids.shape[0] == nb["02"]["k"]
    ch = nb["03"]["chosen"]
    params = {"alpha": nb["01"]["hybrid"]["alpha"]["no_category"], "beta": ch["beta"], "mmr_lambda": ch["mmr_lambda"],
              "profile": ch["method"], "half_life": None if ch["half_life"] in (None, "inf") or ch["half_life"] == float("inf") else ch["half_life"],
              "history_window": ch["history_window"], "min_distinct_history": ch["min_distinct_history"],
              "candidate_pool": ch["candidate_pool"], "results": ch["results"], "k": nb["02"]["k"],
              "variant_collapse": True, "variant_pool": K.VARIANT_POOL}
    assert "variant_collapse" in nb["03"], "notebook 03 must include the S6c.1 variant-collapse evaluation"
    assert params["profile"] == "multi_interest" and params["half_life"] is None, params

    full_texts = [R.product_text(p) for p in cat]
    keys = [K.variant_key(p) for p in cat]
    type_texts = [R.product_text(p, "type") for p in cat]
    vec = TfidfVectorizer(**TFIDF_PARAMS).fit(full_texts)
    T = vec.transform(full_texts).tocsr()
    model_dir = P / "models/e5-small"
    hf_id, rev, prefix = R.MODELS["e5-small"]

    content = E.tobytes() + centroids.tobytes() + json.dumps(params, sort_keys=True).encode() + sha(ROOT / "ml/theme_labels.json").encode() + sha(P / "images.json").encode() + "".join(
        sha(RES / d / "summary.json") for d in ("01_data_embeddings_retrieval", "02_clustering_pca", "03_personalization_mmr")).encode()
    version = time.strftime("%Y%m%d") + "-" + hashlib.sha256(content).hexdigest()[:8]
    out = Path(out_root) / version
    if out.exists():
        shutil.rmtree(out)
    (out / "model").mkdir(parents=True)
    (out / "fixtures").mkdir()

    # --- catalog and matrices
    with (out / "catalog.jsonl").open("w", encoding="utf-8") as f:
        for i, p in enumerate(cat):
            f.write(json.dumps({
                "index": i, "id": p["id"], "barcode": p["barcode"], "name": p["name"],
                "brand": p["brand"] or None, "category": p["category"] or None, "description": p["description"] or None,
                "tags": [R.tag_label(t) for t in p["categories"]],
                "provenance": {"type": "FALLBACK_DATASET", "source": p["source"]},
                "fullText": full_texts[i], "typeText": type_texts[i], "cluster": int(clusters[i]),
                "variantKey": keys[i]}, ensure_ascii=False) + "\n")
    write_f32(out / "embeddings.f32", E)
    write_f32(out / "type_embeddings.f32", TE)
    write_f32(out / "cluster_centroids.f32", centroids)
    write_f32(out / "pca_mean.f32", pca["mean"])
    write_f32(out / "pca_components.f32", pca["components"])
    vocab = sorted(vec.vocabulary_.items(), key=lambda kv: kv[1])
    (out / "tfidf.json").write_text(json.dumps({"params": {"analyzer": "char_wb", "ngramMin": 3, "ngramMax": 5, "lowercase": True,
                                                         "sublinearTf": True, "norm": "l2", "smoothIdf": True},
                                              "vocabulary": [t for t, _ in vocab], "idf": [round(float(x), 8) for x in vec.idf_]},
                                             ensure_ascii=False))
    theme_xy = S.pca_project(centroids, pca["mean"], pca["components"], 2)
    reviewed = json.loads((ROOT / "ml/theme_labels.json").read_text())["labels"]
    assert sorted(map(int, reviewed)) == list(range(len(themes))), "theme_labels.json must label every cluster"
    (out / "themes.json").write_text(json.dumps([dict(t, autoLabel=t["label"], label=reviewed[str(t["cluster"])],
                                                      x=round(float(theme_xy[t["cluster"], 0]), 6), y=round(float(theme_xy[t["cluster"], 1]), 6))
                                                for t in themes], ensure_ascii=False, indent=1))
    shutil.copy(P / "map_sample.json", out / "map_sample.json")
    shutil.copy(P / "images.json", out / "images.json")  # S7c.1: ml/asap_ml/images.py, OFF-family CC BY-SA
    for f in ("model.onnx", "tokenizer.json"):
        shutil.copy(model_dir / f, out / "model" / f)

    # --- parity fixtures (computed from the bundle's own matrices with the shared ml code)
    rng = np.random.default_rng(SEED)
    sample = [int(i) for i in rng.choice(N, 20, replace=False)]
    enc = R.OnnxEncoder(model_dir, prefix)
    new_products = [  # uncatalogued products: text building + encoding + assignment
        {"name": "Ovsena kaša sa borovnicama", "brand": "Bakina tajna", "category": "Porridges", "categories": ["en:breakfasts", "en:porridges"], "description": "", "labels": ""},
        {"name": "Ryobi 18V trim router", "brand": "Ryobi", "category": "", "categories": [], "description": "Cordless router", "labels": ""},
        {"name": "Coca-Cola Zero 0,5 l", "brand": "Coca-Cola", "category": "Sodas", "categories": ["en:beverages", "en:sodas"], "description": "", "labels": ""}]
    embed_cases = [{"text": t, "vector": [round(float(x), 7) for x in v]} for t, v in
                   zip([full_texts[i] for i in sample[:5]] + TRICKY_TEXTS[:3], enc.encode([full_texts[i] for i in sample[:5]] + TRICKY_TEXTS[:3]))]
    tfidf_cases = [{"text": t, **sparse_row(vec.transform([t]).tocsr(), 0)} for t in [full_texts[i] for i in sample[:5]] + TRICKY_TEXTS]

    def relevance(qv, qt):
        return params["alpha"] * (E @ qv) + (1 - params["alpha"]) * (T @ qt.T).toarray().ravel()

    retrieval_cases = []
    for q in sample[:10]:
        r = relevance(E[q], T[q])
        cand = K.collapsed_candidates(r, {q}, keys, keys[q], params["candidate_pool"])
        retrieval_cases.append({"queryIndex": q, "candidates": cand.tolist(), "scores": [round(float(x), 6) for x in r[cand]],
                                "mmrTop": K.mmr(cand, r, E, params["mmr_lambda"], params["results"]).tolist()})
    users = []
    for u in range(8):
        hist_len = [0, 2, 3, 5, 20, 20, 20, 20][u]
        hist = [int(i) for i in rng.choice(N, hist_len, replace=False)]
        if u == 3:
            hist[1] = hist[0]  # repeated view: 5 events, 4 distinct products
        q = int(rng.choice([i for i in range(N) if i not in hist]))
        r = relevance(E[q], T[q])
        ready = K.readiness(hist)
        if ready == "SUFFICIENT":
            p = K.multi_interest_profile(E, hist, np.inf, clusters, E[q])
            s = K.personalized_scores(r, E, p, params["beta"])
        else:
            s = r
        cand = K.collapsed_candidates(s, {q, *hist}, keys, keys[q], params["candidate_pool"])
        you = R.normalize(TE[hist].mean(0, keepdims=True))[0] if hist else None
        users.append({"historyIndices": hist, "queryIndex": q, "readiness": ready,
                      "mode": "PERSONALIZED_HISTORY" if ready == "SUFFICIENT" else "GENERIC_SEMANTIC",
                      "candidates": cand.tolist(), "scores": [round(float(x), 6) for x in s[cand]],
                      "mmrTop": K.mmr(cand, s, E, params["mmr_lambda"], params["results"]).tolist(),
                      "youXY": None if you is None else [round(float(x), 6) for x in S.pca_project(you, pca["mean"], pca["components"], 2)[0]]})
    new_cases = []
    for np_ in new_products:
        ft, tt = R.product_text(np_), R.product_text(np_, "type")
        qv, tv = enc.encode([ft])[0], enc.encode([tt])[0]
        r = relevance(qv, vec.transform([ft]).tocsr())
        cand = K.collapsed_candidates(r, set(), keys, K.variant_key(np_), params["candidate_pool"])
        new_cases.append({"product": np_, "fullText": ft, "typeText": tt, "variantKey": K.variant_key(np_), "cluster": int(S.assign(tv[None], centroids)[0]),
                          "xy": [round(float(x), 6) for x in S.pca_project(tv, pca["mean"], pca["components"], 2)[0]],
                          "mmrTop": K.mmr(cand, r, E, params["mmr_lambda"], params["results"]).tolist()})
    assign_cases = [{"index": i, "cluster": int(clusters[i]), "xy": [round(float(x), 6) for x in S.pca_project(TE[i], pca["mean"], pca["components"], 2)[0]]}
                    for i in sample]
    assert all(c["cluster"] == int(S.assign(TE[c["index"]][None], centroids)[0]) for c in assign_cases)
    fixtures = {"tolerances": {"embeddingCosineMin": 0.999, "tfidfAbs": 1e-6, "scoreAbs": 1e-4, "xyAbs": 1e-4},
                "notes": "Indices refer to catalog.jsonl 'index'. Ranked lists must match exactly unless two scores differ by < scoreAbs.",
                "embedding": embed_cases, "tfidf": tfidf_cases, "retrieval": retrieval_cases, "users": users,
                "newProducts": new_cases, "variantKeyNote": "name + U+241F + brand, each lowercased and whitespace-collapsed", "assignment": assign_cases}
    (out / "fixtures/parity.json").write_text(json.dumps(fixtures, ensure_ascii=False))

    files = sorted(p for p in out.rglob("*") if p.is_file())
    manifest = {
        "version": version, "createdUtc": time.strftime("%Y-%m-%dT%H:%M:%SZ", time.gmtime()), "products": N, "dim": int(E.shape[1]),
        "attribution": "Contains data from Open Food Facts, available under the Open Database License (ODbL 1.0).",
        "model": {"id": hf_id, "revision": rev, "prefix": prefix, "pooling": "mean", "normalize": True,
                  "onnxInputs": ["input_ids", "attention_mask"], "onnxOutput": "last_hidden_state", "maxLength": 512},
        "params": params,
        "textRules": {"full": "name | brand | category | deepest-3 category labels (', ') | labels | description; blanks omitted",
                      "type": "category | deepest-3 category labels; falls back to name", "tagLabel": "strip 'xx:' prefix, '-' -> ' '"},
        "matrices": {"embeddings.f32": [N, int(E.shape[1])], "type_embeddings.f32": [N, int(TE.shape[1])],
                     "cluster_centroids.f32": list(centroids.shape), "pca_mean.f32": [int(pca["mean"].shape[0])],
                     "pca_components.f32": list(pca["components"].shape)},
        "sources": {"catalogManifestSha256": sha(ROOT / "data/catalog_manifest.json"),
                    "notebookSummariesSha256": {k: sha(RES / d / "summary.json") for k, d in
                                                [("01", "01_data_embeddings_retrieval"), ("02", "02_clustering_pca"), ("03", "03_personalization_mmr")]}},
        "files": {str(p.relative_to(out)): {"bytes": p.stat().st_size, "sha256": sha(p)} for p in files},
    }
    (out / "manifest.json").write_text(json.dumps(manifest, indent=2, ensure_ascii=False))
    (Path(out_root) / "CURRENT").write_text(version + "\n")
    print(json.dumps({"version": version, "files": len(files), "bytes": sum(p.stat().st_size for p in files)}))
    return out


if __name__ == "__main__":
    main()
