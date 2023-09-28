package sysy.parser.syntaxtree;

import sysy.lexer.LexType;
import sysy.parser.syntaxtree.symbol.NonTerminalSymbol;
import sysy.parser.syntaxtree.symbol.TerminalSymbol;

import java.util.function.Consumer;

public class StmtNodeForGetInt extends StmtNode {
    public LValNode lVal;

    @Override
    public void walk(Consumer<TerminalSymbol> terminalConsumer, Consumer<NonTerminalSymbol> nonTerminalConsumer) {
        lVal.walk(terminalConsumer, nonTerminalConsumer);

        terminalConsumer.accept(new TerminalSymbol(LexType.ASSIGN));
        terminalConsumer.accept(new TerminalSymbol(LexType.GETINTTK));
        terminalConsumer.accept(new TerminalSymbol(LexType.LPARENT));
        terminalConsumer.accept(new TerminalSymbol(LexType.RPARENT));
        terminalConsumer.accept(new TerminalSymbol(LexType.SEMICN));

        nonTerminalConsumer.accept(new NonTerminalSymbol(this));
    }
}
