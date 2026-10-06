package rs.ac.ni.elfak.asap.backend.ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import rs.ac.ni.elfak.asap.backend.ai.BundleModel.CatalogEntry;
import rs.ac.ni.elfak.asap.backend.ai.BundleModel.Params;

/** Catalog lookup plus the full hybrid → personalization → MMR pipeline over one runtime bundle. */
public final class RecommendationEngine {

    public enum Mode { GENERIC_SEMANTIC, PERSONALIZED_HISTORY }

    public enum HistoryState { COLD_START, APPLIED }

    public record Ranked(int index, double score) {
    }

    public record Result(Mode mode, HistoryState historyState, List<Ranked> items, double[] you) {
    }

    private final RuntimeBundle bundle;
    private final HybridRetriever retriever;
    private final Params params;
    private final int[] clusters;
    private final Map<String, Integer> byBarcode = new HashMap<>();
    private final Map<String, Integer> byId = new HashMap<>();

    public RecommendationEngine(RuntimeBundle bundle, HybridRetriever retriever) {
        this.bundle = bundle;
        this.retriever = retriever;
        this.params = bundle.manifest().params();
        List<CatalogEntry> catalog = bundle.catalog();
        this.clusters = new int[catalog.size()];
        for (CatalogEntry entry : catalog) {
            clusters[entry.index()] = entry.cluster();
            byBarcode.put(key(entry.barcode().value(), entry.barcode().format()), entry.index());
            byId.put(entry.id(), entry.index());
        }
    }

    private static String key(String value, String format) {
        return format + ":" + value;
    }

    public Optional<CatalogEntry> findByBarcode(String value, String format) {
        Integer index = byBarcode.get(key(value, format));
        return index == null ? Optional.empty() : Optional.of(bundle.catalog().get(index));
    }

    /** Maps newest-first history product IDs to catalog indices, dropping unknown IDs, within the window. */
    public List<Integer> historyIndices(List<String> productIds) {
        List<Integer> out = new ArrayList<>();
        for (String id : productIds) {
            if (out.size() == params.historyWindow()) {
                break;
            }
            Integer index = byId.get(id);
            if (index != null) {
                out.add(index);
            }
        }
        return out;
    }

    public Result recommend(int queryIndex, List<Integer> history) {
        double[] relevance = retriever.relevance(queryIndex);
        return rank(queryIndex, bundle.embeddings().row(queryIndex), relevance, history);
    }

    private Result rank(int queryIndex, float[] queryVector, double[] relevance, List<Integer> history) {
        boolean personal = PersonalRanker.sufficient(history, params.minDistinctHistory());
        double[] scores = relevance;
        if (personal) {
            float[] profile = PersonalRanker.multiInterestProfile(bundle.embeddings(), history, clusters, queryVector);
            scores = PersonalRanker.personalize(relevance, bundle.embeddings(), profile, params.beta());
        }
        Set<Integer> exclude = new HashSet<>(history);
        if (queryIndex >= 0) {
            exclude.add(queryIndex);
        }
        int[] candidates = HybridRetriever.candidates(scores, exclude, params.candidatePool());
        int[] top = PersonalRanker.mmr(candidates, scores, bundle.embeddings(), params.mmrLambda(), params.results());
        List<Ranked> items = new ArrayList<>(top.length);
        for (int index : top) {
            items.add(new Ranked(index, scores[index]));
        }
        double[] you = history.isEmpty() ? null : mapPosition(PersonalRanker.youVector(bundle.typeEmbeddings(), history));
        return new Result(personal ? Mode.PERSONALIZED_HISTORY : Mode.GENERIC_SEMANTIC,
                personal ? HistoryState.APPLIED : HistoryState.COLD_START, List.copyOf(items), you);
    }

    public double[] catalogMapPosition(int index) {
        return mapPosition(bundle.typeEmbeddings().row(index));
    }

    public double[] mapPosition(float[] typeVector) {
        return PersonalRanker.project(bundle.pcaMean(), bundle.pcaComponents(), typeVector);
    }

    public int clusterOf(int index) {
        return clusters[index];
    }

    public RuntimeBundle bundle() {
        return bundle;
    }
}
