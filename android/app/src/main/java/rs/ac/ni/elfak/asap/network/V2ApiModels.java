package rs.ac.ni.elfak.asap.network;

import java.util.List;
import java.util.Map;

/** Moshi shapes of the AI v2 API ({@code docs/V2_CONTRACT.md}). */
public final class V2ApiModels {

    private V2ApiModels() {
    }

    public static final class ScanQueryRequest {
        public I1ApiModels.BarcodeData barcode;
        public List<HistoryEvent> history;

        public ScanQueryRequest(String value, String format, List<HistoryEvent> history) {
            this.barcode = new I1ApiModels.BarcodeData(value, format);
            this.history = history == null || history.isEmpty() ? null : history;
        }
    }

    public static final class HistoryEvent {
        public String id;
        public String productId;
        public String kind;
        public String occurredAt;

        public HistoryEvent() {
        }

        public HistoryEvent(String id, String productId, String occurredAt) {
            this.id = id;
            this.productId = productId;
            this.kind = "PRODUCT_VIEWED";
            this.occurredAt = occurredAt;
        }
    }

    public static final class ScanQueryResponse {
        public ProductOutcome product;
        public RecommendationOutcome recommendations;
        public You you;
        public String attribution;
    }

    public static final class ProductOutcome {
        public String status;
        public ProductData data;
    }

    public static final class ProductData {
        public String id;
        public I1ApiModels.BarcodeData barcode;
        public String name;
        public String brand;
        public String category;
        public String description;
        public List<String> tags;
        public I1ApiModels.Provenance provenance;
        public Map<String, String> fieldSources;
        public ThemeRef theme;
        public MapPoint mapPosition;
    }

    public static final class ThemeRef {
        public int id;
        public String label;
    }

    public static final class MapPoint {
        public double x;
        public double y;
    }

    public static final class RecommendationOutcome {
        public String status;
        public String mode;
        public String historyState;
        public String pipelineVersion;
        public Diversification diversification;
        public List<Item> items;
    }

    public static final class Diversification {
        public String method;
        public double lambda;
    }

    public static final class Item {
        public int rank;
        public Summary product;
        public ThemeRef theme;
        public Evidence evidence;
    }

    public static final class Summary {
        public String id;
        public I1ApiModels.BarcodeData barcode;
        public String name;
        public String brand;
        public String category;
    }

    public static final class Evidence {
        public double score;
        public String scoreType;
        public String modelVersion;
    }

    public static final class You {
        public int historyUsed;
        public MapPoint mapPosition;
    }

    public static final class CatalogMap {
        public String pipelineVersion;
        public String attribution;
        public List<ThemePoint> themes;
        public List<ProductPoint> points;
    }

    public static final class ThemePoint {
        public int id;
        public String label;
        public int size;
        public double x;
        public double y;
    }

    public static final class ProductPoint {
        public String id;
        public int theme;
        public double x;
        public double y;
    }
}
