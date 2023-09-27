package sysy.parser.syntaxtree;

import sysy.lexer.Token;

import java.util.function.Consumer;

public abstract class SyntaxNode {
    public abstract void walk(Consumer<Token> terminalConsumer, Consumer<SyntaxNode> nonTerminalConsumer);
}
