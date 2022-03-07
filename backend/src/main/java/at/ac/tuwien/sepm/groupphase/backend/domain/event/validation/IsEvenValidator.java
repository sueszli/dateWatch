package at.ac.tuwien.sepm.groupphase.backend.domain.event.validation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;


@Slf4j
@RequiredArgsConstructor
public class IsEvenValidator implements ConstraintValidator<IsEven, Object> {

    private final HttpServletRequest request;


    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        log.trace("calling isValid() ...");
        return request == null || ((int) value) % 2 == 0;
    }
}
