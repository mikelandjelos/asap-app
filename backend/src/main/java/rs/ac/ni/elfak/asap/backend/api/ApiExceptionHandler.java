package rs.ac.ni.elfak.asap.backend.api;

import java.util.Comparator;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import rs.ac.ni.elfak.asap.backend.api.ApiContract.FieldError;
import rs.ac.ni.elfak.asap.backend.api.ApiContract.ProblemResponse;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final String INVALID_REQUEST = "urn:asap:problem:invalid-request";
    private static final String INTERNAL_ERROR = "urn:asap:problem:internal-error";
    private static final String NOT_FOUND = "urn:asap:problem:not-found";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ProblemResponse> validationFailure(MethodArgumentNotValidException exception) {
        List<FieldError> errors = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> new FieldError(error.getField(), error.getDefaultMessage()))
                .sorted(Comparator.comparing(FieldError::field).thenComparing(FieldError::code))
                .toList();
        return problem(HttpStatus.BAD_REQUEST, "Invalid request",
                "The request contains invalid fields.", errors);
    }

    @ExceptionHandler(InvalidRequestException.class)
    ResponseEntity<ProblemResponse> invalidRequest(InvalidRequestException exception) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid request",
                "The request contains invalid fields.", exception.errors());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ProblemResponse> unreadable(HttpMessageNotReadableException exception) {
        boolean unknownField = hasCauseNamed(exception, "UnrecognizedPropertyException");
        String code = unknownField ? "UNKNOWN_FIELD" : "MALFORMED_JSON";
        return problem(HttpStatus.BAD_REQUEST, "Invalid request",
                "The request body is not valid.", List.of(new FieldError("$", code)));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    ResponseEntity<ProblemResponse> unsupportedMediaType() {
        return problem(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Unsupported media type",
                "Content-Type must be application/json.", List.of());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    ResponseEntity<ProblemResponse> notFound() {
        return problem(NOT_FOUND, HttpStatus.NOT_FOUND, "Not found",
                "No resource exists at this path.", List.of());
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ProblemResponse> unexpected() {
        return problem(INTERNAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error",
                "The request could not be completed.", List.of());
    }

    private static boolean hasCauseNamed(Throwable throwable, String simpleName) {
        Throwable current = throwable;
        while (current != null) {
            if (simpleName.equals(current.getClass().getSimpleName())) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private static ResponseEntity<ProblemResponse> problem(
            HttpStatus status,
            String title,
            String detail,
            List<FieldError> errors) {
        return problem(INVALID_REQUEST, status, title, detail, errors);
    }

    private static ResponseEntity<ProblemResponse> problem(
            String type,
            HttpStatus status,
            String title,
            String detail,
            List<FieldError> errors) {
        ProblemResponse body = new ProblemResponse(
                type, title, status.value(), detail, errors);
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(body);
    }
}
