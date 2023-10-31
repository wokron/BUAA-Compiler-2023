package sysy.frontend.parser.syntaxtree;

import sysy.frontend.parser.syntaxtree.symbol.NonTerminalSymbol;
import sysy.frontend.parser.syntaxtree.symbol.TerminalSymbol;

import java.util.function.Consumer;

public class AddExpNodeForSingle extends AddExpNode {
    public MulExpNode mulExp;

    @Override
    public void walk(Consumer<TerminalSymbol> terminalConsumer, Consumer<NonTerminalSymbol> nonTerminalConsumer) {
        mulExp.walk(terminalConsumer, nonTerminalConsumer);
        nonTerminalConsumer.accept(new NonTerminalSymbol(this));
    }
}
