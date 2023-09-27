package sysy.parser.syntaxtree;

import sysy.lexer.LexType;
import sysy.lexer.Token;

import java.util.function.Consumer;

public class StmtNodeForLoop extends StmtNode {
    public ForStmtNode forStmt1;
    public CondNode cond;
    public ForStmtNode forStmt2;
    public StmtNode stmt;

    @Override
    public void walk(Consumer<Token> terminalConsumer, Consumer<SyntaxNode> nonTerminalConsumer) {
        terminalConsumer.accept(new Token(null, LexType.FORTK, -1));
        terminalConsumer.accept(new Token(null, LexType.LPARENT, -1));

        if (forStmt1 != null) {
            forStmt1.walk(terminalConsumer, nonTerminalConsumer);
        }

        terminalConsumer.accept(new Token(null, LexType.SEMICN, -1));

        if (cond != null) {
            cond.walk(terminalConsumer, nonTerminalConsumer);
        }

        terminalConsumer.accept(new Token(null, LexType.SEMICN, -1));

        if (forStmt2 != null) {
            forStmt2.walk(terminalConsumer, nonTerminalConsumer);
        }

        terminalConsumer.accept(new Token(null, LexType.RPARENT, -1));

        stmt.walk(terminalConsumer, nonTerminalConsumer);

        nonTerminalConsumer.accept(this);
    }
}
