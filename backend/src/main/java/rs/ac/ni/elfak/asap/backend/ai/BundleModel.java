package rs.ac.ni.elfak.asap.backend.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/** JSON shapes of the runtime bundle exported by {@code ml/export_bundle.py} (D-034). */
public final class BundleModel {

    private BundleModel() {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Manifest(
            String version,
            int products,
            int dim,
            String attribution,
            ModelSpec model,
            Params params,
            Map<String, List<Integer>> matrices,
            Map<String, FileEntry> files) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ModelSpec(String id, String revision, String prefix, int maxLength) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Params(
            double alpha,
            double beta,
            @JsonProperty("mmr_lambda") double mmrLambda,
            String profile,
            @JsonProperty("half_life") Double halfLife,
            @JsonProperty("history_window") int historyWindow,
            @JsonProperty("min_distinct_history") int minDistinctHistory,
            @JsonProperty("candidate_pool") int candidatePool,
            int results,
            int k,
            @JsonProperty("variant_pool") int variantPool) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FileEntry(long bytes, String sha256) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CatalogEntry(
            int index,
            String id,
            BarcodeEntry barcode,
            String name,
            String brand,
            String category,
            String description,
            List<String> tags,
            ProvenanceEntry provenance,
            String fullText,
            String typeText,
            int cluster,
            String variantKey) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record BarcodeEntry(String value, String format) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ProvenanceEntry(String type, String source) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TfidfSpec(List<String> vocabulary, double[] idf) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MapPoint(String id, double x, double y, int cluster) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Theme(
            int cluster,
            int size,
            String label,
            @JsonProperty("top_category") String topCategory,
            List<String> terms,
            double x,
            double y) {
    }
}
