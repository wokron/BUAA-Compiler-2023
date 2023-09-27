package sysy.parser.syntaxtree;

import sysy.lexer.LexType;
import sysy.parser.syntaxtree.symbol.NonTerminalSymbol;
import sysy.parser.syntaxtree.symbol.TerminalSymbol;

import java.util.function.Consumer;

public class StmtNodeForIfElse extends StmtNode {
    public CondNode cond;
    public StmtNode ifStmt;
    public StmtNode elseStmt;

    @Override
    public void walk(Consumer<TerminalSymbol> terminalConsumer, Consumer<NonTerminalSymbol> nonTerminalConsumer) {
        terminalConsumer.accept(new TerminalSymbol(LexType.IFTK));
        terminalConsumer.accept(new TerminalSymbol(LexType.LPARENT));

        cond.walk(terminalConsumer, nonTerminalConsumer);

        terminalConsumer.accept(new TerminalSymbol(LexType.RPARENT));

        ifStmt.walk(terminalConsumer, nonTerminalConsumer);

        if (elseStmt != null) {
            terminalConsumer.accept(new TerminalSymbol(LexType.ELSETK));
            elseStmt.walk(terminalConsumer, nonTerminalConsumer);
        }

        nonTerminalConsumer.accept(new NonTerminalSymbol(this));
    }
}
