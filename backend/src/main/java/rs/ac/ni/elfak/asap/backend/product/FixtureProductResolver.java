package rs.ac.ni.elfak.asap.backend.product;

import org.springframework.stereotype.Service;
import rs.ac.ni.elfak.asap.backend.barcode.Barcode;
import rs.ac.ni.elfak.asap.backend.fixture.FixtureStore;

@Service
public class FixtureProductResolver implements ProductResolver {

    private final FixtureStore fixtureStore;

    public FixtureProductResolver(FixtureStore fixtureStore) {
        this.fixtureStore = fixtureStore;
    }

    @Override
    public ProductOutcome resolve(Barcode barcode) {
        if (fixtureStore.isUnavailable(barcode)) {
            return ProductOutcome.unavailable();
        }
        return fixtureStore.findByBarcode(barcode)
                .map(ProductOutcome::known)
                .orElseGet(ProductOutcome::unknown);
    }
}
