package rs.ac.ni.elfak.asap.backend.recommendation;

import java.util.List;
import org.springframework.stereotype.Service;
import rs.ac.ni.elfak.asap.backend.fixture.FixtureStore;
import rs.ac.ni.elfak.asap.backend.product.Product;

@Service
public class FixtureRecommendationProvider implements RecommendationProvider {

    private final FixtureStore fixtureStore;

    public FixtureRecommendationProvider(FixtureStore fixtureStore) {
        this.fixtureStore = fixtureStore;
    }

    @Override
    public RecommendationOutcome recommendationsFor(Product product) {
        FixtureStore.RecommendationBehavior behavior = fixtureStore.recommendationBehavior(product.id());
        return switch (behavior.status()) {
            case RESULTS -> RecommendationOutcome.results(resolveProducts(behavior.productIds()));
            case EMPTY -> RecommendationOutcome.empty();
            case UNAVAILABLE -> RecommendationOutcome.unavailable();
        };
    }

    private List<Product> resolveProducts(List<String> productIds) {
        return productIds.stream()
                .map(productId -> fixtureStore.findById(productId)
                        .orElseThrow(() -> new IllegalStateException("Missing fixture product")))
                .toList();
    }
}
