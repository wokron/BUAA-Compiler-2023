package sysy.parser.syntaxtree;

import sysy.lexer.LexType;
import sysy.lexer.Token;

import java.util.function.Consumer;

public class StmtNodeForIfElse extends StmtNode {
    public CondNode cond;
    public StmtNode ifStmt;
    public StmtNode elseStmt;

    @Override
    public void walk(Consumer<Token> terminalConsumer, Consumer<SyntaxNode> nonTerminalConsumer) {
        terminalConsumer.accept(new Token(null, LexType.IFTK, -1));
        terminalConsumer.accept(new Token(null, LexType.LPARENT, -1));

        cond.walk(terminalConsumer, nonTerminalConsumer);

        terminalConsumer.accept(new Token(null, LexType.RPARENT, -1));

        ifStmt.walk(terminalConsumer, nonTerminalConsumer);

        if (elseStmt != null) {
            terminalConsumer.accept(new Token(null, LexType.ELSETK, -1));
            elseStmt.walk(terminalConsumer, nonTerminalConsumer);
        }

        nonTerminalConsumer.accept(this);
    }
}
