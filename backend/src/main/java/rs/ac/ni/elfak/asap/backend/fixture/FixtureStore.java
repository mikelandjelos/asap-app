package rs.ac.ni.elfak.asap.backend.fixture;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import rs.ac.ni.elfak.asap.backend.barcode.Barcode;
import rs.ac.ni.elfak.asap.backend.barcode.BarcodeRules;
import rs.ac.ni.elfak.asap.backend.product.Product;
import tools.jackson.databind.ObjectMapper;

@Component
public class FixtureStore {

    private static final String RESOURCE = "fixtures/i1-products.json";

    private final ObjectMapper objectMapper;
    private Map<Barcode, Product> productsByBarcode;
    private Map<String, Product> productsById;
    private Map<String, RecommendationBehavior> behaviorsByProductId;
    private Set<Barcode> unavailableBarcodes;
    private String fixtureSetName;

    public FixtureStore(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    void load() throws IOException {
        try (InputStream input = new ClassPathResource(RESOURCE).getInputStream()) {
            FixtureSet fixtures = objectMapper.readValue(input, FixtureSet.class);
            validateAndIndex(fixtures);
        }
    }

    public Optional<Product> findByBarcode(Barcode barcode) {
        return Optional.ofNullable(productsByBarcode.get(barcode));
    }

    public Optional<Product> findById(String productId) {
        return Optional.ofNullable(productsById.get(productId));
    }

    public RecommendationBehavior recommendationBehavior(String productId) {
        RecommendationBehavior behavior = behaviorsByProductId.get(productId);
        if (behavior == null) {
            throw new IllegalStateException("Missing fixture recommendation behavior");
        }
        return behavior;
    }

    public boolean isUnavailable(Barcode barcode) {
        return unavailableBarcodes.contains(barcode);
    }

    public String fixtureSetName() {
        return fixtureSetName;
    }

    private void validateAndIndex(FixtureSet fixtures) {
        if (fixtures.schemaVersion() != 1
                || !"asap-i1-fixtures-v1".equals(fixtures.fixtureSet())
                || !"LOCAL_TEST_AND_DEMO_ONLY".equals(fixtures.restriction())) {
            throw new IllegalStateException("Unsupported I1 fixture metadata");
        }

        Map<Barcode, Product> barcodeIndex = new HashMap<>();
        Map<String, Product> idIndex = new HashMap<>();
        Map<String, RecommendationBehavior> behaviorIndex = new HashMap<>();

        for (FixtureProduct fixture : fixtures.products()) {
            Barcode barcode = toValidatedBarcode(fixture.barcode());
            Product product = new Product(fixture.id(), barcode, fixture.name(), fixture.brand(),
                    fixture.category(), fixture.description(), fixture.tags());
            if (barcodeIndex.put(barcode, product) != null || idIndex.put(product.id(), product) != null) {
                throw new IllegalStateException("Duplicate product fixture identifier");
            }
            behaviorIndex.put(product.id(), fixture.recommendationBehavior());
        }

        for (RecommendationBehavior behavior : behaviorIndex.values()) {
            if (behavior.status() == RecommendationStatus.RESULTS && behavior.productIds().isEmpty()) {
                throw new IllegalStateException("RESULTS fixture requires candidates");
            }
            if (behavior.status() != RecommendationStatus.RESULTS && !behavior.productIds().isEmpty()) {
                throw new IllegalStateException("Non-result fixture cannot contain candidates");
            }
            for (String productId : behavior.productIds()) {
                if (!idIndex.containsKey(productId)) {
                    throw new IllegalStateException("Unknown recommendation fixture reference");
                }
            }
        }

        Set<Barcode> unavailable = new HashSet<>();
        for (FixtureBarcode fixture : fixtures.productUnavailableBarcodes()) {
            Barcode barcode = toValidatedBarcode(fixture);
            if (barcodeIndex.containsKey(barcode) || !unavailable.add(barcode)) {
                throw new IllegalStateException("Conflicting unavailable fixture barcode");
            }
        }
        for (FixtureBarcode fixture : fixtures.documentedUnknownBarcodes()) {
            Barcode barcode = toValidatedBarcode(fixture);
            if (barcodeIndex.containsKey(barcode) || unavailable.contains(barcode)) {
                throw new IllegalStateException("Conflicting unknown fixture barcode");
            }
        }

        this.productsByBarcode = Map.copyOf(barcodeIndex);
        this.productsById = Map.copyOf(idIndex);
        this.behaviorsByProductId = Map.copyOf(behaviorIndex);
        this.unavailableBarcodes = Set.copyOf(unavailable);
        this.fixtureSetName = fixtures.fixtureSet();
    }

    private static Barcode toValidatedBarcode(FixtureBarcode fixture) {
        return BarcodeRules.validatedBarcode(fixture.value(), fixture.format());
    }

    private record FixtureSet(
            int schemaVersion,
            String fixtureSet,
            String restriction,
            List<FixtureProduct> products,
            List<FixtureBarcode> productUnavailableBarcodes,
            List<FixtureBarcode> documentedUnknownBarcodes) {
    }

    private record FixtureProduct(
            String id,
            FixtureBarcode barcode,
            String name,
            String brand,
            String category,
            String description,
            List<String> tags,
            RecommendationBehavior recommendationBehavior) {
    }

    private record FixtureBarcode(String value, String format) {
    }

    public record RecommendationBehavior(RecommendationStatus status, List<String> productIds) {
        public RecommendationBehavior {
            productIds = List.copyOf(productIds);
        }
    }

    public enum RecommendationStatus {
        RESULTS,
        EMPTY,
        UNAVAILABLE
    }
}
