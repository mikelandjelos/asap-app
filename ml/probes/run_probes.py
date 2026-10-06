"""T-011/S2 bounded provider probes (protocol: docs/PRODUCT_DATA_API_EVALUATION.md).

Sequential, one connection, >= 11 s between calls, no retries, no redirects followed.
Records presence flags and SHA-256 of response bytes only; raw payloads are not stored.
"""
import csv
import hashlib
import json
import sys
import time
import urllib.error
import urllib.request
from datetime import datetime, timezone
from pathlib import Path

USER_AGENT = "ASAP-evaluation/0.1 (https://github.com/mikelandjelos/asap-app)"
SPACING_S = 11.0
TIMEOUT_S = 10.0
HARD_CEILING_PER_PROVIDER = 14

CORPUS = [
    ("P01", "42070047", "EAN_8"),
    ("P02", "04963406", "UPC_E"),
    ("P03", "3017620422003", "EAN_13"),
    ("P04", "3850334341389", "EAN_13"),
    ("P05", "5000112519945", "EAN_13"),
    ("P06", "069000019832", "UPC_A"),
    ("P07", "069055838150", "UPC_A"),
    ("P08", "033287135141", "UPC_A"),
    ("P09", "883929540969", "UPC_A"),
    ("P10", "792692000115", "UPC_A"),
    ("P11", "717489740753", "UPC_A"),
    ("P12", "842885098716", "UPC_A"),
    ("U01", "9999999999994", "EAN_13"),
]

OFF_FIELDS = ("code,product_name,product_name_en,generic_name,generic_name_en,brands,"
              "categories_tags,labels_tags,lang,last_modified_t,product_type")

FIELDS = ["corpus_id", "value", "format", "provider", "endpoint", "utc", "http_status",
          "provider_code", "outcome", "content_type", "elapsed_ms", "returned_codes",
          "has_name", "has_brand", "has_category", "has_description", "has_tags",
          "has_language", "has_freshness", "product_type", "redirect", "rate_headers",
          "notes", "sha256"]


class NoRedirect(urllib.request.HTTPRedirectHandler):
    def redirect_request(self, *args, **kwargs):
        return None


OPENER = urllib.request.build_opener(NoRedirect)


def fetch(url):
    req = urllib.request.Request(url, headers={"User-Agent": USER_AGENT, "Accept": "application/json"})
    start = time.perf_counter()
    try:
        with OPENER.open(req, timeout=TIMEOUT_S) as resp:
            body, status, headers = resp.read(), resp.status, resp.headers
    except urllib.error.HTTPError as e:
        body, status, headers = e.read(), e.code, e.headers
    except Exception as e:  # transport failure -> UNAVAILABLE
        return None, None, {}, (time.perf_counter() - start) * 1000, repr(e)
    return body, status, headers, (time.perf_counter() - start) * 1000, ""


def nonblank(v):
    return bool(v) and (not isinstance(v, str) or v.strip() != "")


def parse_off(body, status):
    row = {}
    try:
        data = json.loads(body)
    except ValueError:
        return {"outcome": "UNAVAILABLE", "notes": "malformed JSON"}
    row["provider_code"] = data.get("result", {}).get("id", "")
    p = data.get("product") or {}
    if status == 200 and p and data.get("status") in ("success", "success_with_warnings", 1):
        name = p.get("product_name") or p.get("product_name_en")
        row.update(
            outcome="KNOWN" if nonblank(name) else "UNAVAILABLE",
            returned_codes=data.get("code", p.get("code", "")),
            has_name=nonblank(name), has_brand=nonblank(p.get("brands")),
            has_category=nonblank(p.get("categories_tags")),
            has_description=nonblank(p.get("generic_name") or p.get("generic_name_en")),
            has_tags=nonblank(p.get("labels_tags")), has_language=nonblank(p.get("lang")),
            has_freshness=nonblank(p.get("last_modified_t")), product_type=p.get("product_type", ""))
        if not nonblank(name):
            row["notes"] = "found without name"
    elif status == 404 or data.get("status") == "failure":
        row["outcome"] = "UNKNOWN"
    else:
        row["outcome"] = "UNAVAILABLE"
    return row


