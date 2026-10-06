"""Generate notebooks/02_clustering_pca.ipynb (executed separately with nbconvert)."""
import nbformat as nbf

md, code = nbf.v4.new_markdown_cell, nbf.v4.new_code_cell
nb = nbf.v4.new_notebook()
nb.cells = [
    md("# 02 — Clustering and PCA (T-011/S4)\n\n"
       "Input: production (`full` text) e5-small catalog embeddings from notebook 01. Components verified: spherical k-means "
       "(themes, multi-interest profiles, map colouring), its HDBSCAN comparator, and PCA (2-D product map + dimensionality study). "
       "See `docs/AI_MVP_DESIGN.md` §6 and §8.\n\n"
       "**Pre-declared k rule:** among k ∈ {10, 15, …, 60}, discard k with mean pairwise seed-ARI < 0.7 (unstable), then pick the k "
       "with the best average rank over cosine silhouette (↑), Davies–Bouldin (↓) and NMI against top-level OFF categories (↑). "
       "Internal scores alone are not treated as semantic truth."),
    code("import json, hashlib, platform, random, time, itertools\nfrom pathlib import Path\n"
         "import numpy as np, pandas as pd, matplotlib\nmatplotlib.use('Agg')\nimport matplotlib.pyplot as plt\nimport sklearn\n"
         "from sklearn.metrics import silhouette_score, davies_bouldin_score, adjusted_rand_score as ARI, normalized_mutual_info_score as NMI\n"
         "from sklearn.cluster import HDBSCAN\n"
         "ROOT = Path.cwd().parent if Path.cwd().name == 'notebooks' else Path.cwd()\nimport os, sys; os.chdir(ROOT); sys.path.insert(0, str(ROOT))\n"
         "from ml.asap_ml import retrieval as R, structure as S\n"
         "OUT = ROOT / 'notebooks/results/02_clustering_pca'; OUT.mkdir(parents=True, exist_ok=True)\nSEED = 20261006; random.seed(SEED); np.random.seed(SEED)\n"
         "cat = R.load_catalog(); X = np.load('data/processed/embeddings_e5-small.npy')\n"
         "nb01 = json.loads(Path('notebooks/results/01_data_embeddings_retrieval/summary.json').read_text())\n"
         "assert hashlib.sha256(X.tobytes()).hexdigest() == nb01['embeddings_sha256'] and len(cat) == len(X)\n"
         "texts = [R.product_text(p) for p in cat]\ntop = np.array([p['top_category'] for p in cat]); leaf = np.array([p['category'] for p in cat])\n"
         "env = {'python': platform.python_version(), 'sklearn': sklearn.__version__, 'numpy': np.__version__,\n"
         "       'run_utc': time.strftime('%Y-%m-%dT%H:%M:%SZ', time.gmtime())}\nX.shape, env"),
    md("## 1. Representation comparison and k sweep\n"
       "A first run on the full-text embedding (A) produced language/country clusters rather than product types (low purity, unstable). "
       "Three representations are therefore compared under the same rule: **A** full-text e5 (retrieval space); **B** e5 of the English "
       "OFF taxonomy text only (product-type space); **C** A with its top 5 principal components removed (all-but-the-top debiasing). "
       "Caveat: B is built from category text, so its agreement with categories is partly by construction; stability and silhouette are "
       "the independent evidence, and the purpose of themes is precisely product-type grouping."),
    code("type_texts = [R.product_text(p, 'type') for p in cat]\nXB, _ = R.encode('e5-small', type_texts)\n"
         "mA, cA, _ = S.pca_fit(X); XC = R.normalize((X - mA) - ((X - mA) @ cA[:5].T) @ cA[:5])\n"
         "REPS = {'A_full': X, 'B_type': XB, 'C_debiased': XC}\n"
         "ks = list(range(10, 61, 5)); seeds = [SEED + i for i in range(5)]\n"
         "def k_sweep(V):\n    rows = []\n    for k in ks:\n        t = time.perf_counter(); runs = [S.spherical_kmeans(V, k, s)[0] for s in seeds]; fit_s = (time.perf_counter() - t) / len(seeds)\n"
         "        lab = runs[0]\n        rows.append({'k': k, 'silhouette': silhouette_score(V, lab, metric='cosine', sample_size=5000, random_state=SEED),\n"
         "                     'davies_bouldin': davies_bouldin_score(V, lab), 'stability_ari': np.mean([ARI(a, b) for a, b in itertools.combinations(runs, 2)]),\n"
         "                     'nmi_top': NMI(top, lab), 'nmi_leaf': NMI(leaf, lab), 'fit_s': fit_s})\n    return pd.DataFrame(rows).set_index('k')\n"
         "sweeps = {r: k_sweep(V) for r, V in REPS.items()}\npd.concat(sweeps, axis=1).round(3)"),
    code("def choose_k(sw):\n    st = sw[sw.stability_ari >= 0.7]\n    if st.empty: return None\n"
         "    rk = st.silhouette.rank(ascending=False) + st.davies_bouldin.rank() + st.nmi_top.rank(ascending=False)\n    return int(rk.idxmin())\n"
         "best = {r: choose_k(sw) for r, sw in sweeps.items()}\n"
         "rep_table = pd.DataFrame({r: {'k': best[r], **(sweeps[r].loc[best[r]].to_dict() if best[r] else {})} for r in REPS}).T\n"
         "best_original = best[rep_table.dropna(subset=['k']).sort_values(['stability_ari', 'silhouette'], ascending=False).index[0]]\n"
         "original_choice = rep_table.dropna(subset=['k']).sort_values(['stability_ari', 'silhouette'], ascending=False).index[0]\n"
         "print('original pre-declared rule selects:', original_choice, 'k =', best[original_choice]); rep_table.round(4)"),
    md("### Rule revision (user decision, 2026-10-06, D-031)\n"
       "The original rule selects A_full at k = 15, whose clusters split products by **language/country** rather than product type "
       "(see the c-TF-IDF terms of the first run). B_type is better on every quality metric (silhouette 3–5× higher, lower Davies–Bouldin, "
       "higher NMI) but no representation except A reaches seed stability 0.7, even with `n_init = 20` (B ≈ 0.58–0.68). "
       "Themes exist to group product types for the user, so the revised rule selects **B_type** and chooses k ∈ {20, …, 60} by the best "
       "average rank of silhouette (↑), Davies–Bouldin (↓), NMI vs top-level categories (↑) and stability (↑). The moderate stability is "
       "reported as a limitation; the app uses one fixed, versioned clustering. B's category agreement is partly by construction."),
    code("REP = 'B_type'; Xc = REPS[REP]; sweep = sweeps[REP]\ncand = sweep.loc[20:60]\n"
         "rk = cand.silhouette.rank(ascending=False) + cand.davies_bouldin.rank() + cand.nmi_top.rank(ascending=False) + cand.stability_ari.rank(ascending=False)\n"
         "best[REP] = int(rk.idxmin()); rk.rename('rank_sum').to_frame().T"),
    code("K = best[REP]; print(REP, '| chosen k =', K, '| stability at K =', round(sweep.loc[K, 'stability_ari'], 3))\n"
         "fig, ax = plt.subplots(1, 3, figsize=(13, 3.2))\n"
         "sweep[['silhouette']].plot(ax=ax[0], marker='o', title='Cosine silhouette (5k sample)'); sweep[['stability_ari']].plot(ax=ax[1], marker='o', title='Seed stability (mean ARI)')\n"
         "sweep[['nmi_top', 'nmi_leaf']].plot(ax=ax[2], marker='o', title='NMI vs OFF categories')\n"
         "for a in ax: a.axvline(K, ls='--', c='grey')\nplt.tight_layout(); plt.savefig(OUT / 'k_sweep.png', dpi=150); plt.show()"),
    md("## 2. Comparator: HDBSCAN on PCA-50\nDensity-based clustering does not need k but labels low-density points as noise; the app needs every product in a theme."),
    code("mean, comps, evr = S.pca_fit(X)  # retrieval space (dimensionality study)\nmc, cc, evr_c = S.pca_fit(Xc)  # clustering space (HDBSCAN input, map)\nZ50 = S.pca_project(Xc, mc, cc, 50)\nt = time.perf_counter()\n"
         "hd = HDBSCAN(min_cluster_size=25, min_samples=5).fit_predict(Z50); hd_s = time.perf_counter() - t\nmask = hd >= 0\n"
         "labels_k, centroids = S.spherical_kmeans(Xc, K, SEED, n_init=20)\n"
         "comparison = pd.DataFrame({\n  f'kmeans_k{K}': {'clusters': K, 'noise_share': 0.0, 'nmi_top': NMI(top, labels_k), 'nmi_top_on_hdbscan_core': NMI(top[mask], labels_k[mask])},\n"
         "  'hdbscan': {'clusters': int(hd.max() + 1), 'noise_share': float(1 - mask.mean()), 'nmi_top': NMI(top, hd), 'nmi_top_on_hdbscan_core': NMI(top[mask], hd[mask])}}).T\n"
         "comparison['fit_s'] = [sweep.loc[K, 'fit_s'], hd_s]; comparison.round(4)"),
    md("## 3. Cluster themes\nAutomatic label = dominant leaf category in the cluster; c-TF-IDF terms are shown for review. Labels are machine-generated "
       "and still need a human pass before they are shown in the UI as final."),
    code("themes = S.label_clusters(labels_k, cat, texts)\nth = pd.DataFrame(themes).set_index('cluster')\n"
         "pd.set_option('display.max_rows', 80, 'display.max_colwidth', 70)\nth.sort_values('size', ascending=False)"),
    code("fig, ax = plt.subplots(1, 2, figsize=(11, 3.2))\nth['size'].sort_values().plot.barh(ax=ax[0], title=f'Cluster sizes (k={K})', fontsize=6)\n"
         "ax[1].hist(th.top_purity, bins=10, range=(0, 1)); ax[1].set_title('Top-level category purity per cluster')\n"
         "plt.tight_layout(); plt.savefig(OUT / 'clusters.png', dpi=150); plt.show()\n"
         "purity = {'median_top_purity': float(th.top_purity.median()), 'median_label_share': float(th.label_share.median()),\n"
         "          'smallest': int(th['size'].min()), 'largest': int(th['size'].max())}; purity"),
    md("## 4. Degenerate cases\nAssignment of a new (uncatalogued) product by nearest centroid must be deterministic, finite and agree with the fit."),
    code("agree = float((S.assign(Xc, centroids) == labels_k).mean())\nzero = S.assign(R.normalize(np.zeros((1, Xc.shape[1]))), centroids)\n"
         "dup = S.assign(np.vstack([Xc[0], Xc[0]]), centroids)\nedge = {'refit_assign_agreement': agree, 'zero_vector_assigns_to': int(zero[0]), 'duplicates_same_cluster': bool(dup[0] == dup[1]),\n"
         "        'empty_clusters': int(K - len(np.unique(labels_k)))}; edge"),
    md("## 5. PCA: explained variance"),
    code("cum = np.cumsum(evr)\nvar_summary = {f'pc{d}': float(cum[d - 1]) for d in (2, 16, 32, 64, 128)}\n"
         "var_summary.update({'dims_for_80pct': int(np.searchsorted(cum, 0.8) + 1), 'dims_for_95pct': int(np.searchsorted(cum, 0.95) + 1)})\n"
         "fig, ax = plt.subplots(figsize=(6, 3.2)); ax.plot(np.arange(1, len(cum) + 1), cum); ax.set_xlabel('components'); ax.set_ylabel('cumulative explained variance')\n"
         "ax.axhline(0.8, ls=':', c='grey'); ax.axhline(0.95, ls=':', c='grey'); ax.set_title('PCA on e5-small catalog embeddings')\n"
         "plt.tight_layout(); plt.savefig(OUT / 'pca_variance.png', dpi=150); plt.show(); var_summary"),
    md("## 6. PCA: does reduction preserve retrieval?\nOverlap@10 with full-dimension neighbours and full-text nDCG@10 (category-inflated, used only for the *relative* loss). "
       "Rule from the design: adopt a reduced d only if overlap ≥ 0.95 and nDCG@10 loss ≤ 0.01."),
    code("from collections import defaultdict\ngroups_by = defaultdict(set)\nfor i, p in enumerate(cat): groups_by[p['category']].add(i)\n"
         "groups = {i: groups_by[p['category']] for i, p in enumerate(cat)}\n"
         "queries = sorted(random.Random(SEED).sample([i for i in range(len(cat)) if len(groups[i]) >= 5], 500))\n"
         "full_top = {q: set(R.top_k(X, q, 10)) for q in queries}; base = R.evaluate(X, queries, groups)['ndcg@10']\nred = []\n"
         "for d in (16, 32, 64, 128):\n    Xd = R.normalize(S.pca_project(X, mean, comps, d))\n"
         "    ov = np.mean([len(full_top[q] & set(R.top_k(Xd, q, 10))) / 10 for q in queries])\n"
         "    nd = R.evaluate(Xd, queries, groups)['ndcg@10']\n    red.append({'d': d, 'overlap@10': ov, 'ndcg@10': nd, 'ndcg_loss': base - nd, 'adopt': ov >= 0.95 and base - nd <= 0.01})\n"
         "red = pd.DataFrame(red).set_index('d'); print('full-dimension nDCG@10 =', round(base, 4)); red.round(4)"),
    md("## 7. 2-D product map\nPCA(2) is a linear projection, so clusters overlap; it is an orientation aid, not a quality claim. Colours = 12 largest clusters."),
    code("Z2 = S.pca_project(Xc, mc, cc, 2); cum_c = np.cumsum(evr_c)\nbig = th['size'].sort_values(ascending=False).index[:12]\nfig, ax = plt.subplots(figsize=(8, 6))\n"
         "ax.scatter(Z2[:, 0], Z2[:, 1], s=2, c='lightgrey')\n"
         "for c in big:\n    m = labels_k == c; ax.scatter(Z2[m, 0], Z2[m, 1], s=3, label=f\"{th.loc[c, 'label'][:22]} ({m.sum()})\")\n"
         "ax.legend(fontsize=7, markerscale=4, loc='best'); ax.set_title(f'PCA(2) map ({REP}), PC1+PC2 = {cum_c[1]:.1%} variance')\n"
         "plt.tight_layout(); plt.savefig(OUT / 'pca_map.png', dpi=150); plt.show()\n"
         "sep = {'nmi_top_vs_kmeans_on_2d': NMI(labels_k, S.assign(R.normalize(Z2), R.normalize(np.vstack([Z2[labels_k == c].mean(0) for c in range(K)]))))}; sep"),
    md("## 8. Runtime (desktop CPU)"),
    code("q = Xc[123:124]\nrt = {'assign_single': R.timed(lambda: S.assign(q, centroids), 500), 'pca2_project_single': R.timed(lambda: S.pca_project(q, mc, cc, 2), 500),\n"
         "      'kmeans_fit_k': {'mean_s': float(sweep.loc[K, 'fit_s'])}, 'hdbscan_fit': {'s': hd_s}}\nrt"),
    md("## 9. Export (git-ignored runtime artifacts + committed results)"),
    code("P = ROOT / 'data/processed'\nnp.save(P / 'cluster_centroids.npy', centroids); np.save(P / 'cluster_labels.npy', labels_k.astype(np.int32))\n"
         "np.savez(P / 'pca.npz', mean=mc, components=cc[:2]); np.save(P / f'embeddings_cluster_{REP}.npy', Xc); (P / 'cluster_themes.json').write_text(json.dumps(themes, ensure_ascii=False, indent=1))\n"
         "rng = np.random.default_rng(SEED); sample = np.sort(np.concatenate([rng.choice(np.flatnonzero(labels_k == c), min(25, (labels_k == c).sum()), replace=False) for c in range(K)]))\n"
         "(P / 'map_sample.json').write_text(json.dumps([{'id': cat[i]['id'], 'x': round(float(Z2[i, 0]), 5), 'y': round(float(Z2[i, 1]), 5), 'cluster': int(labels_k[i])} for i in sample]))\n"
         "summary = {'env': env, 'k_sweep': {str(k): {c: round(float(v), 4) for c, v in r.items()} for k, r in sweep.iterrows()}, 'k': K, 'representation': REP, 'original_rule_choice': {'representation': original_choice, 'k': best_original}, 'representation_table': {r: {c: (None if pd.isna(v) else round(float(v), 4)) for c, v in row.items()} for r, row in rep_table.iterrows()},\n"
         "  'comparison': {i: {c: round(float(v), 4) for c, v in r.items()} for i, r in comparison.iterrows()}, 'purity': purity, 'edge_cases': edge,\n"
         "  'pca_variance': var_summary, 'pca_reduction': {str(d): {c: (bool(v) if c == 'adopt' else round(float(v), 4)) for c, v in r.items()} for d, r in red.iterrows()},\n"
         "  'full_dim_ndcg@10': base, 'map_sample_size': int(len(sample)), 'map_2d_separation': sep, 'runtime_ms': rt,\n"
         "  'artifact_sha256': {f: hashlib.sha256((P / f).read_bytes()).hexdigest() for f in ('cluster_centroids.npy', 'cluster_labels.npy', 'pca.npz')}}\n"
         "(OUT / 'summary.json').write_text(json.dumps(summary, indent=2, default=float)); sweep.round(4).to_csv(OUT / 'k_sweep.csv')\n"
         "th.to_csv(OUT / 'cluster_themes.csv'); red.round(4).to_csv(OUT / 'pca_reduction.csv')\n"
         "print(json.dumps({k: summary[k] for k in ('k', 'purity', 'pca_variance', 'edge_cases')}, indent=1))"),
    md("## Limitations\n- OFF categories are crowd-sourced and imperfect; NMI against them is an external sanity check, not ground truth.\n"
       "- Silhouette is computed on a seeded 5,000-point sample.\n- Theme labels are automatic; a human review pass is pending before UI use.\n"
       "- PCA(2) preserves little global variance of 384-d embeddings; non-linear maps (UMAP/t-SNE) would separate better but are not required."),
]
nbf.write(nb, "notebooks/02_clustering_pca.ipynb")
