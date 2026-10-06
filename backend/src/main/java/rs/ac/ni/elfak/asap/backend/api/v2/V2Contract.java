package rs.ac.ni.elfak.asap.backend.api.v2;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import rs.ac.ni.elfak.asap.backend.api.ApiContract.BarcodeData;
import rs.ac.ni.elfak.asap.backend.api.ApiContract.BarcodeInput;
import rs.ac.ni.elfak.asap.backend.api.ApiContract.Provenance;

/** HTTP/JSON shapes of {@code /api/v2}; see {@code docs/V2_CONTRACT.md}. */
public final class V2Contract {

    private V2Contract() {
    }

    public record ScanQueryRequest(
            @NotNull(message = "REQUIRED") @Valid BarcodeInput barcode,
            List<HistoryEvent> history) {
    }

    public record HistoryEvent(String id, String productId, String kind, String occurredAt) {
    }

    public record ScanQueryResponse(
            ProductResponse product,
            RecommendationResponse recommendations,
            You you,
            String attribution) {
    }

    public record ProductResponse(String status, ProductData data) {
    }

    public record ProductData(
            String id,
            BarcodeData barcode,
            String name,
            String brand,
            String category,
            String description,
            List<String> tags,
            Provenance provenance,
            ThemeRef theme,
            MapPoint mapPosition) {
    }

    public record ThemeRef(int id, String label) {
    }

    public record MapPoint(double x, double y) {
    }

    public record RecommendationResponse(
            String status,
            String mode,
            String historyState,
            String pipelineVersion,
            Diversification diversification,
            List<RecommendationItem> items) {
    }

    public record Diversification(String method, double lambda) {
    }

    public record RecommendationItem(int rank, ProductSummary product, ThemeRef theme, Evidence evidence) {
    }

    public record ProductSummary(String id, BarcodeData barcode, String name, String brand, String category) {
    }

    public record Evidence(double score, String scoreType, String modelVersion) {
    }

    public record You(int historyUsed, MapPoint mapPosition) {
    }

    public record CatalogMapResponse(
            String pipelineVersion,
            String attribution,
            List<ThemePoint> themes,
            List<ProductPoint> points) {
    }

    public record ThemePoint(int id, String label, int size, double x, double y) {
    }

    public record ProductPoint(String id, int theme, double x, double y) {
    }
}
