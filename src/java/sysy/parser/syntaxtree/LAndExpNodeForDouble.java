package sysy.parser.syntaxtree;

import sysy.lexer.LexType;
import sysy.lexer.Token;

import java.util.function.Consumer;

public class LAndExpNodeForDouble extends LAndExpNode {
    public LAndExpNode lAndExp;
    public EqExpNode eqExp;

    @Override
    public void walk(Consumer<Token> terminalConsumer, Consumer<SyntaxNode> nonTerminalConsumer) {
        lAndExp.walk(terminalConsumer, nonTerminalConsumer);

        terminalConsumer.accept(new Token(null, LexType.AND, -1));

        eqExp.walk(terminalConsumer, nonTerminalConsumer);

        nonTerminalConsumer.accept(this);
    }
}
