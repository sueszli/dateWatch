package at.ac.tuwien.sepm.groupphase.backend.common.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;


/**
 * Contains handlers for common {@link java.lang.RuntimeException}s which might be thrown across all domains and layers
 * and require no further handling or wrapping.
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Handles validation errors by returning a body containing all errors that occurred during the validation of a dto
     * along with the fields they each belong to. The response has the status
     * {@link org.springframework.http.HttpStatus#UNPROCESSABLE_ENTITY}.
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException exception,
                                                                  HttpHeaders headers,
                                                                  HttpStatus status,
                                                                  WebRequest request) {
        log.error(exception.getMessage(), exception);

        Map<String, String> errors = new HashMap<>();
        for (var error : exception.getFieldErrors()) {
            String fieldName = error.getField();
            String errorMessage = errors.containsKey(fieldName) ? (errors.get(fieldName) + " ") : "";
            errorMessage += error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        }

        return ResponseEntity.unprocessableEntity().headers(headers).body(errors);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handle(Exception ex, HttpServletRequest request, HttpServletResponse response) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex);
    }
}
