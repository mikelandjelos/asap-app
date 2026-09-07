package rs.ac.ni.elfak.asap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import rs.ac.ni.elfak.asap.network.I1ApiModels;

final class I1OutcomeUiModel {

    enum ProductState {
        KNOWN,
        UNKNOWN,
        UNAVAILABLE
    }

    enum RecommendationState {
        RESULTS,
        EMPTY,
        UNAVAILABLE,
        NOT_APPLICABLE
    }

    final ProductState productState;
    final I1ApiModels.ProductData product;
    final RecommendationState recommendationState;
    final boolean placeholder;
    final List<I1ApiModels.RecommendationItem> items;

    private I1OutcomeUiModel(
            ProductState productState,
            I1ApiModels.ProductData product,
            RecommendationState recommendationState,
            boolean placeholder,
            List<I1ApiModels.RecommendationItem> items) {
        this.productState = productState;
        this.product = product;
        this.recommendationState = recommendationState;
        this.placeholder = placeholder;
        this.items = items;
    }

    static I1OutcomeUiModel fromValidatedResponse(I1ApiModels.ScanQueryResponse response) {
        return new I1OutcomeUiModel(
                ProductState.valueOf(response.product.status),
                response.product.data,
                RecommendationState.valueOf(response.recommendations.status),
                response.recommendations.placeholder,
                response.recommendations.items == null
                        ? Collections.emptyList()
                        : Collections.unmodifiableList(
                                new ArrayList<>(response.recommendations.items)));
    }
}
