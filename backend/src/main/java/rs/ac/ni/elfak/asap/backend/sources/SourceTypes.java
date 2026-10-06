package rs.ac.ni.elfak.asap.backend.sources;

import java.time.Duration;
import java.util.List;
import rs.ac.ni.elfak.asap.backend.barcode.Barcode;

/** Shared types of the product source router (AI_MVP_DESIGN §2, D-029). */
public final class SourceTypes {

    private SourceTypes() {
    }

    /** Minimal HTTP seam so adapters are testable without the network. */
    public interface HttpFetcher {
        Response get(String url, Duration timeout) throws java.io.IOException, InterruptedException;
    }

    public record Response(int status, String contentType, String body) {
    }

    /** One external or local product source. Adapters never invent fields. */
    public interface ProductSource {
        String id();

        /** Whether normalized results may be cached (licence/terms, D-029). */
        boolean cacheable();

        Lookup lookup(Barcode barcode, Duration timeout);
    }

    /** Partial normalized record from one source; blank fields are absent ({@code null}). */
    public record SourceRecord(
            String source,
            String name,
            String brand,
            String category,
            List<String> categoryTags,
            String labels,
            String description) {

        public SourceRecord {
            categoryTags = categoryTags == null ? List.of() : List.copyOf(categoryTags);
        }
    }

    public sealed interface Lookup permits Found, NotFound, Unavailable {
    }

    public record Found(SourceRecord record) implements Lookup {
    }

    public record NotFound() implements Lookup {
    }

    /** Transport failure, 5xx, unexpected body, rate limit, quota or open circuit; never treated as "unknown". */
    public record Unavailable(String reason) implements Lookup {
    }

    static String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = java.text.Normalizer.normalize(value.strip(), java.text.Normalizer.Form.NFC);
        return trimmed.isEmpty() ? null : trimmed;
    }

    /** UPC-E (8 digits, number system 0/1) to UPC-A, keeping the check digit. */
    public static String upcEToUpcA(String upcE) {
        char ns = upcE.charAt(0);
        String d = upcE.substring(1, 7);
        char check = upcE.charAt(7);
        String body = switch (d.charAt(5)) {
            case '0', '1', '2' -> d.substring(0, 2) + d.charAt(5) + "0000" + d.substring(2, 5);
            case '3' -> d.substring(0, 3) + "00000" + d.substring(3, 5);
            case '4' -> d.substring(0, 4) + "00000" + d.charAt(4);
            default -> d.substring(0, 5) + "0000" + d.charAt(5);
        };
        return ns + body + check;
    }

    /** In-store / restricted-circulation codes are never sent to external providers (I1/T-009 rule). */
    public static boolean restricted(Barcode barcode) {
        String v = barcode.value();
        return switch (barcode.format()) {
            case EAN_13 -> v.charAt(0) == '2' || v.startsWith("02") || v.startsWith("04");
            case UPC_A -> v.charAt(0) == '2' || v.charAt(0) == '4';
            case EAN_8 -> v.charAt(0) == '0' || v.charAt(0) == '2';
            case UPC_E -> false;
        };
    }
}
