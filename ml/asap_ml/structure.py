"""Clustering (spherical k-means) and PCA shared by notebook 02 and the backend artifact export (T-011/S4)."""
from collections import Counter

import numpy as np
from sklearn.cluster import KMeans
from sklearn.feature_extraction.text import CountVectorizer

from .retrieval import normalize


def spherical_kmeans(vectors, k, seed, n_init=4):
    """k-means on L2-normalized vectors with unit-normalized centroids; assignment = max cosine."""
    km = KMeans(n_clusters=k, n_init=n_init, random_state=seed).fit(vectors)
    centroids = normalize(km.cluster_centers_)
    return assign(vectors, centroids), centroids


def assign(vectors, centroids):
    return np.argmax(vectors @ centroids.T, axis=1)


def label_clusters(labels, catalog, texts, top_terms=5):
    """Label = dominant leaf category; plus purity, top-level category and c-TF-IDF terms per cluster."""
    k = labels.max() + 1
    docs = [" ".join(texts[i] for i in np.flatnonzero(labels == c)) for c in range(k)]
    cv = CountVectorizer(min_df=2, max_df=0.5, token_pattern=r"(?u)\b[^\W\d_]{3,}\b")
    tf = cv.fit_transform(docs).astype(float)
    tf = tf.multiply(1 / np.maximum(tf.sum(axis=1), 1))
    idf = np.log(1 + tf.shape[0] / np.maximum((tf > 0).sum(axis=0).A1, 1))
    ctfidf = tf.multiply(idf).tocsr()
    vocab = np.array(cv.get_feature_names_out())
    out = []
    for c in range(k):
        members = np.flatnonzero(labels == c)
        cats = Counter(catalog[i]["category"] for i in members)
        tops = Counter(catalog[i]["top_category"] for i in members)
        row = ctfidf[c].toarray().ravel()
        dominant, n = cats.most_common(1)[0]
        out.append({"cluster": c, "size": int(len(members)), "label": dominant,
                    "label_share": round(n / len(members), 3),
                    "top_category": tops.most_common(1)[0][0].split(":", 1)[-1],
                    "top_purity": round(tops.most_common(1)[0][1] / len(members), 3),
                    "terms": vocab[np.argsort(-row)[:top_terms]].tolist()})
    return out


def pca_fit(vectors):
    """Centered PCA via SVD; returns mean, components (rows) and explained-variance ratios."""
    mean = vectors.mean(axis=0)
    _, s, vt = np.linalg.svd(vectors - mean, full_matrices=False)
    var = s ** 2
    return mean.astype(np.float32), vt.astype(np.float32), var / var.sum()


def pca_project(vectors, mean, components, d):
    return (np.atleast_2d(vectors) - mean) @ components[:d].T
