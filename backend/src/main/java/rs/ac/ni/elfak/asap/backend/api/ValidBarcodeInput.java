package rs.ac.ni.elfak.asap.backend.api;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = BarcodeInputValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidBarcodeInput {

    String message() default "INVALID_BARCODE";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
