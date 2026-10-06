"""Collect front-image URLs for catalog products from the OFF-family exports (T-011/S7c.1).

Writes data/processed/images.json {product id: https URL}; catalog.jsonl and all notebook inputs stay unchanged.
Images are CC BY-SA (Open Food Facts contributors) and are loaded by the app directly from the OFF image servers.
"""
import csv
import gzip
import json
import sys
from pathlib import Path

csv.field_size_limit(sys.maxsize)

EXPORTS = ["en.openfoodfacts.org.products.csv.gz", "en.openbeautyfacts.org.products.csv.gz",
           "en.openpetfoodfacts.org.products.csv.gz"]
ALLOWED_PREFIXES = ("https://images.openfoodfacts.org/", "https://images.openbeautyfacts.org/",
                    "https://images.openpetfoodfacts.org/", "https://static.openfoodfacts.org/")


def build(raw_dir="data/raw", catalog="data/processed/catalog.jsonl", out="data/processed/images.json"):
    wanted = {}
    for line in Path(catalog).read_text(encoding="utf-8").splitlines():
        p = json.loads(line)
        wanted[p["barcode"]["value"]] = p["id"]
    images = {}
    for name in EXPORTS:
        with gzip.open(Path(raw_dir) / name, "rt", encoding="utf-8", newline="") as f:
            for row in csv.DictReader(f, delimiter="\t", quoting=csv.QUOTE_NONE):
                pid = wanted.get((row.get("code") or "").strip())
                if pid is None or pid in images:
                    continue
                url = (row.get("image_small_url") or row.get("image_url") or "").strip()
                if url.startswith(ALLOWED_PREFIXES):
                    images[pid] = url
    Path(out).write_text(json.dumps(images, sort_keys=True, indent=0) + "\n")
    print(json.dumps({"catalog": len(wanted), "with_image": len(images),
                      "coverage": round(len(images) / len(wanted), 4)}))


if __name__ == "__main__":
    build()
