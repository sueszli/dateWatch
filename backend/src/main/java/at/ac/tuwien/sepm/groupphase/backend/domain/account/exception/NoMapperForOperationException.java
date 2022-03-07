package at.ac.tuwien.sepm.groupphase.backend.domain.account.exception;

public class NoMapperForOperationException extends RuntimeException {
    public NoMapperForOperationException() {
    }

    public NoMapperForOperationException(String message) {
        super(message);
    }

    public NoMapperForOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoMapperForOperationException(Throwable cause) {
        super(cause);
    }
}
