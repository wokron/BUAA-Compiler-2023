package sysy.exception;

public class CompilerException extends Exception {
    public CompilerException() {
        super();
    }

    public CompilerException(String message) {
        super(message);
    }

    public CompilerException(String message, Throwable cause) {
        super(message, cause);
    }

    public CompilerException(Throwable cause) {
        super(cause);
    }
}
