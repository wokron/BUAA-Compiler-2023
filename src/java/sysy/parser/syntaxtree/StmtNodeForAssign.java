package sysy.parser.syntaxtree;

import sysy.lexer.LexType;
import sysy.lexer.Token;

import java.util.function.Consumer;

public class StmtNodeForAssign extends StmtNode {
    public LValNode lVal;
    public ExpNode exp;

    @Override
    public void walk(Consumer<Token> terminalConsumer, Consumer<SyntaxNode> nonTerminalConsumer) {
        lVal.walk(terminalConsumer, nonTerminalConsumer);

        terminalConsumer.accept(new Token(null, LexType.ASSIGN, -1));

        exp.walk(terminalConsumer, nonTerminalConsumer);

        terminalConsumer.accept(new Token(null, LexType.SEMICN, -1));

        nonTerminalConsumer.accept(this);
    }
}
