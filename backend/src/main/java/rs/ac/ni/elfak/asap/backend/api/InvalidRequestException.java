package rs.ac.ni.elfak.asap.backend.api;

import java.util.List;
import rs.ac.ni.elfak.asap.backend.api.ApiContract.FieldError;

/** Semantic request validation failure mapped to an RFC 9457 invalid-request problem. */
public class InvalidRequestException extends RuntimeException {

    private final transient List<FieldError> errors;

    public InvalidRequestException(List<FieldError> errors) {
        super("Invalid request");
        this.errors = List.copyOf(errors);
    }

    public List<FieldError> errors() {
        return errors;
    }
}
