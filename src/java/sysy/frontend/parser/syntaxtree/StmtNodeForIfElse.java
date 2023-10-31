package sysy.frontend.parser.syntaxtree;

import sysy.frontend.lexer.LexType;
import sysy.frontend.parser.syntaxtree.symbol.NonTerminalSymbol;
import sysy.frontend.parser.syntaxtree.symbol.TerminalSymbol;

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
