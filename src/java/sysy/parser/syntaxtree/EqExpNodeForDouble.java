package sysy.parser.syntaxtree;

import sysy.lexer.LexType;
import sysy.lexer.Token;

import java.util.function.Consumer;

public class EqExpNodeForDouble extends EqExpNode {
    public EqExpNode eqExp;
    public LexType op;
    public RelExpNode relExp;

    @Override
    public void walk(Consumer<Token> terminalConsumer, Consumer<SyntaxNode> nonTerminalConsumer) {
        eqExp.walk(terminalConsumer, nonTerminalConsumer);

        terminalConsumer.accept(new Token(null, op, -1));

        relExp.walk(terminalConsumer, nonTerminalConsumer);

        nonTerminalConsumer.accept(this);
    }
}
