package at.ac.tuwien.sepm.groupphase.backend.common.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Validates that a string property is either null or not {@link String#isBlank() blank}.<br>
 * Must only be used on strings.<br>
 * See {@link NullOrNotBlankValidator} for the implementation.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { NullOrNotBlankValidator.class })
public @interface NullOrNotBlank {
    String message() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
