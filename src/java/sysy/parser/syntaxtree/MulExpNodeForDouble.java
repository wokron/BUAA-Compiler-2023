package sysy.parser.syntaxtree;

import sysy.lexer.LexType;
import sysy.lexer.Token;

import java.util.function.Consumer;

public class MulExpNodeForDouble extends MulExpNode {
    public MulExpNode mulExp;
    public LexType op;
    public UnaryExpNode unaryExp;

    @Override
    public void walk(Consumer<Token> terminalConsumer, Consumer<SyntaxNode> nonTerminalConsumer) {
        mulExp.walk(terminalConsumer, nonTerminalConsumer);

        terminalConsumer.accept(new Token(null, op, -1));

        unaryExp.walk(terminalConsumer, nonTerminalConsumer);

        nonTerminalConsumer.accept(this);
    }
}
