package rs.ac.ni.elfak.asap.backend.recommendation;

import rs.ac.ni.elfak.asap.backend.product.Product;

public interface RecommendationProvider {

    RecommendationOutcome recommendationsFor(Product product);
}
