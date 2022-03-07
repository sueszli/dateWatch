package at.ac.tuwien.sepm.groupphase.backend.common.validation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;


@Slf4j
@RequiredArgsConstructor
public class NotNullWhenAddingValidator implements ConstraintValidator<NotNullWhenAdding, Object> {

    private final HttpServletRequest request;


    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        log.trace("calling isValid() ...");
        return request == null || !(request.getMethod().equals(RequestMethod.POST.name()) && value == null);
    }
}