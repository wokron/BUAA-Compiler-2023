package sysy.parser.syntaxtree;

import sysy.lexer.LexType;
import sysy.lexer.Token;

import java.util.function.Consumer;

public class BTypeNode extends SyntaxNode {
    public LexType type = LexType.INTTK;

    @Override
    public void walk(Consumer<Token> terminalConsumer, Consumer<SyntaxNode> nonTerminalConsumer) {
        terminalConsumer.accept(new Token(null, type, -1));
        nonTerminalConsumer.accept(this);
    }
}
