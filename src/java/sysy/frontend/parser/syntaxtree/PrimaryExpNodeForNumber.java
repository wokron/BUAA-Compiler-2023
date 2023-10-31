package sysy.frontend.parser.syntaxtree;

import sysy.frontend.parser.syntaxtree.symbol.NonTerminalSymbol;
import sysy.frontend.parser.syntaxtree.symbol.TerminalSymbol;

import java.util.function.Consumer;

public class PrimaryExpNodeForNumber extends PrimaryExpNode {
    public NumberNode number;

    @Override
    public void walk(Consumer<TerminalSymbol> terminalConsumer, Consumer<NonTerminalSymbol> nonTerminalConsumer) {
        number.walk(terminalConsumer, nonTerminalConsumer);

        nonTerminalConsumer.accept(new NonTerminalSymbol(this));
    }
}
