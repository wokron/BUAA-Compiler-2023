package sysy.parser.syntaxtree;

import sysy.parser.syntaxtree.symbol.NonTerminalSymbol;
import sysy.parser.syntaxtree.symbol.TerminalSymbol;

import java.util.function.Consumer;

public class UnaryExpNodeForPrimaryExp extends UnaryExpNode {
    public PrimaryExpNode primaryExp;

    @Override
    public void walk(Consumer<TerminalSymbol> terminalConsumer, Consumer<NonTerminalSymbol> nonTerminalConsumer) {
        primaryExp.walk(terminalConsumer, nonTerminalConsumer);

        nonTerminalConsumer.accept(new NonTerminalSymbol(this));
    }
}
