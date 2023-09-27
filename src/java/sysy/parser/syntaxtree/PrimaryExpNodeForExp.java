package sysy.parser.syntaxtree;

import sysy.lexer.LexType;
import sysy.lexer.Token;

import java.util.function.Consumer;

public class PrimaryExpNodeForExp extends PrimaryExpNode {
    public ExpNode exp;

    @Override
    public void walk(Consumer<Token> terminalConsumer, Consumer<SyntaxNode> nonTerminalConsumer) {
        terminalConsumer.accept(new Token(null, LexType.LPARENT, -1));

        exp.walk(terminalConsumer, nonTerminalConsumer);

        terminalConsumer.accept(new Token(null, LexType.RPARENT, -1));

        nonTerminalConsumer.accept(this);
    }
}
