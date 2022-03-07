package at.ac.tuwien.sepm.groupphase.backend.common.exception;

import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


/**
 * A {@link java.lang.RuntimeException} that should be used when
 * user has no rights to execute a request.
 * The exception will automatically be caught by the framework
 * and the request will fail with {@link org.springframework.http.HttpStatus#NOT_FOUND}.
 */
@NoArgsConstructor
@ResponseStatus(HttpStatus.FORBIDDEN)
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }

    public ForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }
}

