package rs.ac.ni.elfak.asap.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.squareup.moshi.Moshi;
import java.lang.reflect.Constructor;
import org.junit.Test;
import rs.ac.ni.elfak.asap.ScanSession;
import rs.ac.ni.elfak.asap.network.I1ApiModels;
import rs.ac.ni.elfak.asap.network.ScanQueryClient;
import rs.ac.ni.elfak.asap.network.V2ApiModels;
import rs.ac.ni.elfak.asap.network.V2ClientTest;

public class ProductUiModelTest {

    private static ScanSession.State state(ScanSession.Phase phase, String barcode, V2ApiModels.ScanQueryResponse r,
            ScanQueryClient.Failure f) throws Exception {
        Constructor<ScanSession.State> c = ScanSession.State.class.getDeclaredConstructor(
                ScanSession.Phase.class, String.class, V2ApiModels.ScanQueryResponse.class, ScanQueryClient.Failure.class);
        c.setAccessible(true);
        return c.newInstance(phase, barcode, r, f);
    }

    private static V2ApiModels.ScanQueryResponse parse(String json) throws Exception {
        return new Moshi.Builder().build().adapter(V2ApiModels.ScanQueryResponse.class).fromJson(json);
    }

    @Test
    public void knownProductMapsCardResultsAndPersonalHeader() throws Exception {
        ProductUiModel m = ProductUiModel.from(state(ScanSession.Phase.LOADED, "3017620422003", parse(V2ClientTest.KNOWN), null));
        assertEquals(ProductUiModel.Screen.PRODUCT, m.screen);
        assertEquals("Nutella", m.name);
        assertEquals("Ferrero · Spreads", m.details);
        assertEquals("Chocolate & hazelnut spreads", m.theme);
        assertEquals("ASAP catalog · Open Food Facts", m.source);
        assertEquals(ProductUiModel.ResultsKind.PERSONAL, m.resultsKind);
        assertEquals(5, m.historyUsed);
        assertEquals(1, m.rows.size());
        assertEquals("Cream", m.rows.get(0).title);
        assertNull(m.rows.get(0).subtitle); // no brand/category: not rendered as a blank line
        assertFalse(m.retry);
    }

    @Test
    public void coldStartAndEmptyResultsGetTheirOwnHeaders() throws Exception {
        String cold = V2ClientTest.KNOWN.replace("PERSONALIZED_HISTORY", "GENERIC_SEMANTIC").replace("\"APPLIED\"", "\"COLD_START\"");
        assertEquals(ProductUiModel.ResultsKind.SIMILAR,
                ProductUiModel.from(state(ScanSession.Phase.LOADED, "1", parse(cold), null)).resultsKind);
        V2ApiModels.ScanQueryResponse empty = parse(cold);
        empty.recommendations.items = null;
        assertEquals(ProductUiModel.ResultsKind.EMPTY,
                ProductUiModel.from(state(ScanSession.Phase.LOADED, "1", empty, null)).resultsKind);
    }

    @Test
    public void unknownUnavailableAndFailuresHaveDistinctScreensAndRetryRules() throws Exception {
        V2ApiModels.ScanQueryResponse r = parse(V2ClientTest.KNOWN);
        r.product.status = "UNKNOWN";
        ProductUiModel unknown = ProductUiModel.from(state(ScanSession.Phase.LOADED, "123", r, null));
        assertEquals(ProductUiModel.Screen.NOT_FOUND, unknown.screen);
        assertFalse(unknown.retry); // retrying cannot help a product nobody knows
        r.product.status = "UNAVAILABLE";
        assertTrue(ProductUiModel.from(state(ScanSession.Phase.LOADED, "123", r, null)).retry);
        assertEquals(ProductUiModel.Screen.OFFLINE,
                ProductUiModel.from(state(ScanSession.Phase.FAILED, "1", null, ScanQueryClient.Failure.transport())).screen);
        ProductUiModel http = ProductUiModel.from(state(ScanSession.Phase.FAILED, "1", null, ScanQueryClient.Failure.http(503)));
        assertEquals(ProductUiModel.Screen.SERVER_ERROR, http.screen);
        assertEquals(Integer.valueOf(503), http.httpStatus);
        assertEquals(ProductUiModel.Screen.LOADING,
                ProductUiModel.from(state(ScanSession.Phase.LOADING, "1", null, null)).screen);
        assertEquals(ProductUiModel.Screen.NONE, ProductUiModel.from(state(ScanSession.Phase.IDLE, null, null, null)).screen);
    }

    @Test
    public void liveProviderSourcesAreNamedAndMarkedLive() {
        I1ApiModels.Provenance p = new I1ApiModels.Provenance();
        p.type = "EXTERNAL_PROVIDER";
        p.source = "open_food_facts+upcitemdb";
        assertEquals("Open Food Facts + UPCitemdb (live lookup)", ProductUiModel.sourceLabel(p));
    }
}
