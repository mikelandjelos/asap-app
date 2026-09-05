package rs.ac.ni.elfak.asap.backend.api;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Optional;
import rs.ac.ni.elfak.asap.backend.barcode.BarcodeRules;

public class BarcodeInputValidator
        implements ConstraintValidator<ValidBarcodeInput, ApiContract.BarcodeInput> {

    @Override
    public boolean isValid(ApiContract.BarcodeInput input, ConstraintValidatorContext context) {
        if (input == null) {
            return true;
        }
        Optional<String> error = BarcodeRules.validationCode(input.value(), input.format());
        if (error.isEmpty()) {
            return true;
        }

        String field = "format";
        if (!"UNSUPPORTED_FORMAT".equals(error.get())
                && !("REQUIRED".equals(error.get())
                        && (input.format() == null || input.format().isBlank()))) {
            field = "value";
        }
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(error.get())
                .addPropertyNode(field)
                .addConstraintViolation();
        return false;
    }
}
