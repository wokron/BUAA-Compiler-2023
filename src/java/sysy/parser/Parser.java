package sysy.parser;

import sysy.lexer.Lexer;

public class Parser {
    private final Lexer lexer;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
    }

    public SyntaxNode parse() {
        throw new RuntimeException();
    }
}
