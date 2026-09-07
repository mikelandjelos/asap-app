package rs.ac.ni.elfak.asap.network;

import java.util.Collections;
import java.util.List;

final class I1ResponseValidator {

    private I1ResponseValidator() {
    }

    static boolean isUsable(I1ApiModels.ScanQueryResponse response) {
        if (response == null || response.product == null || response.recommendations == null) {
            return false;
        }

        String productStatus = response.product.status;
        if ("KNOWN".equals(productStatus)) {
            return validKnownProduct(response.product.data)
                    && validKnownRecommendations(response.recommendations);
        }
        if ("UNKNOWN".equals(productStatus) || "UNAVAILABLE".equals(productStatus)) {
            return response.product.data == null
                    && "NOT_APPLICABLE".equals(response.recommendations.status)
                    && response.recommendations.mode == null
                    && !response.recommendations.placeholder
                    && safeItems(response.recommendations.items).isEmpty();
        }
        return false;
    }

    private static boolean validKnownProduct(I1ApiModels.ProductData product) {
        return product != null
                && nonBlank(product.id)
                && product.barcode != null
                && nonBlank(product.barcode.value)
                && nonBlank(product.barcode.format)
                && nonBlank(product.name)
                && nonBlank(product.brand)
                && nonBlank(product.category)
                && nonBlank(product.description)
                && product.tags != null
                && product.provenance != null
                && "CONTROLLED_FIXTURE".equals(product.provenance.type)
                && nonBlank(product.provenance.source);
    }

    private static boolean validKnownRecommendations(
            I1ApiModels.RecommendationOutcome recommendations) {
        if (!"DETERMINISTIC_FIXTURE".equals(recommendations.mode)
                || !recommendations.placeholder
                || recommendations.items == null) {
            return false;
        }

        if ("RESULTS".equals(recommendations.status)) {
            if (recommendations.items.isEmpty()) {
                return false;
            }
            for (int index = 0; index < recommendations.items.size(); index++) {
                I1ApiModels.RecommendationItem item = recommendations.items.get(index);
                if (item == null || item.rank != index + 1 || !validSummary(item.product)) {
                    return false;
                }
            }
            return true;
        }
        return ("EMPTY".equals(recommendations.status)
                        || "UNAVAILABLE".equals(recommendations.status))
                && recommendations.items.isEmpty();
    }

    private static boolean validSummary(I1ApiModels.ProductSummary product) {
        return product != null
                && nonBlank(product.id)
                && nonBlank(product.name)
                && nonBlank(product.brand)
                && nonBlank(product.category);
    }

    private static List<I1ApiModels.RecommendationItem> safeItems(
            List<I1ApiModels.RecommendationItem> items) {
        return items == null ? Collections.emptyList() : items;
    }

    private static boolean nonBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
