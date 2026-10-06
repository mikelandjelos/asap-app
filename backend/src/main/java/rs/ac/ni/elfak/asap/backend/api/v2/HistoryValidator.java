package rs.ac.ni.elfak.asap.backend.api.v2;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import rs.ac.ni.elfak.asap.backend.api.ApiContract.FieldError;
import rs.ac.ni.elfak.asap.backend.api.InvalidRequestException;
import rs.ac.ni.elfak.asap.backend.api.v2.V2Contract.HistoryEvent;

/**
 * Validates the optional newest-first {@code PRODUCT_VIEWED} history context (DOMAIN_MODEL.md). Malformed context is
 * rejected, never silently reordered: events must be ordered by descending {@code occurredAt}, ties by ascending id.
 */
final class HistoryValidator {

    static final int MAX_EVENTS = 50;
    private static final int MAX_ID_LENGTH = 128;

    private HistoryValidator() {
    }

    static List<String> validatedProductIds(List<HistoryEvent> history) {
        if (history == null || history.isEmpty()) {
            return List.of();
        }
        if (history.size() > MAX_EVENTS) {
            throw new InvalidRequestException(List.of(new FieldError("history", "HISTORY_TOO_LONG")));
        }
        List<FieldError> errors = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        Instant[] times = new Instant[history.size()];
        for (int i = 0; i < history.size(); i++) {
            HistoryEvent e = history.get(i);
            String field = "history[" + i + "]";
            if (e == null) {
                errors.add(new FieldError(field, "REQUIRED"));
                continue;
            }
            if (!validId(e.id())) {
                errors.add(new FieldError(field + ".id", "INVALID_EVENT_ID"));
            } else if (!ids.add(e.id())) {
                errors.add(new FieldError(field + ".id", "DUPLICATE_EVENT_ID"));
            }
            if (!validId(e.productId())) {
                errors.add(new FieldError(field + ".productId", "INVALID_PRODUCT_ID"));
            }
            if (!"PRODUCT_VIEWED".equals(e.kind())) {
                errors.add(new FieldError(field + ".kind", "UNSUPPORTED_KIND"));
            }
            try {
                times[i] = e.occurredAt() == null ? null : Instant.parse(e.occurredAt());
            } catch (DateTimeParseException ex) {
                times[i] = null;
            }
            if (times[i] == null) {
                errors.add(new FieldError(field + ".occurredAt", "INVALID_TIMESTAMP"));
            }
        }
        if (errors.isEmpty()) {
            for (int i = 1; i < history.size(); i++) {
                int cmp = times[i - 1].compareTo(times[i]);
                if (cmp < 0 || (cmp == 0 && history.get(i - 1).id().compareTo(history.get(i).id()) > 0)) {
                    errors.add(new FieldError("history", "OUT_OF_ORDER"));
                    break;
                }
            }
        }
        if (!errors.isEmpty()) {
            throw new InvalidRequestException(errors);
        }
        return history.stream().map(HistoryEvent::productId).toList();
    }

    private static boolean validId(String id) {
        return id != null && !id.isBlank() && id.length() <= MAX_ID_LENGTH;
    }
}
