package rs.ac.ni.elfak.asap.backend.ai;

import java.util.ArrayList;
import java.util.List;
import rs.ac.ni.elfak.asap.backend.ai.CharTfidf.SparseVector;

/** Inverted index over catalog TF-IDF rows, giving cosine scores of a query against every product. */
public final class LexicalIndex {

    private final int size;
    private final int[][] postingDocs;
    private final double[][] postingWeights;
    private final List<SparseVector> rows;

    public LexicalIndex(CharTfidf tfidf, List<String> texts, int vocabularySize) {
        this.size = texts.size();
        List<List<int[]>> docs = new ArrayList<>(vocabularySize);
        List<List<Double>> weights = new ArrayList<>(vocabularySize);
        for (int t = 0; t < vocabularySize; t++) {
            docs.add(new ArrayList<>());
            weights.add(new ArrayList<>());
        }
        List<SparseVector> built = new ArrayList<>(size);
        for (int d = 0; d < size; d++) {
            SparseVector row = tfidf.transform(texts.get(d));
            built.add(row);
            for (int k = 0; k < row.indices().length; k++) {
                docs.get(row.indices()[k]).add(new int[] {d});
                weights.get(row.indices()[k]).add(row.values()[k]);
            }
        }
        this.rows = List.copyOf(built);
        this.postingDocs = new int[vocabularySize][];
        this.postingWeights = new double[vocabularySize][];
        for (int t = 0; t < vocabularySize; t++) {
            postingDocs[t] = docs.get(t).stream().mapToInt(a -> a[0]).toArray();
            postingWeights[t] = weights.get(t).stream().mapToDouble(Double::doubleValue).toArray();
        }
    }

    public SparseVector row(int doc) {
        return rows.get(doc);
    }

    public double[] scores(SparseVector query) {
        double[] out = new double[size];
        for (int k = 0; k < query.indices().length; k++) {
            int term = query.indices()[k];
            double q = query.values()[k];
            int[] ds = postingDocs[term];
            double[] ws = postingWeights[term];
            for (int j = 0; j < ds.length; j++) {
                out[ds[j]] += q * ws[j];
            }
        }
        return out;
    }
}
