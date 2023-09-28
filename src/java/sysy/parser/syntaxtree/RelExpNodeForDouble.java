package sysy.parser.syntaxtree;

import sysy.lexer.LexType;
import sysy.parser.syntaxtree.symbol.NonTerminalSymbol;
import sysy.parser.syntaxtree.symbol.TerminalSymbol;

import java.util.function.Consumer;

public class RelExpNodeForDouble extends RelExpNode {
    public RelExpNode relExp;
    public LexType op;
    public AddExpNode addExp;

    @Override
    public void walk(Consumer<TerminalSymbol> terminalConsumer, Consumer<NonTerminalSymbol> nonTerminalConsumer) {
        relExp.walk(terminalConsumer, nonTerminalConsumer);

        terminalConsumer.accept(new TerminalSymbol(op));

        addExp.walk(terminalConsumer, nonTerminalConsumer);

        nonTerminalConsumer.accept(new NonTerminalSymbol(this));
    }
}
