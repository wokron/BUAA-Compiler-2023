package sysy.exception;

public class LexerException extends CompilerException {
    public LexerException() {
        super();
    }

    public LexerException(String message) {
        super(message);
    }

    public LexerException(String message, Throwable cause) {
        super(message, cause);
    }

    public LexerException(Throwable cause) {
        super(cause);
    }
}
