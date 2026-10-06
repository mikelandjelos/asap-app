"""Build the offline ASAP catalog from official Open Food Facts family CSV exports (T-011/S2).

One streaming pass per export, bounded memory: a per-stratum heap keeps the best candidates,
then strata are interleaved round-robin up to each source's quota. Deterministic for a fixed seed.
Contains data from Open Food Facts, available under the Open Database License (ODbL 1.0).
"""
import csv
import gzip
import hashlib
import heapq
import json
import sys
import unicodedata
from pathlib import Path

csv.field_size_limit(sys.maxsize)

SEED = 20261006
REGION = {"en:serbia", "en:croatia", "en:bosnia-and-herzegovina", "en:montenegro",
          "en:north-macedonia", "en:slovenia", "en:hungary", "en:romania", "en:bulgaria"}
SOURCES = [  # (label, file, quota, number of top-level strata kept)
    ("open_food_facts", "en.openfoodfacts.org.products.csv.gz", 8000, 30),
    ("open_beauty_facts", "en.openbeautyfacts.org.products.csv.gz", 1200, 12),
    ("open_pet_food_facts", "en.openpetfoodfacts.org.products.csv.gz", 800, 8),
]
PER_STRATUM_POOL = 3000
META_TAGS = {"en:undefined", "en:non-food-products", "en:open-beauty-facts", "en:open-pet-food-facts",
             "en:open-products-facts", "en:non-open-products-facts", "en:to-be-completed"}


def gs1_valid(code):
    digits = [int(c) for c in code]
    body, check = digits[:-1], digits[-1]
    total = sum(d * (3 if i % 2 == 0 else 1) for i, d in enumerate(reversed(body)))
    return (10 - total % 10) % 10 == check


def barcode_format(code):
    """Return EAN_13 / UPC_A / EAN_8 for public-circulation GTINs, else None."""
    if not code.isdigit() or len(code) not in (8, 12, 13) or not gs1_valid(code):
        return None
    if len(code) == 13:
        if code[0] == "2" or code[:3] in {f"0{p}" for p in range(20, 30)} | {f"0{p}" for p in range(40, 50)}:
            return None  # in-store / restricted circulation
        return "EAN_13"
    if len(code) == 12:
        return None if code[0] in "24" else "UPC_A"
    return None if code[0] in "02" else "EAN_8"


def clean(text, limit):
    text = unicodedata.normalize("NFC", (text or "").strip())
    return text[:limit] if text else ""


def latin_script(text):
    letters = [c for c in text if c.isalpha()]
    return bool(letters) and sum(unicodedata.name(c, "").startswith("LATIN") for c in letters) >= 0.9 * len(letters)


def tiebreak(code):
    return hashlib.sha256(f"{SEED}:{code}".encode()).hexdigest()


def candidates(path, label):
    with gzip.open(path, "rt", encoding="utf-8", newline="") as f:
        for row in csv.DictReader(f, delimiter="\t", quoting=csv.QUOTE_NONE):
            code = (row.get("code") or "").strip()
            fmt = barcode_format(code)
            name = clean(row.get("product_name"), 200)
            cats = [c for c in (row.get("categories_tags") or "").split(",")
                    if c.startswith("en:") and c not in META_TAGS]
            category = clean(row.get("main_category_en"), 120)
            if not fmt or not name or not cats or not latin_script(name) or not category or ":" in category:
                continue
            countries = set((row.get("countries_tags") or "").split(","))
            try:
                completeness = float(row.get("completeness") or 0)
            except ValueError:
                completeness = 0.0
            yield {
                "id": f"off:{code}", "barcode": {"value": code, "format": fmt}, "name": name,
                "brand": clean((row.get("brands") or "").split(",")[0], 120),
                "categories": cats[-3:], "top_category": cats[0],
                "category": category,
                "description": clean(row.get("generic_name"), 2000),
                "labels": clean(row.get("labels_en"), 300),
                "region": bool(countries & REGION), "completeness": completeness,
                "source": label, "last_modified_t": row.get("last_modified_t") or "",
            }


def select(path, label, quota, n_strata):
    pools, counts, seen, scanned = {}, {}, set(), 0
    for item in candidates(path, label):
        scanned += 1
        code = item["barcode"]["value"]
        if code in seen:
            continue
        seen.add(code)
        counts[item["top_category"]] = counts.get(item["top_category"], 0) + 1
        key = (item["region"], item["completeness"], tiebreak(code))
        heap = pools.setdefault(item["top_category"], [])
        if len(heap) < PER_STRATUM_POOL:
            heapq.heappush(heap, (key, code, item))
        elif key > heap[0][0]:
            heapq.heapreplace(heap, (key, code, item))
    strata = sorted(counts, key=lambda s: (-counts[s], s))[:n_strata]
    ordered = {s: [it for _, _, it in sorted(pools[s], reverse=True)] for s in strata}
    weight = {s: counts[s] ** 0.5 for s in strata}
    total = sum(weight.values())
    take = {s: min(len(ordered[s]), int(quota * weight[s] / total)) for s in strata}
    while sum(take.values()) < quota and any(take[s] < len(ordered[s]) for s in strata):
        for s in strata:  # distribute rounding remainder
            if take[s] < len(ordered[s]) and sum(take.values()) < quota:
                take[s] += 1
    chosen = [it for s in strata for it in ordered[s][:take[s]]]
    return chosen, {"eligible_unique": len(seen), "eligible_rows": scanned, "strata_total": len(counts),
                    "strata_kept": {s: take[s] for s in strata}}


def build(raw_dir="data/raw", out_dir="data/processed", manifest="data/catalog_manifest.json"):
    raw, out = Path(raw_dir), Path(out_dir)
    out.mkdir(parents=True, exist_ok=True)
    catalog, stats = [], {}
    for label, fname, quota, n_strata in SOURCES:
        items, s = select(raw / fname, label, quota, n_strata)
        s["selected"] = len(items)
        s["region_selected"] = sum(it["region"] for it in items)
        stats[label] = s
        catalog.extend(items)
        print(label, s, flush=True)
    seen, unique = set(), []
    for it in catalog:  # cross-source dedupe by barcode, first source wins
        if it["barcode"]["value"] not in seen:
            seen.add(it["barcode"]["value"])
            unique.append(it)
    path = out / "catalog.jsonl"
    with path.open("w", encoding="utf-8") as f:
        for it in unique:
            f.write(json.dumps(it, ensure_ascii=False) + "\n")
    sums = dict(line.split()[::-1] for line in (raw / "sha256.txt").read_text().splitlines())
    Path(manifest).write_text(json.dumps({
        "attribution": "Contains data from Open Food Facts, available under the Open Database License (ODbL 1.0).",
        "exports": {fname: {"url_base": "https://static.open{}.org/data/".format(label.split("_", 1)[1].replace("_", "")),
                            "sha256": sums.get(fname)} for label, fname, _, _ in SOURCES},
        "downloaded_utc": (raw / "download_utc.txt").read_text().strip(),
        "config": {"seed": SEED, "region": sorted(REGION), "quotas": {l: q for l, _, q, _ in SOURCES}, "strata_kept": {l: n for l, _, _, n in SOURCES},
                   "filters": "valid public GTIN, Latin-script name, en: taxonomy categories, English main category",
                   "per_stratum_pool": PER_STRATUM_POOL},
        "stats": stats, "total": len(unique),
        "catalog_sha256": hashlib.sha256(path.read_bytes()).hexdigest(),
    }, indent=2) + "\n")
    print("total", len(unique))


if __name__ == "__main__":
    build()
