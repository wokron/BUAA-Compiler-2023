package sysy.parser.syntaxtree;

import sysy.lexer.LexType;
import sysy.lexer.Token;

import java.util.function.Consumer;

public class StmtNodeForReturn extends StmtNode {
    public ExpNode exp;

    @Override
    public void walk(Consumer<Token> terminalConsumer, Consumer<SyntaxNode> nonTerminalConsumer) {
        terminalConsumer.accept(new Token(null, LexType.RETURNTK, -1));

        if (exp != null) {
            exp.walk(terminalConsumer, nonTerminalConsumer);
        }

        terminalConsumer.accept(new Token(null, LexType.SEMICN, -1));

        nonTerminalConsumer.accept(this);
    }
}
