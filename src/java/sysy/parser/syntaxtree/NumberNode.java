package sysy.parser.syntaxtree;

import sysy.lexer.LexType;
import sysy.lexer.Token;

import java.util.function.Consumer;

public class NumberNode extends SyntaxNode {
    public String intConst;

    @Override
    public void walk(Consumer<Token> terminalConsumer, Consumer<SyntaxNode> nonTerminalConsumer) {
        terminalConsumer.accept(new Token(intConst, LexType.INTCON, -1));

        nonTerminalConsumer.accept(this);
    }
}
