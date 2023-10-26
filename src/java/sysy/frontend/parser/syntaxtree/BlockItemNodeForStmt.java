package sysy.frontend.parser.syntaxtree;

import sysy.frontend.parser.syntaxtree.symbol.NonTerminalSymbol;
import sysy.frontend.parser.syntaxtree.symbol.TerminalSymbol;

import java.util.function.Consumer;

public class BlockItemNodeForStmt extends BlockItemNode {
    public StmtNode stmt;

    @Override
    public void walk(Consumer<TerminalSymbol> terminalConsumer, Consumer<NonTerminalSymbol> nonTerminalConsumer) {
        stmt.walk(terminalConsumer, nonTerminalConsumer);
        nonTerminalConsumer.accept(new NonTerminalSymbol(this));
    }
}
