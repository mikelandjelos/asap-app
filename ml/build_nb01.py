"""Generate notebooks/01_data_embeddings_retrieval.ipynb (executed separately with nbconvert)."""
import nbformat as nbf

md, code = nbf.v4.new_markdown_cell, nbf.v4.new_code_cell
nb = nbf.v4.new_notebook()
nb.cells = [
    md("# 01 — Dataset, embeddings and similarity retrieval (T-011/S3)\n\n"
       "Components verified: catalog dataset, embedding model, exact cosine top-N retrieval and the ONNX "
       "inference path the Java backend will mirror (`docs/AI_MVP_DESIGN.md` §3–5, `docs/NOTEBOOK_VALIDATION.md`).\n\n"
       "**Relevance proxy.** A catalog product is relevant to a query product when both share the same Open Food "
       "Facts leaf category (`main_category_en`). Because the production text contains category words, the "
       "*leak-free benchmark* uses a `no_category` text (name, brand, description only); `full`-text scores are "
       "reported but inflated by construction."),
    code("import json, hashlib, platform, random, time\nfrom collections import Counter, defaultdict\nfrom pathlib import Path\n"
         "import numpy as np, pandas as pd, matplotlib\nmatplotlib.use('Agg')\nimport matplotlib.pyplot as plt\n"
         "import torch, sklearn, sentence_transformers, onnxruntime\n"
         "ROOT = Path.cwd().parent if Path.cwd().name == 'notebooks' else Path.cwd()\nimport os, sys; os.chdir(ROOT); sys.path.insert(0, str(ROOT))\n"
         "from ml.asap_ml import retrieval as R\n"
         "OUT = ROOT / 'notebooks/results/01_data_embeddings_retrieval'; OUT.mkdir(parents=True, exist_ok=True)\n"
         "SEED = 20261006; random.seed(SEED); np.random.seed(SEED); torch.manual_seed(SEED); torch.set_num_threads(8)\n"
         "manifest = json.loads(Path('data/catalog_manifest.json').read_text())\n"
         "assert hashlib.sha256(Path('data/processed/catalog.jsonl').read_bytes()).hexdigest() == manifest['catalog_sha256']\n"
         "env = {'python': platform.python_version(), 'torch': torch.__version__, 'sentence_transformers': sentence_transformers.__version__,\n"
         "       'sklearn': sklearn.__version__, 'onnxruntime': onnxruntime.__version__, 'cpu': platform.processor() or platform.machine(),\n"
         "       'threads': torch.get_num_threads(), 'run_utc': time.strftime('%Y-%m-%dT%H:%M:%SZ', time.gmtime())}\nenv"),
    md("## 1. Dataset"),
    code("cat = R.load_catalog(); N = len(cat)\n"
         "df = pd.DataFrame({'source': [p['source'] for p in cat], 'category': [p['category'] for p in cat],\n"
         "                   'top': [p['top_category'] for p in cat], 'has_brand': [bool(p['brand']) for p in cat],\n"
         "                   'has_desc': [bool(p['description']) for p in cat], 'region': [p['region'] for p in cat],\n"
         "                   'len_full': [len(R.product_text(p)) for p in cat], 'len_nocat': [len(R.product_text(p, 'no_category')) for p in cat]})\n"
         "cat_sizes = df.category.value_counts()\n"
         "stats = {'products': N, 'by_source': df.source.value_counts().to_dict(), 'top_categories': df.top.nunique(),\n"
         "         'leaf_categories': int(cat_sizes.size), 'leaf_categories_ge5': int((cat_sizes >= 5).sum()),\n"
         "         'share_brand': round(df.has_brand.mean(), 3), 'share_description': round(df.has_desc.mean(), 3),\n"
         "         'share_region': round(df.region.mean(), 3), 'median_text_len_full': int(df.len_full.median()),\n"
         "         'median_text_len_no_category': int(df.len_nocat.median())}\nstats"),
    code("fig, ax = plt.subplots(1, 2, figsize=(11, 3.5))\n"
         "df.top.value_counts().head(20).iloc[::-1].plot.barh(ax=ax[0], title='Top-level strata (20 largest)')\n"
         "ax[1].hist(cat_sizes.values, bins=np.logspace(0, np.log10(cat_sizes.max()), 25)); ax[1].set_xscale('log')\n"
         "ax[1].set_title('Leaf-category size distribution'); ax[1].set_xlabel('products per leaf category')\n"
         "plt.tight_layout(); plt.savefig(OUT / 'dataset.png', dpi=150); plt.show()"),
    md("## 2. Query set\n500 seeded queries drawn from leaf categories with ≥ 5 products, so every query has ≥ 4 relevant items."),
    code("groups_by_cat = defaultdict(set)\nfor i, p in enumerate(cat): groups_by_cat[p['category']].add(i)\n"
         "groups = {i: groups_by_cat[p['category']] for i, p in enumerate(cat)}\n"
         "eligible = [i for i in range(N) if len(groups[i]) >= 5]\nrng = random.Random(SEED)\nqueries = sorted(rng.sample(eligible, 500))\n"
         "top_groups = defaultdict(set)\nfor i, p in enumerate(cat): top_groups[p['top_category']].add(i)\n"
         "coarse = {i: top_groups[p['top_category']] for i, p in enumerate(cat)}\n"
         "random_p10 = float(np.mean([(len(groups[q]) - 1) / (N - 1) for q in queries]))\n"
         "print(len(eligible), 'eligible;', 'random-ranking expected P@10 =', round(random_p10, 4))"),
    md("## 3. Methods\nLexical baselines (TF-IDF word 1–2-grams and character 3–5-grams, cosine) versus the two candidate "
       "sentence-embedding models. Every method is scored on both text variants with identical queries and metrics."),
    code("from sklearn.feature_extraction.text import TfidfVectorizer\n"
         "def eval_scores(score_fn, qs=queries, rel=groups, k=10, pool=50):\n"
         "    rows = []\n    for q in qs:\n        s = score_fn(q); s[q] = -np.inf\n        idx = np.argpartition(-s, pool)[:pool]; idx = idx[np.lexsort((idx, -s[idx]))]\n"
         "        rows.append(R.metrics(idx, rel[q] - {q}, k, pool))\n    return {m: float(np.mean([r[m] for r in rows])) for m in rows[0]}\n"
         "texts = {v: [R.product_text(p, v) for p in cat] for v in ('no_category', 'full')}\n"
         "results, vectors, encode_s = [], {}, {}\n"
         "for v in texts:\n    for name, kw in [('tfidf-word', dict(ngram_range=(1, 2), min_df=2, sublinear_tf=True)),\n"
         "                     ('tfidf-char', dict(analyzer='char_wb', ngram_range=(3, 5), min_df=2, sublinear_tf=True))]:\n"
         "        X = TfidfVectorizer(**kw).fit_transform(texts[v])\n"
         "        r = eval_scores(lambda q: (X @ X[q].T).toarray().ravel()); results.append({'method': name, 'text': v, **r,\n"
         "            'coarse_p@10': eval_scores(lambda q: (X @ X[q].T).toarray().ravel(), rel=coarse)['p@10']})\n"
         "for m in R.MODELS:\n    for v in texts:\n        t = time.perf_counter(); vec, _ = R.encode(m, texts[v]); encode_s[(m, v)] = time.perf_counter() - t\n"
         "        vectors[(m, v)] = vec; assert np.isfinite(vec).all() and np.allclose(np.linalg.norm(vec, axis=1), 1, atol=1e-4)\n"
         "        results.append({'method': m, 'text': v, **R.evaluate(vec, queries, groups), 'coarse_p@10': R.evaluate(vec, queries, coarse)['p@10']})\n"
         "res = pd.DataFrame(results).set_index(['text', 'method']).sort_index()\nres.round(4)"),
    code("ax = res.loc['no_category'][['p@10', 'ndcg@10', 'recall@50']].plot.bar(rot=0, figsize=(8, 3.5))\n"
         "ax.axhline(random_p10, ls='--', c='grey', label='random P@10'); ax.legend(); ax.set_ylim(0, 1)\n"
         "ax.set_title('Leak-free retrieval quality (no_category text, 500 queries)')\n"
         "plt.tight_layout(); plt.savefig(OUT / 'retrieval_quality.png', dpi=150); plt.show()\n"
         "pd.Series({f'{m}/{v}': round(s, 1) for (m, v), s in encode_s.items()}, name='catalog encode seconds (10k, CPU)')"),
    md("## 4. Uncertainty\nBootstrap 95 % confidence interval of nDCG@10 per method (no_category text, 1,000 resamples of queries)."),
    code("def per_query_ndcg(vec): return np.array([R.metrics(R.top_k(vec, q, 50), groups[q] - {q})['ndcg@10'] for q in queries])\n"
         "boot = {}\nbrng = np.random.default_rng(SEED)\nfor m in R.MODELS:\n    pq = per_query_ndcg(vectors[(m, 'no_category')])\n"
         "    bs = [pq[brng.integers(0, len(pq), len(pq))].mean() for _ in range(1000)]\n    boot[m] = (pq.mean(), *np.percentile(bs, [2.5, 97.5]))\n"
         "pd.DataFrame(boot, index=['ndcg@10', 'ci_low', 'ci_high']).T.round(4)"),
    md("## 5. Selection\nRule fixed before the run: highest leak-free nDCG@10 among the embedding models; it must also beat both TF-IDF baselines."),
    code("emb = res.loc['no_category'].loc[list(R.MODELS)]\nchosen = emb['ndcg@10'].idxmax()\n"
         "beats_tfidf = bool(emb.loc[chosen, 'ndcg@10'] > res.loc['no_category'].loc[['tfidf-word', 'tfidf-char'], 'ndcg@10'].max())\n"
         "print('chosen:', chosen, '| beats TF-IDF:', beats_tfidf)"),
    md("## 5b. Hybrid lexical + semantic fusion\n"
       "Because the chosen embedding only ties character TF-IDF, test the standard remedy: "
       "`s = α·cos_e5 + (1−α)·cos_tfidf_char`. α is tuned on a **separate** 500-query tuning set (disjoint from the "
       "test queries) and then evaluated once on the test set with paired bootstrap CIs of the nDCG@10 difference."),
    code("tune_pool = sorted(set(eligible) - set(queries)); tune = sorted(random.Random(SEED + 1).sample(tune_pool, 500))\n"
         "hyb = {}\nfor v in texts:\n    Xv = TfidfVectorizer(analyzer='char_wb', ngram_range=(3, 5), min_df=2, sublinear_tf=True).fit_transform(texts[v])\n"
         "    Ev = vectors[(chosen, v)]\n    hyb[v] = (Xv, Ev)\n"
         "def hybrid_fn(v, a):\n    Xv, Ev = hyb[v]\n    return lambda q: a * (Ev @ Ev[q]) + (1 - a) * (Xv @ Xv[q].T).toarray().ravel()\n"
         "alphas = np.round(np.arange(0, 1.01, 0.1), 2)\n"
         "sweep = pd.DataFrame({v: [eval_scores(hybrid_fn(v, a), qs=tune)['ndcg@10'] for a in alphas] for v in texts}, index=alphas)\n"
         "best_alpha = {v: float(sweep[v].idxmax()) for v in texts}\n"
         "ax = sweep.plot(marker='o', figsize=(6, 3.5), title='Hybrid α sweep (tuning queries)'); ax.set_xlabel('α (weight of e5)'); ax.set_ylabel('nDCG@10')\n"
         "plt.tight_layout(); plt.savefig(OUT / 'hybrid_alpha_sweep.png', dpi=150); plt.show(); best_alpha"),
    code("def per_query(score_fn, qs=queries):\n    out = []\n    for q in qs:\n        sc = score_fn(q); sc[q] = -np.inf; idx = np.argpartition(-sc, 50)[:50]; idx = idx[np.lexsort((idx, -sc[idx]))]\n"
         "        out.append(R.metrics(idx, groups[q] - {q})['ndcg@10'])\n    return np.array(out)\n"
         "v = 'no_category'; Xv, Ev = hyb[v]\n"
         "pq = {'hybrid': per_query(hybrid_fn(v, best_alpha[v])), chosen: per_query(lambda q: Ev @ Ev[q]),\n"
         "      'tfidf-char': per_query(lambda q: (Xv @ Xv[q].T).toarray().ravel())}\n"
         "brng2 = np.random.default_rng(SEED + 2); idxs = [brng2.integers(0, len(queries), len(queries)) for _ in range(1000)]\n"
         "def paired(a, b): d = pq[a] - pq[b]; bs = [d[i].mean() for i in idxs]; return [round(float(d.mean()), 4), *np.round(np.percentile(bs, [2.5, 97.5]), 4).tolist()]\n"
         "hybrid_test = {'alpha': best_alpha, 'test_ndcg@10': {k: round(float(x.mean()), 4) for k, x in pq.items()},\n"
         "               'diff_vs_' + chosen: paired('hybrid', chosen), 'diff_vs_tfidf-char': paired('hybrid', 'tfidf-char'),\n"
         "               'full_text_test': eval_scores(hybrid_fn('full', best_alpha['full']))}\n"
         "hybrid_wins = hybrid_test['diff_vs_' + chosen][1] > 0 and hybrid_test['diff_vs_tfidf-char'][1] > 0\n"
         "print(json.dumps(hybrid_test, indent=1)); print('hybrid significantly beats both:', hybrid_wins)"),
    md("## 6. Qualitative check\nTop-5 neighbours (production `full` text) for six seeded queries: chosen model vs. character TF-IDF."),
    code("Xc = TfidfVectorizer(analyzer='char_wb', ngram_range=(3, 5), min_df=2, sublinear_tf=True).fit_transform(texts['full'])\n"
         "vf = vectors[(chosen, 'full')]\nrows = []\nfor q in rng.sample(queries, 6):\n"
         "    s = (Xc @ Xc[q].T).toarray().ravel(); s[q] = -np.inf; t5 = np.argsort(-s)[:5]\n"
         "    rows.append({'query': f\"{cat[q]['name']} [{cat[q]['category']}]\",\n"
         "                 chosen: '; '.join(cat[i]['name'] for i in R.top_k(vf, q, 5)), 'tfidf-char': '; '.join(cat[i]['name'] for i in t5)})\n"
         "pd.set_option('display.max_colwidth', 120); qual = pd.DataFrame(rows); qual"),
    md("## 7. Latency (desktop CPU; not phone timing)\nSingle-text encoding of an uncatalogued product, and exact top-50 search versus corpus size "
       "(larger corpora are random unit vectors of the same dimension; search cost does not depend on content)."),
    code("from sentence_transformers import SentenceTransformer\nhf, rev, prefix = R.MODELS[chosen]\nst = SentenceTransformer(hf, revision=rev, device='cpu')\n"
         "sample_texts = [texts['full'][i] for i in rng.sample(range(N), 50)]\nit = iter(sample_texts * 3)\n"
         "lat = {'encode_single_torch': R.timed(lambda: st.encode([prefix + next(it)], normalize_embeddings=True), 100)}\n"
         "dim = vf.shape[1]\nfor n in (1_000, 10_000, 100_000):\n"
         "    M = vf[:n] if n <= N else R.normalize(np.random.default_rng(SEED).standard_normal((n, dim)))\n"
         "    q = M[0].copy()\n    lat[f'search_top50_n{n}'] = R.timed(lambda: np.argpartition(-(M @ q), 50)[:50], 200)\n"
         "lat_df = pd.DataFrame(lat).T.round(2); lat_df"),
    md("## 8. ONNX export and parity\nExport the chosen model to ONNX, then run it exactly as Java will (tokenizer.json → ONNX → mean pooling → L2) "
       "and compare against sentence-transformers. Gate: minimum cosine ≥ 0.999."),
    code("MODEL_DIR = R.export_onnx(chosen, ROOT / 'data/processed/models' / chosen)\n"
         "onnx_enc = R.OnnxEncoder(MODEL_DIR, prefix)\nparity_idx = rng.sample(range(N), 500)\n"
         "ref = vf[parity_idx]; got = onnx_enc.encode([texts['full'][i] for i in parity_idx])\ncos = (ref * got).sum(1)\n"
         "it2 = iter(sample_texts * 3)\nlat['encode_single_onnx'] = R.timed(lambda: onnx_enc.encode([next(it2)]), 100)\n"
         "parity = {'n': len(cos), 'min_cosine': float(cos.min()), 'mean_cosine': float(cos.mean()), 'gate_pass': bool(cos.min() >= 0.999),\n"
         "          'onnx_bytes': (MODEL_DIR / 'model.onnx').stat().st_size}\nparity, lat['encode_single_onnx']"),
    md("## 9. Export"),
    code("np.save(ROOT / 'data/processed' / f'embeddings_{chosen}.npy', vf)\n"
         "(ROOT / 'data/processed' / 'embedding_ids.json').write_text(json.dumps([p['id'] for p in cat]))\n"
         "summary = {'env': env, 'dataset': stats, 'queries': len(queries), 'random_p@10': random_p10,\n"
         "  'results': {f'{t}/{m}': {k: round(v, 4) for k, v in r.items()} for (t, m), r in res.iterrows()},\n"
         "  'bootstrap_ndcg@10': {m: [round(x, 4) for x in v] for m, v in boot.items()},\n"
         "  'encode_catalog_s': {f'{m}/{v}': round(s, 1) for (m, v), s in encode_s.items()},\n"
         "  'chosen': chosen, 'chosen_revision': rev, 'beats_tfidf': beats_tfidf, 'latency_ms': {k: {a: round(b, 3) for a, b in v.items()} for k, v in lat.items()},\n"
         "  'hybrid': hybrid_test, 'hybrid_beats_both': bool(hybrid_wins), 'hybrid_alpha_sweep': {str(k): v for k, v in sweep.round(4).to_dict().items()},\n"
         "  'onnx_parity': parity, 'embeddings_sha256': hashlib.sha256(vf.tobytes()).hexdigest()}\n"
         "(OUT / 'summary.json').write_text(json.dumps(summary, indent=2)); res.round(4).to_csv(OUT / 'retrieval_results.csv')\n"
         "qual.to_csv(OUT / 'qualitative_top5.csv', index=False); pd.DataFrame(lat).T.round(3).to_csv(OUT / 'latency.csv')\nprint(json.dumps({k: summary[k] for k in ('chosen', 'beats_tfidf', 'onnx_parity')}, indent=1))"),
    md("## Limitations\n- Category agreement is a proxy for similarity; OFF categories are crowd-sourced and some leaf categories are "
       "near-duplicates, so absolute scores understate perceived quality. Relative ranking of methods is the decision signal.\n"
       "- No human relevance judgements yet; a small judged set is a recommended addition before the final report.\n"
       "- Timings are desktop CPU; phone-side and network latency are measured separately (S8)."),
]
nbf.write(nb, "notebooks/01_data_embeddings_retrieval.ipynb")
