package sysy.parser.syntaxtree;

import sysy.lexer.LexType;
import sysy.lexer.Token;

import java.util.function.Consumer;

public class LOrExpNodeForDouble extends LOrExpNode {
    public LOrExpNode lOrExp;
    public LAndExpNode lAndExp;

    @Override
    public void walk(Consumer<Token> terminalConsumer, Consumer<SyntaxNode> nonTerminalConsumer) {
        lOrExp.walk(terminalConsumer, nonTerminalConsumer);

        terminalConsumer.accept(new Token(null, LexType.OR, -1));

        lAndExp.walk(terminalConsumer, nonTerminalConsumer);

        nonTerminalConsumer.accept(this);
    }
}
