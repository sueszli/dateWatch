package at.ac.tuwien.sepm.groupphase.backend.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


/** Should be used when an action can not be performed because a precondition was not met. */
@ResponseStatus(HttpStatus.PRECONDITION_REQUIRED)
public class PreconditionRequiredException extends RuntimeException {
    public PreconditionRequiredException(String message) {
        super(message);
    }
}
