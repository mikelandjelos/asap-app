"""History profile, personalized scoring and MMR shared by notebook 03 and the backend spec (T-011/S5)."""
import numpy as np

from .retrieval import normalize

MIN_DISTINCT = 3  # readiness threshold: fewer distinct known products -> COLD_START


def recency_weights(n, half_life):
    """w_i = 0.5^(i/H), i = 0 for the newest event; H = inf gives uniform weights."""
    i = np.arange(n, dtype=np.float32)
    return np.ones(n, np.float32) if np.isinf(half_life) else 0.5 ** (i / half_life)


def readiness(history_idx):
    return "SUFFICIENT" if len(set(history_idx)) >= MIN_DISTINCT else "COLD_START"


def centroid_profile(E, history_idx, half_life):
    w = recency_weights(len(history_idx), half_life)
    return normalize((w[:, None] * E[history_idx]).sum(0, keepdims=True))[0]


def multi_interest_profile(E, history_idx, half_life, clusters, query_vec):
    """Group history by type cluster; return the interest centroid closest to the query."""
    w = recency_weights(len(history_idx), half_life)
    labels = clusters[history_idx]
    cents = [normalize((w[labels == c, None] * E[np.asarray(history_idx)[labels == c]]).sum(0, keepdims=True))[0]
             for c in np.unique(labels)]
    cents = np.stack(cents)
    return cents[np.argmax(cents @ query_vec)]


def personalized_scores(relevance, E, profile, beta):
    """s(x) = (1-beta) * r(q, x) + beta * cos(p, x)."""
    return (1 - beta) * relevance + beta * (E @ profile)


def candidates(scores, exclude, pool=50):
    s = scores.copy()
    s[list(exclude)] = -np.inf
    pool = min(pool, int(np.isfinite(s).sum()))
    idx = np.argpartition(-s, pool - 1)[:pool] if pool < len(s) else np.arange(len(s))
    idx = idx[np.isfinite(s[idx])]
    return idx[np.lexsort((idx, -s[idx]))]


def mmr(cand, scores, E, lam, n=10):
    """Greedy MMR: argmax lam*s(x) - (1-lam)*max_{y in S} cos(x, y). lam = 1 reproduces relevance order."""
    cand = list(cand)
    chosen = []
    sim = E[cand] @ E[cand].T
    max_sim = np.full(len(cand), -np.inf)
    rel = scores[cand]
    remaining = np.ones(len(cand), bool)
    for _ in range(min(n, len(cand))):
        penalty = np.where(np.isfinite(max_sim), max_sim, 0.0)
        val = np.where(remaining, lam * rel - (1 - lam) * penalty, -np.inf)
        j = int(np.argmax(val))  # ties -> lowest position = higher relevance rank
        chosen.append(cand[j])
        remaining[j] = False
        max_sim = np.maximum(max_sim, sim[j])
    return np.array(chosen)


def ild(items, E):
    """Intra-list diversity: 1 - mean pairwise cosine."""
    if len(items) < 2:
        return 0.0
    s = E[items] @ E[items].T
    m = len(items)
    return float(1 - (s.sum() - np.trace(s)) / (m * (m - 1)))


def graded_ndcg(ranked, grades, k=10):
    """Graded nDCG@k with gain 2^g - 1; `grades` is an int array over the whole catalog."""
    g = grades[np.asarray(ranked[:k], int)].astype(float)
    dcg = ((2 ** g - 1) / np.log2(np.arange(2, len(g) + 2))).sum()
    ideal = np.sort(grades[grades > 0])[::-1][:k].astype(float)
    idcg = ((2 ** ideal - 1) / np.log2(np.arange(2, len(ideal) + 2))).sum()
    return float(dcg / idcg) if idcg else 0.0
