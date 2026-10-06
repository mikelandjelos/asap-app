package rs.ac.ni.elfak.asap.backend.ai;

import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Set;
import rs.ac.ni.elfak.asap.backend.ai.CharTfidf.SparseVector;

/** Exact hybrid relevance {@code r(x) = α·cos_e5(q, x) + (1−α)·cos_tfidf(q, x)} over the whole catalog (D-030). */
public final class HybridRetriever {

    private final FloatMatrix embeddings;
    private final CharTfidf tfidf;
    private final LexicalIndex lexical;
    private final double alpha;

    public HybridRetriever(FloatMatrix embeddings, CharTfidf tfidf, LexicalIndex lexical, double alpha) {
        this.embeddings = embeddings;
        this.tfidf = tfidf;
        this.lexical = lexical;
        this.alpha = alpha;
    }

    public static HybridRetriever of(RuntimeBundle bundle) {
        CharTfidf tfidf = CharTfidf.of(bundle.tfidf());
        LexicalIndex lexical = new LexicalIndex(tfidf,
                bundle.catalog().stream().map(BundleModel.CatalogEntry::fullText).toList(), bundle.tfidf().vocabulary().size());
        return new HybridRetriever(bundle.embeddings(), tfidf, lexical, bundle.manifest().params().alpha());
    }

    /** Relevance of a catalog product against all products. */
    public double[] relevance(int catalogIndex) {
        return combine(embeddings.times(embeddings.row(catalogIndex)), lexical.scores(lexical.row(catalogIndex)));
    }

    /** Relevance of an uncatalogued product, given its encoded vector and full text. */
    public double[] relevance(float[] queryVector, String fullText) {
        return combine(embeddings.times(queryVector), lexical.scores(tfidf.transform(fullText)));
    }

    private double[] combine(double[] semantic, double[] lexicalScores) {
        double[] out = new double[semantic.length];
        for (int i = 0; i < out.length; i++) {
            out[i] = alpha * semantic[i] + (1 - alpha) * lexicalScores[i];
        }
        return out;
    }

    /** Top {@code pool} indices by descending score, ties broken by lower index; excluded indices are skipped. */
    public static int[] candidates(double[] scores, Set<Integer> exclude, int pool) {
        Comparator<Integer> worstFirst = Comparator.<Integer>comparingDouble(i -> scores[i]).thenComparing(Comparator.reverseOrder());
        PriorityQueue<Integer> heap = new PriorityQueue<>(pool + 1, worstFirst);
        for (int i = 0; i < scores.length; i++) {
            if (exclude.contains(i) || !Double.isFinite(scores[i])) {
                continue;
            }
            heap.add(i);
            if (heap.size() > pool) {
                heap.poll();
            }
        }
        Integer[] top = heap.toArray(new Integer[0]);
        Arrays.sort(top, worstFirst.reversed());
        return Arrays.stream(top).mapToInt(Integer::intValue).toArray();
    }

    public CharTfidf tfidf() {
        return tfidf;
    }
}
