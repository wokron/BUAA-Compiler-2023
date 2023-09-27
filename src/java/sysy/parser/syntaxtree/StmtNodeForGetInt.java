package sysy.parser.syntaxtree;

import sysy.lexer.LexType;
import sysy.lexer.Token;

import java.util.function.Consumer;

public class StmtNodeForGetInt extends StmtNode {
    public LValNode lVal;

    @Override
    public void walk(Consumer<Token> terminalConsumer, Consumer<SyntaxNode> nonTerminalConsumer) {
        lVal.walk(terminalConsumer, nonTerminalConsumer);

        terminalConsumer.accept(new Token(null, LexType.ASSIGN, -1));
        terminalConsumer.accept(new Token(null, LexType.GETINTTK, -1));
        terminalConsumer.accept(new Token(null, LexType.LPARENT, -1));
        terminalConsumer.accept(new Token(null, LexType.RPARENT, -1));
        terminalConsumer.accept(new Token(null, LexType.SEMICN, -1));

        nonTerminalConsumer.accept(this);
    }
}
