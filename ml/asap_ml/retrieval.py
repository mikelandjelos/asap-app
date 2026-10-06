"""Shared text building, embedding, search and metrics for notebooks and artifact export (T-011)."""
import json
import time
from pathlib import Path

import numpy as np

MODELS = {  # name -> (HF id, pinned revision, text prefix)
    "e5-small": ("intfloat/multilingual-e5-small", "614241f622f53c4eeff9890bdc4f31cfecc418b3", "query: "),
    "minilm": ("sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2",
               "e8f8c211226b894fcb81acc59f3b34ba3efd5f42", ""),
}


def load_catalog(path="data/processed/catalog.jsonl"):
    return [json.loads(line) for line in Path(path).read_text(encoding="utf-8").splitlines()]


def tag_label(tag):
    return tag.split(":", 1)[-1].replace("-", " ")


def product_text(p, variant="full"):
    """`full` is the production text; `no_category` excludes every category/label field (leak-free eval)."""
    parts = [p["name"], p.get("brand", "")]
    if variant == "full":
        parts += [p.get("category", ""), ", ".join(tag_label(t) for t in p.get("categories", [])),
                  p.get("labels", "")]
    parts.append(p.get("description", ""))
    return " | ".join(x for x in parts if x)


def normalize(m):
    m = np.asarray(m, dtype=np.float32)
    return m / np.maximum(np.linalg.norm(m, axis=1, keepdims=True), 1e-12)


def encode(model_name, texts, batch_size=64):
    from sentence_transformers import SentenceTransformer
    hf_id, rev, prefix = MODELS[model_name]
    model = SentenceTransformer(hf_id, revision=rev, device="cpu")
    vecs = model.encode([prefix + t for t in texts], batch_size=batch_size, normalize_embeddings=True,
                        show_progress_bar=False, convert_to_numpy=True)
    return vecs.astype(np.float32), model


def top_k(vectors, q_index, k):
    """Exact cosine top-k over L2-normalized rows, excluding the query; ties broken by index."""
    scores = vectors @ vectors[q_index]
    scores[q_index] = -np.inf
    idx = np.argpartition(-scores, k)[:k]
    return idx[np.lexsort((idx, -scores[idx]))]


def metrics(ranked, relevant, k=10, pool=50):
    """Binary-relevance P@k, nDCG@k, MRR and recall@pool for one query."""
    rel = np.array([i in relevant for i in ranked], dtype=float)
    gains = rel[:k] / np.log2(np.arange(2, k + 2))
    ideal = (1 / np.log2(np.arange(2, min(len(relevant), k) + 2))).sum()
    first = np.flatnonzero(rel)
    return {
        f"p@{k}": rel[:k].mean(),
        f"ndcg@{k}": gains.sum() / ideal if ideal else 0.0,
        "mrr": 1 / (first[0] + 1) if len(first) else 0.0,
        f"recall@{pool}": rel[:pool].sum() / min(len(relevant), pool),
    }


def evaluate(vectors, queries, groups, k=10, pool=50):
    rows = [metrics(top_k(vectors, q, pool), groups[q] - {q}, k, pool) for q in queries]
    return {m: float(np.mean([r[m] for r in rows])) for m in rows[0]}


def timed(fn, repeat):
    times = []
    for _ in range(repeat):
        t = time.perf_counter()
        fn()
        times.append((time.perf_counter() - t) * 1000)
    return {"median_ms": float(np.median(times)), "p95_ms": float(np.percentile(times, 95))}


class OnnxEncoder:
    """The exact inference path the Java backend will mirror: tokenizer.json + ONNX + mean pooling + L2."""

    def __init__(self, model_dir, prefix, max_length=512):
        import onnxruntime as ort
        from tokenizers import Tokenizer
        self.tok = Tokenizer.from_file(str(Path(model_dir) / "tokenizer.json"))
        self.tok.enable_truncation(max_length)
        self.sess = ort.InferenceSession(str(Path(model_dir) / "model.onnx"), providers=["CPUExecutionProvider"])
        self.inputs = {i.name for i in self.sess.get_inputs()}
        self.prefix = prefix

    def encode(self, texts):
        out = []
        for t in texts:
            e = self.tok.encode(self.prefix + t)
            ids = np.array([e.ids], dtype=np.int64)
            mask = np.array([e.attention_mask], dtype=np.int64)
            feed = {"input_ids": ids, "attention_mask": mask}
            if "token_type_ids" in self.inputs:
                feed["token_type_ids"] = np.zeros_like(ids)
            hidden = self.sess.run(None, feed)[0]
            pooled = (hidden * mask[..., None]).sum(1) / mask.sum(1, keepdims=True)
            out.append(pooled[0])
        return normalize(np.stack(out))


def export_onnx(model_name, out_dir, opset=17):
    """Export the transformer encoder (last_hidden_state) plus tokenizer.json; pooling stays outside the graph."""
    import torch
    from transformers import AutoModel, AutoTokenizer
    hf_id, rev, prefix = MODELS[model_name]
    out = Path(out_dir)
    out.mkdir(parents=True, exist_ok=True)
    tok = AutoTokenizer.from_pretrained(hf_id, revision=rev)
    tok.save_pretrained(out)
    base = AutoModel.from_pretrained(hf_id, revision=rev).eval()

    class Encoder(torch.nn.Module):
        def __init__(self, m):
            super().__init__()
            self.m = m

        def forward(self, input_ids, attention_mask):
            return self.m(input_ids=input_ids, attention_mask=attention_mask).last_hidden_state

    model = Encoder(base).eval()
    sample = tok([prefix + "sample product"], return_tensors="pt")
    with torch.no_grad():
        torch.onnx.export(model, (sample["input_ids"], sample["attention_mask"]), str(out / "model.onnx"),
                          input_names=["input_ids", "attention_mask"], output_names=["last_hidden_state"],
                          dynamic_axes={n: {0: "batch", 1: "seq"} for n in ("input_ids", "attention_mask", "last_hidden_state")},
                          opset_version=opset, dynamo=False)
    return out
