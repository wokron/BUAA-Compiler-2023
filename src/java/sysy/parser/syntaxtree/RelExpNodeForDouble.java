package sysy.parser.syntaxtree;

import sysy.lexer.LexType;
import sysy.lexer.Token;

import java.util.function.Consumer;

public class RelExpNodeForDouble extends RelExpNode {
    public RelExpNode relExp;
    public LexType op;
    public AddExpNode addExp;

    @Override
    public void walk(Consumer<Token> terminalConsumer, Consumer<SyntaxNode> nonTerminalConsumer) {
        relExp.walk(terminalConsumer, nonTerminalConsumer);

        terminalConsumer.accept(new Token(null, op, -1));

        addExp.walk(terminalConsumer, nonTerminalConsumer);

        nonTerminalConsumer.accept(this);
    }
}
