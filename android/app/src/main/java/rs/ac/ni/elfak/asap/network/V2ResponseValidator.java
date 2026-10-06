package rs.ac.ni.elfak.asap.network;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Rejects v2 responses the UI could misrepresent; mirrors the outcome rules of {@code docs/V2_CONTRACT.md}. */
final class V2ResponseValidator {

    private V2ResponseValidator() {
    }

    static boolean isUsable(V2ApiModels.ScanQueryResponse r) {
        if (r == null || r.product == null || r.recommendations == null || !nonBlank(r.attribution)) {
            return false;
        }
        if (r.you != null && (r.you.historyUsed < 1 || !finite(r.you.mapPosition))) {
            return false;
        }
        String status = r.product.status;
        if ("UNKNOWN".equals(status) || "UNAVAILABLE".equals(status)) {
            return r.product.data == null
                    && "NOT_APPLICABLE".equals(r.recommendations.status)
                    && r.recommendations.mode == null
                    && "NOT_USED".equals(r.recommendations.historyState)
                    && (r.recommendations.items == null || r.recommendations.items.isEmpty());
        }
        return "KNOWN".equals(status) && validProduct(r.product.data) && validRecommendations(r.recommendations, r.product.data.id);
    }

    private static boolean validProduct(V2ApiModels.ProductData p) {
        return p != null && nonBlank(p.id) && p.barcode != null && nonBlank(p.barcode.value) && nonBlank(p.barcode.format)
                && nonBlank(p.name) && p.provenance != null && nonBlank(p.provenance.type) && nonBlank(p.provenance.source)
                && p.theme != null && nonBlank(p.theme.label) && finite(p.mapPosition);
    }

    private static boolean validRecommendations(V2ApiModels.RecommendationOutcome rec, String productId) {
        boolean personalized = "PERSONALIZED_HISTORY".equals(rec.mode) && "APPLIED".equals(rec.historyState);
        boolean generic = "GENERIC_SEMANTIC".equals(rec.mode) && "COLD_START".equals(rec.historyState);
        if (!(personalized || generic) || !nonBlank(rec.pipelineVersion)) {
            return false;
        }
        List<V2ApiModels.Item> items = rec.items == null ? Collections.<V2ApiModels.Item>emptyList() : rec.items;
        if ("EMPTY".equals(rec.status)) {
            return items.isEmpty();
        }
        if (!"RESULTS".equals(rec.status) || items.isEmpty() || items.size() > 10) {
            return false;
        }
        Set<String> ids = new HashSet<>();
        for (int i = 0; i < items.size(); i++) {
            V2ApiModels.Item item = items.get(i);
            if (item == null || item.rank != i + 1 || item.product == null || !nonBlank(item.product.id)
                    || !nonBlank(item.product.name) || item.product.id.equals(productId) || !ids.add(item.product.id)
                    || item.theme == null || !nonBlank(item.theme.label) || item.evidence == null
                    || !Double.isFinite(item.evidence.score) || !nonBlank(item.evidence.scoreType)) {
                return false;
            }
        }
        return true;
    }

    static boolean isUsable(V2ApiModels.CatalogMap map) {
        if (map == null || !nonBlank(map.pipelineVersion) || map.themes == null || map.themes.isEmpty() || map.points == null) {
            return false;
        }
        for (V2ApiModels.ThemePoint t : map.themes) {
            if (t == null || !nonBlank(t.label) || t.size < 1 || !Double.isFinite(t.x) || !Double.isFinite(t.y)) {
                return false;
            }
        }
        return true;
    }

    private static boolean finite(V2ApiModels.MapPoint p) {
        return p != null && Double.isFinite(p.x) && Double.isFinite(p.y);
    }

    private static boolean nonBlank(String s) {
        return s != null && !s.trim().isEmpty();
    }
}
