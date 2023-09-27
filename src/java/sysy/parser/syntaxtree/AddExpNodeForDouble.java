package sysy.parser.syntaxtree;

import sysy.lexer.LexType;
import sysy.lexer.Token;

import java.util.function.Consumer;

public class AddExpNodeForDouble extends AddExpNode {
    public AddExpNode addExp;
    public LexType op;
    public MulExpNode mulExp;

    @Override
    public void walk(Consumer<Token> terminalConsumer, Consumer<SyntaxNode> nonTerminalConsumer) {
        addExp.walk(terminalConsumer, nonTerminalConsumer);
        terminalConsumer.accept(new Token(null, op, -1));
        mulExp.walk(terminalConsumer, nonTerminalConsumer);
        nonTerminalConsumer.accept(this);
    }
}
