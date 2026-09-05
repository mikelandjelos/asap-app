package rs.ac.ni.elfak.asap.backend.recommendation;

import java.util.List;
import rs.ac.ni.elfak.asap.backend.product.Product;

public record RecommendationOutcome(Status status, List<Product> products) {

    public RecommendationOutcome {
        products = List.copyOf(products);
    }

    public enum Status {
        RESULTS,
        EMPTY,
        UNAVAILABLE,
        NOT_APPLICABLE
    }

    public static RecommendationOutcome results(List<Product> products) {
        return new RecommendationOutcome(Status.RESULTS, products);
    }

    public static RecommendationOutcome empty() {
        return new RecommendationOutcome(Status.EMPTY, List.of());
    }

    public static RecommendationOutcome unavailable() {
        return new RecommendationOutcome(Status.UNAVAILABLE, List.of());
    }

    public static RecommendationOutcome notApplicable() {
        return new RecommendationOutcome(Status.NOT_APPLICABLE, List.of());
    }
}
