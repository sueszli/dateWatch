package at.ac.tuwien.sepm.groupphase.backend.domain.event.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Can be used for all situations where only the organizer of an event is allowed to perform an action but the actor is not.
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class NotEventOrganizerException extends RuntimeException {
}
