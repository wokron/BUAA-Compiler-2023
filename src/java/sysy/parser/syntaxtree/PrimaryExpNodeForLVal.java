package sysy.parser.syntaxtree;

import sysy.lexer.Token;

import java.util.function.Consumer;

public class PrimaryExpNodeForLVal extends PrimaryExpNode {
    public LValNode lVal;

    @Override
    public void walk(Consumer<Token> terminalConsumer, Consumer<SyntaxNode> nonTerminalConsumer) {
        lVal.walk(terminalConsumer, nonTerminalConsumer);

        nonTerminalConsumer.accept(this);
    }
}
