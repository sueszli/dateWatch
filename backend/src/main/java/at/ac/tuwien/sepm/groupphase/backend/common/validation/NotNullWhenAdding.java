package at.ac.tuwien.sepm.groupphase.backend.common.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Validates that a property is not null when the object that defines it is provided in a POST endpoint call
 * (e.g. to create an entity).
 * When provided in an endpoint of another method (like a PUT request to update an entity) the property may be null.<br>
 * See {@link NotNullWhenAddingValidator} for the implementation.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { NotNullWhenAddingValidator.class })
public @interface NotNullWhenAdding {
    String message() default "";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
