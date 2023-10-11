package sysy.exception;

public class DuplicateIdentException extends Exception {
    public DuplicateIdentException() {
        super();
    }

    public DuplicateIdentException(String message) {
        super(message);
    }

    public DuplicateIdentException(String message, Throwable cause) {
        super(message, cause);
    }

    public DuplicateIdentException(Throwable cause) {
        super(cause);
    }
}
