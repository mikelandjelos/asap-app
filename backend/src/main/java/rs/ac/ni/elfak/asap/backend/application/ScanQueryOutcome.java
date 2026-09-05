package rs.ac.ni.elfak.asap.backend.application;

import rs.ac.ni.elfak.asap.backend.product.ProductOutcome;
import rs.ac.ni.elfak.asap.backend.recommendation.RecommendationOutcome;

public record ScanQueryOutcome(
        ProductOutcome product,
        RecommendationOutcome recommendations) {
}
