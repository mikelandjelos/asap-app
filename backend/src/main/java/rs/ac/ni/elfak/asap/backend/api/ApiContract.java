package rs.ac.ni.elfak.asap.backend.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public final class ApiContract {

    private ApiContract() {
    }

    public record ScanQueryRequest(
            @NotNull(message = "REQUIRED") @Valid BarcodeInput barcode) {
    }

    @ValidBarcodeInput
    public record BarcodeInput(String value, String format) {
    }

    public record ScanQueryResponse(
            ProductResponse product,
            RecommendationResponse recommendations) {
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
            Provenance provenance) {
    }

    public record BarcodeData(String value, String format) {
    }

    public record Provenance(String type, String source) {
    }

    public record RecommendationResponse(
            String status,
            String mode,
            boolean placeholder,
            List<RecommendationItem> items) {
    }

    public record RecommendationItem(int rank, ProductSummary product) {
    }

    public record ProductSummary(String id, String name, String brand, String category) {
    }

    public record ProblemResponse(
            String type,
            String title,
            int status,
            String detail,
            List<FieldError> errors) {
    }

    public record FieldError(String field, String code) {
    }
}
