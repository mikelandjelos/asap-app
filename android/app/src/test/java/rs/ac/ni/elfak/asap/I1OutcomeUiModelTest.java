package rs.ac.ni.elfak.asap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import org.junit.Test;
import rs.ac.ni.elfak.asap.network.I1ApiModels;

public final class I1OutcomeUiModelTest {

    @Test
    public void knownProductAndResultsRemainIndependentDisplayData() {
        I1ApiModels.ScanQueryResponse response = knownResponse("RESULTS");
        response.recommendations.items.add(item(1, "ASAP almond drink"));

        I1OutcomeUiModel model = I1OutcomeUiModel.fromValidatedResponse(response);

        assertEquals(I1OutcomeUiModel.ProductState.KNOWN, model.productState);
        assertSame(response.product.data, model.product);
        assertEquals(I1OutcomeUiModel.RecommendationState.RESULTS, model.recommendationState);
        assertTrue(model.placeholder);
        assertEquals("ASAP almond drink", model.items.get(0).product.name);
    }

    @Test
    public void emptyRecommendationsPreserveKnownProduct() {
        I1OutcomeUiModel model = I1OutcomeUiModel.fromValidatedResponse(knownResponse("EMPTY"));

        assertEquals(I1OutcomeUiModel.ProductState.KNOWN, model.productState);
        assertEquals("ASAP oat drink", model.product.name);
        assertEquals(I1OutcomeUiModel.RecommendationState.EMPTY, model.recommendationState);
        assertTrue(model.placeholder);
        assertTrue(model.items.isEmpty());
    }

    @Test
    public void unavailableRecommendationsPreserveKnownProduct() {
        I1OutcomeUiModel model =
                I1OutcomeUiModel.fromValidatedResponse(knownResponse("UNAVAILABLE"));

        assertEquals(I1OutcomeUiModel.ProductState.KNOWN, model.productState);
        assertEquals("ASAP oat drink", model.product.name);
        assertEquals(
                I1OutcomeUiModel.RecommendationState.UNAVAILABLE,
                model.recommendationState);
        assertTrue(model.placeholder);
    }

    @Test
    public void unknownProductHasNotApplicableResults() {
        I1OutcomeUiModel model = I1OutcomeUiModel.fromValidatedResponse(
                responseWithoutProduct("UNKNOWN"));

        assertEquals(I1OutcomeUiModel.ProductState.UNKNOWN, model.productState);
        assertNull(model.product);
        assertEquals(
                I1OutcomeUiModel.RecommendationState.NOT_APPLICABLE,
                model.recommendationState);
        assertFalse(model.placeholder);
        assertTrue(model.items.isEmpty());
    }

    @Test
    public void unavailableProductHasNotApplicableResults() {
        I1OutcomeUiModel model = I1OutcomeUiModel.fromValidatedResponse(
                responseWithoutProduct("UNAVAILABLE"));

        assertEquals(I1OutcomeUiModel.ProductState.UNAVAILABLE, model.productState);
        assertNull(model.product);
        assertEquals(
                I1OutcomeUiModel.RecommendationState.NOT_APPLICABLE,
                model.recommendationState);
        assertFalse(model.placeholder);
    }

    private static I1ApiModels.ScanQueryResponse knownResponse(String recommendationStatus) {
        I1ApiModels.ScanQueryResponse response = new I1ApiModels.ScanQueryResponse();
        response.product = new I1ApiModels.ProductOutcome();
        response.product.status = "KNOWN";
        response.product.data = new I1ApiModels.ProductData();
        response.product.data.name = "ASAP oat drink";
        response.recommendations = recommendations(recommendationStatus, true);
        return response;
    }

    private static I1ApiModels.ScanQueryResponse responseWithoutProduct(String productStatus) {
        I1ApiModels.ScanQueryResponse response = new I1ApiModels.ScanQueryResponse();
        response.product = new I1ApiModels.ProductOutcome();
        response.product.status = productStatus;
        response.recommendations = recommendations("NOT_APPLICABLE", false);
        return response;
    }

    private static I1ApiModels.RecommendationOutcome recommendations(
            String status, boolean placeholder) {
        I1ApiModels.RecommendationOutcome recommendations =
                new I1ApiModels.RecommendationOutcome();
        recommendations.status = status;
        recommendations.placeholder = placeholder;
        recommendations.items = new ArrayList<>();
        return recommendations;
    }

    private static I1ApiModels.RecommendationItem item(int rank, String name) {
        I1ApiModels.RecommendationItem item = new I1ApiModels.RecommendationItem();
        item.rank = rank;
        item.product = new I1ApiModels.ProductSummary();
        item.product.name = name;
        return item;
    }
}
