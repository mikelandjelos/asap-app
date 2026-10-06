"""Generate notebooks/00_source_probes.ipynb (executed separately with nbconvert)."""
import nbformat as nbf

md, code = nbf.v4.new_markdown_cell, nbf.v4.new_code_cell
nb = nbf.v4.new_notebook()
nb.cells = [
    md("# 00 — Product source probes (T-011/S2)\n\n"
       "Analyses the bounded read-only probe run produced by `ml/probes/run_probes.py` "
       "(protocol: `docs/PRODUCT_DATA_API_EVALUATION.md`). Inputs are presence flags and hashes only; "
       "no raw provider payloads are stored. Question: does combining providers through the router "
       "(design §2) improve field completeness over any single provider?"),
    code("import json, platform\nfrom pathlib import Path\nimport pandas as pd\nimport matplotlib\nmatplotlib.use('Agg')\n"
         "import matplotlib.pyplot as plt\n"
         "ROOT = Path.cwd().parent if Path.cwd().name == 'notebooks' else Path.cwd()\n"
         "OUT = ROOT / 'notebooks/results/00_source_probes'\n"
         "obs = pd.read_csv(OUT / 'observations.csv', dtype={'value': str})\n"
         "print(platform.python_version(), pd.__version__, len(obs), 'observations')\n"
         "obs[['corpus_id','provider','http_status','outcome','elapsed_ms','product_type']]"),
    md("## Outcome classes per provider\nU01 is the not-found control and is excluded from coverage."),
    code("cov = obs[obs.corpus_id != 'U01']\n"
         "outcomes = pd.crosstab(cov.provider, cov.outcome)\n"
         "control = obs[obs.corpus_id == 'U01'][['provider','outcome']]\n"
         "display(outcomes); display(control)"),
    md("## Field completeness: single providers vs router merge\n"
       "Merged = a field is present if *any* provider returned it for that product (field-level merge)."),
    code("FIELDS = ['has_name','has_brand','has_category','has_description']\n"
         "flags = cov.copy()\nfor f in FIELDS: flags[f] = flags[f].fillna(False).astype(str).eq('True')\n"
         "per = flags.pivot_table(index='corpus_id', columns='provider', values=FIELDS, aggfunc='first')\n"
         "rows = {}\nfor p in flags.provider.unique():\n"
         "    rows[p] = {f: per[(f, p)].mean() for f in FIELDS}\n"
         "rows['router_merge'] = {f: per[f].any(axis=1).mean() for f in FIELDS}\n"
         "completeness = pd.DataFrame(rows).T.rename(columns=lambda c: c.replace('has_',''))\n"
         "completeness['known_rate'] = [ (cov[cov.provider==p].outcome=='KNOWN').mean() for p in flags.provider.unique()] + [per['has_name'].any(axis=1).mean()]\n"
         "completeness.round(3)"),
    code("ax = completeness.plot.bar(figsize=(8,4), rot=0, ylim=(0,1.05))\n"
         "ax.set_ylabel('share of 12 corpus products'); ax.set_title('Field completeness by source (n=12)')\n"
         "plt.tight_layout(); plt.savefig(OUT / 'completeness.png', dpi=150); plt.show()"),
    md("## Which provider covers which product"),
    code("display(per['has_name'].replace({True:'✓', False:'—'}))"),
    md("## Latency (single sequential calls, desktop, not phone timing)"),
    code("lat = obs.groupby('provider').elapsed_ms.describe()[['count','mean','50%','max']]\nlat.round(0)"),
    md("## Identity handling (EAN-8 / UPC-E / UPC-A canonicalisation)"),
    code("display(obs[obs.corpus_id.isin(['P01','P02','P06'])][['corpus_id','value','format','provider','outcome','returned_codes']])"),
    md("## Export"),
    code("summary = {\n"
         "  'run_rows': len(obs), 'calls_per_provider': obs.provider.value_counts().to_dict(),\n"
         "  'completeness': completeness.round(4).to_dict(orient='index'),\n"
         "  'latency_ms': lat.round(1).to_dict(orient='index'),\n"
         "  'control_U01': control.set_index('provider').outcome.to_dict(),\n"
         "}\n(OUT / 'summary.json').write_text(json.dumps(summary, indent=2))\n"
         "completeness.round(4).to_csv(OUT / 'completeness.csv'); print(json.dumps(summary, indent=1))"),
    md("## Limitations\n12 products is a qualitative coverage check, not a statistical estimate. "
       "Corpus items P07–P12 were sourced from UPCitemdb's own public catalog, which biases toward it; "
       "P02–P06 come from OFF documentation, which biases toward OFF. The merge result is the meaningful "
       "comparison: the two sources are complementary."),
]
nbf.write(nb, "notebooks/00_source_probes.ipynb")
