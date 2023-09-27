package sysy.parser.syntaxtree;

import sysy.lexer.Token;

import java.util.function.Consumer;

public class LOrExpNodeForSingle extends LOrExpNode {
    public LAndExpNode lAndExp;

    @Override
    public void walk(Consumer<Token> terminalConsumer, Consumer<SyntaxNode> nonTerminalConsumer) {
        lAndExp.walk(terminalConsumer, nonTerminalConsumer);

        nonTerminalConsumer.accept(this);
    }
}
