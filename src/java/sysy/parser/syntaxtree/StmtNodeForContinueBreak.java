package sysy.parser.syntaxtree;

import sysy.lexer.LexType;
import sysy.parser.syntaxtree.symbol.NonTerminalSymbol;
import sysy.parser.syntaxtree.symbol.TerminalSymbol;

import java.util.function.Consumer;

public class StmtNodeForContinueBreak extends StmtNode {
    public LexType type;
    public int tkLineNum = -1;

    @Override
    public void walk(Consumer<TerminalSymbol> terminalConsumer, Consumer<NonTerminalSymbol> nonTerminalConsumer) {
        terminalConsumer.accept(new TerminalSymbol(type));
        terminalConsumer.accept(new TerminalSymbol(LexType.SEMICN));

        nonTerminalConsumer.accept(new NonTerminalSymbol(this));

    }
}
