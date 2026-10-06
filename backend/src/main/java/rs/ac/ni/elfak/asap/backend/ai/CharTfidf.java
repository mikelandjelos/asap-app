package rs.ac.ni.elfak.asap.backend.ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 * Port of sklearn {@code TfidfVectorizer(analyzer="char_wb", ngram_range=(3, 5), sublinear_tf=True)} with a fixed,
 * exported vocabulary and idf. Mirrors {@code ml/verify_bundle.py}; n-grams are taken over Unicode code points.
 */
public final class CharTfidf {

    private static final Pattern WHITESPACE = Pattern.compile("\\s+", Pattern.UNICODE_CHARACTER_CLASS);

    private final Map<String, Integer> index;
    private final double[] idf;
    private final int minN;
    private final int maxN;

    public CharTfidf(List<String> vocabulary, double[] idf, int minN, int maxN) {
        this.index = new HashMap<>(vocabulary.size() * 2);
        for (int i = 0; i < vocabulary.size(); i++) {
            index.put(vocabulary.get(i), i);
        }
        this.idf = idf.clone();
        this.minN = minN;
        this.maxN = maxN;
    }

    public static CharTfidf of(BundleModel.TfidfSpec spec) {
        return new CharTfidf(spec.vocabulary(), spec.idf(), 3, 5);
    }

    /** sklearn {@code _char_wb_ngrams}: words padded with one space, short words counted once. */
    public static List<String> ngrams(String text, int minN, int maxN) {
        List<String> grams = new ArrayList<>();
        for (String word : WHITESPACE.split(text.toLowerCase(Locale.ROOT).strip())) {
            if (word.isEmpty()) {
                continue;
            }
            int[] w = (" " + word + " ").codePoints().toArray();
            for (int n = minN; n <= maxN; n++) {
                int offset = 0;
                grams.add(new String(w, offset, Math.min(n, w.length)));
                while (offset + n < w.length) {
                    offset++;
                    grams.add(new String(w, offset, n));
                }
                if (offset == 0) {
                    break;
                }
            }
        }
        return grams;
    }

    public SparseVector transform(String text) {
        TreeMap<Integer, Integer> counts = new TreeMap<>();
        for (String gram : ngrams(text, minN, maxN)) {
            Integer term = index.get(gram);
            if (term != null) {
                counts.merge(term, 1, Integer::sum);
            }
        }
        int[] indices = new int[counts.size()];
        double[] values = new double[counts.size()];
        double norm = 0;
        int k = 0;
        for (Map.Entry<Integer, Integer> e : counts.entrySet()) {
            indices[k] = e.getKey();
            values[k] = (1 + Math.log(e.getValue())) * idf[e.getKey()];
            norm += values[k] * values[k];
            k++;
        }
        norm = Math.sqrt(norm);
        for (int i = 0; i < values.length; i++) {
            values[i] /= norm;
        }
        return new SparseVector(indices, values);
    }

    /** Sorted term indices with their l2-normalized weights. */
    public record SparseVector(int[] indices, double[] values) {
    }
}
