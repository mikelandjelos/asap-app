"""Generate notebooks/03_personalization_mmr.ipynb (executed separately with nbconvert)."""
import nbformat as nbf

md, code = nbf.v4.new_markdown_cell, nbf.v4.new_code_cell
nb = nbf.v4.new_notebook()
nb.cells = [
    md("# 03 — History personalization and MMR diversification (T-011/S5)\n\n"
       "Components verified: history profile aggregation (recency-weighted centroid vs multi-interest per type cluster), "
       "personalized scoring `s = (1−β)·r(q,x) + β·cos(p,x)` on top of the hybrid relevance `r` (D-030), cold-start readiness, and "
       "MMR re-ranking of the top-50 candidates to 10 (`docs/AI_MVP_DESIGN.md` §7, §9).\n\n"
       "**Synthetic users (seeded, labelled synthetic).** Each user has 1–3 interests (top-level OFF categories). A 20-event history "
       "is drawn from them; the newest 5 events favour a *current mission* interest (80 %). The scanned query product comes from the "
       "current mission, except for 20 % *out-of-interest* scans. Graded relevance: **2** = same leaf category as the query (a similar "
       "product), **1** = inside the user's interests, **0** otherwise. Parameters are tuned on 500 users and reported once on 1,000 "
       "disjoint test users. No real user data is used."),
    code("import json, hashlib, platform, time\nfrom collections import defaultdict\nfrom pathlib import Path\n"
         "import numpy as np, pandas as pd, matplotlib\nmatplotlib.use('Agg')\nimport matplotlib.pyplot as plt\n"
         "from sklearn.feature_extraction.text import TfidfVectorizer\n"
         "ROOT = Path.cwd().parent if Path.cwd().name == 'notebooks' else Path.cwd()\nimport os, sys; os.chdir(ROOT); sys.path.insert(0, str(ROOT))\n"
         "from ml.asap_ml import retrieval as R, ranking as K\n"
         "OUT = ROOT / 'notebooks/results/03_personalization_mmr'; OUT.mkdir(parents=True, exist_ok=True)\nSEED = 20261006\n"
         "cat = R.load_catalog(); N = len(cat); E = np.load('data/processed/embeddings_e5-small.npy'); clusters = np.load('data/processed/cluster_labels.npy')\n"
         "nb01 = json.loads(Path('notebooks/results/01_data_embeddings_retrieval/summary.json').read_text())\n"
         "nb02 = json.loads(Path('notebooks/results/02_clustering_pca/summary.json').read_text())\n"
         "assert hashlib.sha256(E.tobytes()).hexdigest() == nb01['embeddings_sha256']\n"
         "assert hashlib.sha256(Path('data/processed/cluster_labels.npy').read_bytes()).hexdigest() == nb02['artifact_sha256']['cluster_labels.npy']\n"
         "ALPHA = nb01['hybrid']['alpha']['no_category']  # 0.9, D-030\n"
         "T = TfidfVectorizer(analyzer='char_wb', ngram_range=(3, 5), min_df=2, sublinear_tf=True).fit_transform([R.product_text(p) for p in cat]).tocsr()\n"
         "def relevance(q): return ALPHA * (E @ E[q]) + (1 - ALPHA) * (T @ T[q].T).toarray().ravel()\n"
         "env = {'python': platform.python_version(), 'numpy': np.__version__, 'run_utc': time.strftime('%Y-%m-%dT%H:%M:%SZ', time.gmtime()), 'alpha': ALPHA}; env"),
    md("## 1. Synthetic users"),
    code("top = np.array([p['top_category'] for p in cat]); leaf = np.array([p['category'] for p in cat])\n"
         "by_top = {t: np.flatnonzero(top == t) for t in np.unique(top)}; tops = sorted(t for t, ix in by_top.items() if len(ix) >= 30)\n"
         "leaf_size = pd.Series(leaf).value_counts()\n"
         "def make_users(n, seed):\n    rng = np.random.default_rng(seed); users = []\n    while len(users) < n:\n"
         "        interests = list(rng.choice(tops, rng.integers(1, 4), replace=False)); current = interests[0]\n"
         "        hist = [int(rng.choice(by_top[current if (i < 5 and rng.random() < 0.8) else rng.choice(interests)])) for i in range(20)]\n"
         "        outside = rng.random() < 0.2\n        qtop = rng.choice([t for t in tops if t not in interests]) if outside else current\n"
         "        pool = [i for i in by_top[qtop] if i not in hist and leaf_size[leaf[i]] >= 2]\n        if not pool: continue\n"
         "        q = int(rng.choice(pool)); interest_set = set(interests)\n"
         "        gains = np.zeros(N, np.int8); gains[np.isin(top, interests)] = 1; gains[leaf == leaf[q]] = 2; gains[q] = 0; gains[hist] = 0\n"
         "        users.append({'interests': interests, 'hist': hist, 'q': q, 'outside': bool(outside), 'gains': gains})\n    return users\n"
         "tune, test = make_users(500, SEED), make_users(1000, SEED + 1)\n"
         "pd.Series({'tune_users': len(tune), 'test_users': len(test), 'test_out_of_interest_share': np.mean([u['outside'] for u in test]),\n"
         "           'mean_interests': np.mean([len(u['interests']) for u in test]), 'eligible_top_categories': len(tops)})"),
    code("for u in tune + test: u['rel'] = relevance(u['q'])  # cached hybrid relevance per user query\n"
         "def rank(u, method, beta=0.0, H=5.0):\n    if method == 'generic' or K.readiness(u['hist']) == 'COLD_START': s = u['rel']\n"
         "    else:\n        p = K.centroid_profile(E, u['hist'], H) if method == 'centroid' else K.multi_interest_profile(E, u['hist'], H, clusters, E[u['q']])\n"
         "        s = K.personalized_scores(u['rel'], E, p, beta)\n    return s, K.candidates(s, {u['q'], *u['hist']})\n"
         "def score_users(users, method, beta=0.0, H=5.0):\n    return np.array([K.graded_ndcg(rank(u, method, beta, H)[1], u['gains']) for u in users])"),
    md("## 2. Tuning β and half-life H (tuning users only)"),
    code("betas = [0.1, 0.2, 0.3, 0.4, 0.5, 0.6]; Hs = [3.0, 5.0, 10.0, np.inf]\ngrid = []\n"
         "for m in ('centroid', 'multi_interest'):\n    for b in betas:\n        for H in Hs: grid.append({'method': m, 'beta': b, 'H': H, 'ndcg@10': score_users(tune, m, b, H).mean()})\n"
         "grid = pd.DataFrame(grid); generic_tune = score_users(tune, 'generic').mean()\n"
         "best = grid.loc[grid.groupby('method')['ndcg@10'].idxmax()].set_index('method')\n"
         "CHOSEN = best['ndcg@10'].idxmax(); BETA, H = float(best.loc[CHOSEN, 'beta']), float(best.loc[CHOSEN, 'H'])\n"
         "fig, ax = plt.subplots(figsize=(7, 3.5))\nfor m in ('centroid', 'multi_interest'):\n    g = grid[(grid.method == m) & (grid.H == best.loc[m, 'H'])]; ax.plot(g.beta, g['ndcg@10'], marker='o', label=f\"{m} (H={best.loc[m, 'H']})\")\n"
         "ax.axhline(generic_tune, ls='--', c='grey', label='generic'); ax.set_xlabel('β'); ax.set_ylabel('graded nDCG@10'); ax.legend(); ax.set_title('Personalization tuning (500 users)')\n"
         "plt.tight_layout(); plt.savefig(OUT / 'beta_sweep.png', dpi=150); plt.show()\nprint('chosen:', CHOSEN, 'beta =', BETA, 'H =', H, '| generic =', round(generic_tune, 4)); best.round(4)"),
    md("## 3. Test: personalized vs generic (1,000 disjoint users)\nPaired bootstrap 95 % CI of the nDCG@10 difference; split by in-interest and out-of-interest scans."),
    code("res = {m: score_users(test, m, BETA if m != 'generic' else 0.0, H) for m in ('generic', 'centroid', 'multi_interest')}\n"
         "res['centroid'] = score_users(test, 'centroid', float(best.loc['centroid', 'beta']), float(best.loc['centroid', 'H']))\n"
         "res['multi_interest'] = score_users(test, 'multi_interest', float(best.loc['multi_interest', 'beta']), float(best.loc['multi_interest', 'H']))\n"
         "outside = np.array([u['outside'] for u in test]); brng = np.random.default_rng(SEED + 2)\n"
         "def ci(d):\n    bs = [d[brng.integers(0, len(d), len(d))].mean() for _ in range(1000)]\n    return [round(float(d.mean()), 4), *np.round(np.percentile(bs, [2.5, 97.5]), 4).tolist()]\n"
         "def top10(u, m):\n    b = 0.0 if m == 'generic' else float(best.loc[m, 'beta']); h = 5.0 if m == 'generic' else float(best.loc[m, 'H'])\n    return rank(u, m, b, h)[1][:10]\n"
         "def stats(m):\n    t10 = [top10(u, m) for u in test]; g10 = [top10(u, 'generic') for u in test]\n"
         "    return {'ndcg@10': round(float(res[m].mean()), 4), 'ndcg@10_in_interest': round(float(res[m][~outside].mean()), 4), 'ndcg@10_out_of_interest': round(float(res[m][outside].mean()), 4),\n"
         "            'similar_share@10': round(float(np.mean([np.mean([u['gains'][i] == 2 for i in t]) for u, t in zip(test, t10)])), 4),\n"
         "            'interest_share@10': round(float(np.mean([np.mean([u['gains'][i] >= 1 for i in t]) for u, t in zip(test, t10)])), 4),\n"
         "            'rank_changed': round(float(np.mean([not np.array_equal(a, b) for a, b in zip(t10, g10)])), 4)}\n"
         "pers = pd.DataFrame({m: stats(m) for m in res}).T\n"
         "diffs = {f'{m}_minus_generic': {'all': ci(res[m] - res['generic']), 'out_of_interest': ci((res[m] - res['generic'])[outside])} for m in ('centroid', 'multi_interest')}\n"
         "display(pers); diffs"),
    md("## 4. Cold start and readiness\nWith fewer than 3 distinct known products the request is `COLD_START` and the ranking must equal the generic one exactly."),
    code("cold = []\nfor hist in ([], [test[0]['hist'][0]], test[0]['hist'][:2], [test[0]['hist'][0]] * 5):\n"
         "    u = dict(test[0], hist=hist); s, c = rank(u, CHOSEN, BETA, H); g = rank(dict(u), 'generic')[1]\n"
         "    cold.append({'history_len': len(hist), 'distinct': len(set(hist)), 'readiness': K.readiness(hist), 'equals_generic': bool(np.array_equal(c, g))})\n"
         "u3 = dict(test[0], hist=test[0]['hist'][:3]); cold.append({'history_len': 3, 'distinct': len(set(u3['hist'])), 'readiness': K.readiness(u3['hist']),\n"
         "    'equals_generic': bool(np.array_equal(rank(u3, CHOSEN, BETA, H)[1], rank(u3, 'generic')[1]))})\ncold = pd.DataFrame(cold); cold"),
    md("## 5. MMR λ sweep\nRelevance–diversity trade-off on the chosen personalized scores. Rule (pre-declared): λ* = the smallest λ whose tuning "
       "nDCG@10 ≥ 95 % of λ = 1 (relevance only); report test values once."),
    code("lams = np.round(np.arange(0, 1.01, 0.1), 2)\n"
         "def mmr_eval(users, lam):\n    nd, dv, cov, lc = [], [], [], []\n    for u in users:\n        s, c = rank(u, CHOSEN, BETA, H); top = K.mmr(c, s, E, lam)\n"
         "        nd.append(K.graded_ndcg(top, u['gains'])); dv.append(K.ild(top, E)); cov.append(len(set(clusters[top]))); lc.append(len(set(leaf[top])))\n"
         "    return {'ndcg@10': np.mean(nd), 'ild@10': np.mean(dv), 'cluster_coverage@10': np.mean(cov), 'leaf_coverage@10': np.mean(lc)}\n"
         "msw = pd.DataFrame({l: mmr_eval(tune, l) for l in lams}).T\nLAM = float(msw[msw['ndcg@10'] >= 0.95 * msw.loc[1.0, 'ndcg@10']].index.min())\n"
         "fig, ax = plt.subplots(1, 2, figsize=(11, 3.5))\nax[0].plot(msw['ild@10'], msw['ndcg@10'], marker='o')\n"
         "for l, r in msw.iterrows(): ax[0].annotate(f'{l:.1f}', (r['ild@10'], r['ndcg@10']), fontsize=7)\n"
         "ax[0].set_xlabel('ILD@10 (diversity)'); ax[0].set_ylabel('graded nDCG@10'); ax[0].set_title('MMR trade-off (tuning users), labels = λ')\n"
         "msw[['cluster_coverage@10', 'leaf_coverage@10']].plot(ax=ax[1], marker='o', title='Coverage of 10 results'); ax[1].axvline(LAM, ls='--', c='grey')\n"
         "plt.tight_layout(); plt.savefig(OUT / 'mmr_tradeoff.png', dpi=150); plt.show(); print('λ* =', LAM); msw.round(4)"),
    code("mmr_test = pd.DataFrame({'relevance_only (λ=1)': mmr_eval(test, 1.0), f'mmr (λ={LAM})': mmr_eval(test, LAM)}).T; mmr_test.round(4)"),
    md("## 6. MMR edge cases"),
    code("u = test[0]; s, c = rank(u, CHOSEN, BETA, H)\nE2 = np.vstack([E, E[c[0]]]); s2 = np.append(s, s[c[0]] - 1e-6); c2 = np.insert(c, 1, len(E))  # exact duplicate of the top item\n"
         "edge = {'lambda1_equals_relevance_order': bool(np.array_equal(K.mmr(c, s, E, 1.0), c[:10])),\n"
         "        'short_pool_returns_all': len(K.mmr(c[:4], s, E, LAM)) == 4, 'empty_pool': len(K.mmr(np.array([], int), s, E, LAM)) == 0,\n"
         "        'duplicate_position_relevance_only': int(np.flatnonzero(K.mmr(c2, s2, E2, 1.0) == len(E))[0]),\n"
         "        'duplicate_position_mmr': (lambda r: int(np.flatnonzero(r == len(E))[0]) if (r == len(E)).any() else None)(K.mmr(c2, s2, E2, LAM)),\n"
         "        'no_repeats': len(set(K.mmr(c, s, E, LAM))) == 10}; edge"),
    md("## 7. Runtime of one personalized request (desktop CPU, catalog 10k)"),
    code("u = test[1]\ndef full_request():\n    r = relevance(u['q']); p = K.multi_interest_profile(E, u['hist'], H, clusters, E[u['q']]) if CHOSEN == 'multi_interest' else K.centroid_profile(E, u['hist'], H)\n"
         "    s = K.personalized_scores(r, E, p, BETA); return K.mmr(K.candidates(s, {u['q'], *u['hist']}), s, E, LAM)\n"
         "rt = {'full_request': R.timed(full_request, 100), 'mmr_only': R.timed(lambda: K.mmr(c, s, E, LAM), 200)}; rt"),
    md("## 8. Export"),
    code("summary = {'env': env, 'users': {'tune': len(tune), 'test': len(test)}, 'chosen': {'method': CHOSEN, 'beta': BETA, 'half_life': H, 'mmr_lambda': LAM,\n"
         "  'min_distinct_history': K.MIN_DISTINCT, 'history_window': 20, 'candidate_pool': 50, 'results': 10},\n"
         "  'tuning_best': {m: {c: (None if not np.isfinite(v) else round(float(v), 4)) for c, v in r.items()} for m, r in best.iterrows()}, 'generic_tune_ndcg@10': round(float(generic_tune), 4),\n"
         "  'test': pers.to_dict(orient='index'), 'test_diffs_vs_generic': diffs, 'cold_start': cold.to_dict(orient='records'),\n"
         "  'mmr_sweep_tune': {str(l): {c: round(float(v), 4) for c, v in r.items()} for l, r in msw.iterrows()},\n"
         "  'mmr_test': {i: {c: round(float(v), 4) for c, v in r.items()} for i, r in mmr_test.iterrows()}, 'mmr_edge_cases': edge, 'runtime_ms': rt}\n"
         "(OUT / 'summary.json').write_text(json.dumps(summary, indent=2, default=lambda o: None if isinstance(o, float) and not np.isfinite(o) else str(o)))\n"
         "grid.to_csv(OUT / 'personalization_grid.csv', index=False); msw.round(4).to_csv(OUT / 'mmr_sweep.csv'); pers.to_csv(OUT / 'personalization_test.csv')\n"
         "print(json.dumps({k: summary[k] for k in ('chosen', 'test_diffs_vs_generic')}, indent=1))"),
    md("## Limitations\n- Users, interests and relevance are synthetic and derived from OFF categories; the experiment shows the mechanism works as "
       "designed and how to set β/H/λ, not real-user satisfaction (that needs the user study).\n"
       "- Interest membership uses top-level categories while multi-interest grouping uses type clusters; they are correlated, so the "
       "multi-interest advantage may be optimistic.\n- Timings are desktop CPU; phone/network latency is measured in S8."),
]
nbf.write(nb, "notebooks/03_personalization_mmr.ipynb")
