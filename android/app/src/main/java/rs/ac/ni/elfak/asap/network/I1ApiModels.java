package rs.ac.ni.elfak.asap.network;

import java.util.List;

public final class I1ApiModels {

    private I1ApiModels() {
    }

    public static final class ScanQueryRequest {
        public BarcodeData barcode;

        public ScanQueryRequest(String value, String format) {
            this.barcode = new BarcodeData(value, format);
        }
    }

    public static final class ScanQueryResponse {
        public ProductOutcome product;
        public RecommendationOutcome recommendations;
    }

    public static final class BarcodeData {
        public String value;
        public String format;

        public BarcodeData() {
        }

        public BarcodeData(String value, String format) {
            this.value = value;
            this.format = format;
        }
    }

    public static final class ProductOutcome {
        public String status;
        public ProductData data;
    }

    public static final class ProductData {
        public String id;
        public BarcodeData barcode;
        public String name;
        public String brand;
        public String category;
        public String description;
        public List<String> tags;
        public Provenance provenance;
    }

    public static final class Provenance {
        public String type;
        public String source;
    }

    public static final class RecommendationOutcome {
        public String status;
        public String mode;
        public boolean placeholder;
        public List<RecommendationItem> items;
    }

    public static final class RecommendationItem {
        public int rank;
        public ProductSummary product;
    }

    public static final class ProductSummary {
        public String id;
        public String name;
        public String brand;
        public String category;
    }
}
