package rs.ac.ni.elfak.asap.backend.api;

import java.util.List;
import org.springframework.stereotype.Component;
import rs.ac.ni.elfak.asap.backend.api.ApiContract.BarcodeData;
import rs.ac.ni.elfak.asap.backend.api.ApiContract.ProductData;
import rs.ac.ni.elfak.asap.backend.api.ApiContract.ProductResponse;
import rs.ac.ni.elfak.asap.backend.api.ApiContract.ProductSummary;
import rs.ac.ni.elfak.asap.backend.api.ApiContract.Provenance;
import rs.ac.ni.elfak.asap.backend.api.ApiContract.RecommendationItem;
import rs.ac.ni.elfak.asap.backend.api.ApiContract.RecommendationResponse;
import rs.ac.ni.elfak.asap.backend.api.ApiContract.ScanQueryResponse;
import rs.ac.ni.elfak.asap.backend.application.ScanQueryOutcome;
import rs.ac.ni.elfak.asap.backend.fixture.FixtureStore;
import rs.ac.ni.elfak.asap.backend.product.Product;
import rs.ac.ni.elfak.asap.backend.product.ProductOutcome;
import rs.ac.ni.elfak.asap.backend.recommendation.RecommendationOutcome;

@Component
public class ScanQueryResponseMapper {

    private static final String FIXTURE_PROVENANCE = "CONTROLLED_FIXTURE";
    private static final String FIXTURE_MODE = "DETERMINISTIC_FIXTURE";

    private final FixtureStore fixtureStore;

    public ScanQueryResponseMapper(FixtureStore fixtureStore) {
        this.fixtureStore = fixtureStore;
    }

    public ScanQueryResponse map(ScanQueryOutcome outcome) {
        return new ScanQueryResponse(mapProduct(outcome.product()),
                mapRecommendations(outcome.recommendations()));
    }

    private ProductResponse mapProduct(ProductOutcome outcome) {
        ProductData data = outcome.status() == ProductOutcome.Status.KNOWN
                ? productData(outcome.product())
                : null;
        return new ProductResponse(outcome.status().name(), data);
    }

    private RecommendationResponse mapRecommendations(RecommendationOutcome outcome) {
        boolean applicable = outcome.status() != RecommendationOutcome.Status.NOT_APPLICABLE;
        List<RecommendationItem> items = java.util.stream.IntStream
                .range(0, outcome.products().size())
                .mapToObj(index -> new RecommendationItem(index + 1,
                        productSummary(outcome.products().get(index))))
                .toList();
        return new RecommendationResponse(
                outcome.status().name(),
                applicable ? FIXTURE_MODE : null,
                applicable,
                items);
    }

    private ProductData productData(Product product) {
        return new ProductData(
                product.id(),
                new BarcodeData(product.barcode().value(), product.barcode().format().name()),
                product.name(),
                product.brand(),
                product.category(),
                product.description(),
                product.tags(),
                new Provenance(FIXTURE_PROVENANCE, fixtureStore.fixtureSetName()));
    }

    private ProductSummary productSummary(Product product) {
        return new ProductSummary(product.id(), product.name(), product.brand(), product.category());
    }
}
