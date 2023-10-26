package sysy.frontend.parser.syntaxtree.symbol;

import sysy.frontend.lexer.LexType;

public class TerminalSymbol {
    String value;
    LexType type;

    public TerminalSymbol(LexType type) {
        this.type = type;
        this.value = type.toString();
    }

    public TerminalSymbol(LexType type, String value) {
        this.type = type;
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public LexType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "%s %s".formatted(type.name(), value);
    }
}
