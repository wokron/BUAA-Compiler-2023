package sysy.lexer;

public class Token {
    private final String value;
    private final LexType type;

    public Token(String value, LexType type) {
        this.value = value;
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public LexType getType() {
        return type;
    }
}
