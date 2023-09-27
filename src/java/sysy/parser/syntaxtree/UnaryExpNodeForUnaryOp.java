package sysy.parser.syntaxtree;

import sysy.lexer.Token;

import java.util.function.Consumer;

public class UnaryExpNodeForUnaryOp extends UnaryExpNode {
    public UnaryOpNode op;
    public UnaryExpNode exp;

    @Override
    public void walk(Consumer<Token> terminalConsumer, Consumer<SyntaxNode> nonTerminalConsumer) {
        op.walk(terminalConsumer, nonTerminalConsumer);

        exp.walk(terminalConsumer, nonTerminalConsumer);

        nonTerminalConsumer.accept(this);
    }
}
