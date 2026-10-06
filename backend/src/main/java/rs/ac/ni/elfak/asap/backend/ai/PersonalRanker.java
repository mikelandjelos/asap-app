package rs.ac.ni.elfak.asap.backend.ai;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Multi-interest personalization, MMR and map projection (D-031, D-032, D-033); mirrors {@code ml/asap_ml/ranking.py}.
 * History is a newest-first list of catalog indices; repeats add weight, readiness counts distinct products.
 */
public final class PersonalRanker {

    private PersonalRanker() {
    }

    public static boolean sufficient(List<Integer> history, int minDistinct) {
        return new HashSet<>(history).size() >= minDistinct;
    }

    /** Uniform-weight interest centroids per type cluster; returns the one closest to the query vector. */
    public static float[] multiInterestProfile(FloatMatrix embeddings, List<Integer> history, int[] clusters, float[] query) {
        Map<Integer, double[]> sums = new TreeMap<>();
        for (int item : history) {
            double[] sum = sums.computeIfAbsent(clusters[item], c -> new double[embeddings.cols()]);
            int offset = item * embeddings.cols();
            for (int j = 0; j < sum.length; j++) {
                sum[j] += embeddings.data()[offset + j];
            }
        }
        float[] best = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        for (double[] sum : sums.values()) {
            float[] centroid = new float[sum.length];
            for (int j = 0; j < sum.length; j++) {
                centroid[j] = (float) sum[j];
            }
            centroid = VectorMath.normalize(centroid);
            double score = VectorMath.dot(centroid, query);
            if (score > bestScore) {
                bestScore = score;
                best = centroid;
            }
        }
        return best;
    }

    /** {@code s(x) = (1−β)·r(q, x) + β·cos(p, x)}. */
    public static double[] personalize(double[] relevance, FloatMatrix embeddings, float[] profile, double beta) {
        double[] affinity = embeddings.times(profile);
        double[] out = new double[relevance.length];
        for (int i = 0; i < out.length; i++) {
            out[i] = (1 - beta) * relevance[i] + beta * affinity[i];
        }
        return out;
    }

    /** Greedy MMR: argmax {@code λ·s(x) − (1−λ)·max_{y∈S} cos(x, y)}; ties go to the higher-relevance position. */
    public static int[] mmr(int[] candidates, double[] scores, FloatMatrix embeddings, double lambda, int n) {
        int m = candidates.length;
        double[] maxSim = new double[m];
        java.util.Arrays.fill(maxSim, Double.NEGATIVE_INFINITY);
        boolean[] taken = new boolean[m];
        List<Integer> chosen = new ArrayList<>();
        for (int step = 0; step < Math.min(n, m); step++) {
            int best = -1;
            double bestValue = Double.NEGATIVE_INFINITY;
            for (int j = 0; j < m; j++) {
                if (taken[j]) {
                    continue;
                }
                double penalty = Double.isFinite(maxSim[j]) ? maxSim[j] : 0.0;
                double value = lambda * scores[candidates[j]] - (1 - lambda) * penalty;
                if (value > bestValue) {
                    bestValue = value;
                    best = j;
                }
            }
            taken[best] = true;
            chosen.add(candidates[best]);
            float[] picked = embeddings.row(candidates[best]);
            for (int j = 0; j < m; j++) {
                maxSim[j] = Math.max(maxSim[j], embeddings.dot(candidates[j], picked));
            }
        }
        return chosen.stream().mapToInt(Integer::intValue).toArray();
    }

    public static int nearestCluster(FloatMatrix centroids, float[] typeVector) {
        int best = 0;
        double bestScore = Double.NEGATIVE_INFINITY;
        for (int c = 0; c < centroids.rows(); c++) {
            double score = centroids.dot(c, typeVector);
            if (score > bestScore) {
                bestScore = score;
                best = c;
            }
        }
        return best;
    }

    /** PCA(2) map position of a type-space vector. */
    public static double[] project(FloatMatrix mean, FloatMatrix components, float[] vector) {
        double[] xy = new double[components.rows()];
        for (int k = 0; k < xy.length; k++) {
            double sum = 0;
            for (int j = 0; j < vector.length; j++) {
                sum += (vector[j] - mean.data()[j]) * (double) components.data()[k * components.cols() + j];
            }
            xy[k] = sum;
        }
        return xy;
    }

    /** "You" on the map: normalized mean of the history's type embeddings (D-033). */
    public static float[] youVector(FloatMatrix typeEmbeddings, List<Integer> history) {
        double[] sum = new double[typeEmbeddings.cols()];
        for (int item : history) {
            int offset = item * typeEmbeddings.cols();
            for (int j = 0; j < sum.length; j++) {
                sum[j] += typeEmbeddings.data()[offset + j];
            }
        }
        float[] mean = new float[sum.length];
        for (int j = 0; j < sum.length; j++) {
            mean[j] = (float) (sum[j] / history.size());
        }
        return VectorMath.normalize(mean);
    }
}
