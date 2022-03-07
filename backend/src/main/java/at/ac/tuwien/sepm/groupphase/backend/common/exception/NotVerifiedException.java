package at.ac.tuwien.sepm.groupphase.backend.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * A {@link java.lang.RuntimeException} that should be used when someone does an action with an account which is not verified.
 * The exception will automatically be caught by the framework and the request will fail with {@link org.springframework.http.HttpStatus#UNAUTHORIZED}.
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class NotVerifiedException extends RuntimeException {
    public NotVerifiedException(String message) {
        super(message);
    }

    public NotVerifiedException(String message, Throwable cause) {
        super(message, cause);
    }
}