def parse_upc(body, status):
    try:
        data = json.loads(body)
    except ValueError:
        return {"outcome": "UNAVAILABLE", "notes": "non-JSON body"}
    row = {"provider_code": data.get("code", "")}
    items = data.get("items") or []
    if status == 200 and data.get("code") == "OK" and items:
        it = items[0]
        codes = [c for c in (it.get("ean"), it.get("upc"), it.get("gtin")) if c]
        row.update(
            outcome="KNOWN" if nonblank(it.get("title")) else "UNAVAILABLE",
            returned_codes="|".join(codes), has_name=nonblank(it.get("title")),
            has_brand=nonblank(it.get("brand")), has_category=nonblank(it.get("category")),
            has_description=nonblank(it.get("description")), has_tags=False,
            has_language=False, has_freshness=False)
        if len(items) > 1:
            row["notes"] = f"{len(items)} items returned"
    elif status == 200 and data.get("code") == "OK":
        row["outcome"] = "UNKNOWN"
    elif status == 404 or data.get("code") == "NOT_FOUND":
        row["outcome"] = "UNKNOWN"
    else:
        row["outcome"] = "UNAVAILABLE"
    return row


PROVIDERS = [
    ("open_food_facts",
     lambda v: f"https://world.openfoodfacts.org/api/v3/product/{v}?product_type=all&fields={OFF_FIELDS}",
     parse_off),
    ("upcitemdb", lambda v: f"https://api.upcitemdb.com/prod/trial/lookup?upc={v}", parse_upc),
]


def main(out_path):
    out = Path(out_path)
    out.parent.mkdir(parents=True, exist_ok=True)
    counts = {p[0]: 0 for p in PROVIDERS}
    last_call = 0.0
    with out.open("w", newline="") as f:
        w = csv.DictWriter(f, fieldnames=FIELDS)
        w.writeheader()
        for provider, url_for, parse in PROVIDERS:
            for cid, value, fmt in CORPUS:
                if value.startswith("200"):
                    raise SystemExit("restricted prefix in corpus")
                if counts[provider] >= HARD_CEILING_PER_PROVIDER:
                    raise SystemExit("request ceiling reached")
                wait = SPACING_S - (time.monotonic() - last_call)
                if wait > 0:
                    time.sleep(wait)
                url = url_for(value)
                utc = datetime.now(timezone.utc).isoformat(timespec="seconds")
                body, status, headers, ms, err = fetch(url)
                last_call = time.monotonic()
                counts[provider] += 1
                row = {k: "" for k in FIELDS}
                row.update(corpus_id=cid, value=value, format=fmt, provider=provider,
                           endpoint=url.split("?")[0], utc=utc, elapsed_ms=round(ms))
                if body is None:
                    row.update(outcome="UNAVAILABLE", notes=err)
                else:
                    row.update(http_status=status, content_type=headers.get("Content-Type", ""),
                               sha256=hashlib.sha256(body).hexdigest(),
                               redirect=headers.get("Location", "") if 300 <= status < 400 else "",
                               rate_headers=";".join(f"{k}={v}" for k, v in headers.items()
                                                     if "ratelimit" in k.lower() or k.lower() == "retry-after"))
                    if 300 <= status < 400:
                        row.update(outcome="UNAVAILABLE", notes="redirect not followed")
                    else:
                        row.update(parse(body, status))
                w.writerow(row)
                f.flush()
                print(provider, cid, row["http_status"], row["outcome"], row["elapsed_ms"], flush=True)
    print("calls", counts, flush=True)


if __name__ == "__main__":
    main(sys.argv[1] if len(sys.argv) > 1 else "notebooks/results/00_source_probes/observations.csv")
