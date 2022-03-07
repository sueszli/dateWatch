package at.ac.tuwien.sepm.groupphase.backend.common.exception;

import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * A {@link java.lang.RuntimeException} that should be used when something (primarily an entity)
 * conflicts in DB (for example new email to be saved is already presented in DB).
 * The exception will automatically be caught by the framework
 * and the request will fail with {@link org.springframework.http.HttpStatus#NOT_FOUND}.
 */
@NoArgsConstructor
@ResponseStatus(HttpStatus.CONFLICT)
public class IllegalUserArgumentException extends RuntimeException {

    public IllegalUserArgumentException(String message) {
        super(message);
    }

    public IllegalUserArgumentException(String message, Throwable cause) {
        super(message, cause);
    }
}


