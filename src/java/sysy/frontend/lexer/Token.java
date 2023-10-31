package sysy.frontend.lexer;

public class Token {
    private final String value;
    private final LexType type;
    private final int lineNum;

    public Token(String value, LexType type, int lineNum) {
        this.value = value;
        this.type = type;
        this.lineNum = lineNum;
    }

    public String getValue() {
        return value;
    }

    public LexType getType() {
        return type;
    }

    public int getLineNum() {
        return lineNum;
    }
}
