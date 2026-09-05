package rs.ac.ni.elfak.asap.backend.application;

import org.springframework.stereotype.Service;
import rs.ac.ni.elfak.asap.backend.barcode.Barcode;
import rs.ac.ni.elfak.asap.backend.product.ProductOutcome;
import rs.ac.ni.elfak.asap.backend.product.ProductResolver;
import rs.ac.ni.elfak.asap.backend.recommendation.RecommendationOutcome;
import rs.ac.ni.elfak.asap.backend.recommendation.RecommendationProvider;

@Service
public class ScanQueryCoordinator {

    private final ProductResolver productResolver;
    private final RecommendationProvider recommendationProvider;

    public ScanQueryCoordinator(
            ProductResolver productResolver,
            RecommendationProvider recommendationProvider) {
        this.productResolver = productResolver;
        this.recommendationProvider = recommendationProvider;
    }

    public ScanQueryOutcome query(Barcode barcode) {
        ProductOutcome product = productResolver.resolve(barcode);
        RecommendationOutcome recommendations = product.status() == ProductOutcome.Status.KNOWN
                ? recommendationProvider.recommendationsFor(product.product())
                : RecommendationOutcome.notApplicable();
        return new ScanQueryOutcome(product, recommendations);
    }
}
