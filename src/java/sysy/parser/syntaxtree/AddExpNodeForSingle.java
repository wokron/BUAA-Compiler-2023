package sysy.parser.syntaxtree;

import sysy.lexer.Token;

import java.util.function.Consumer;

public class AddExpNodeForSingle extends AddExpNode {
    public MulExpNode mulExp;

    @Override
    public void walk(Consumer<Token> terminalConsumer, Consumer<SyntaxNode> nonTerminalConsumer) {
        mulExp.walk(terminalConsumer, nonTerminalConsumer);
        nonTerminalConsumer.accept(this);
    }
}
