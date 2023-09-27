package sysy.parser.syntaxtree;

import sysy.lexer.LexType;
import sysy.lexer.Token;

import java.util.function.Consumer;

public class StmtNodeForContinueBreak extends StmtNode {
    public LexType type;

    @Override
    public void walk(Consumer<Token> terminalConsumer, Consumer<SyntaxNode> nonTerminalConsumer) {
        terminalConsumer.accept(new Token(null, type, -1));
        terminalConsumer.accept(new Token(null, LexType.SEMICN, -1));

        nonTerminalConsumer.accept(this);

    }
}
