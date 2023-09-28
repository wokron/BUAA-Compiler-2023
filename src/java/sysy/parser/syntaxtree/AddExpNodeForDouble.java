package sysy.parser.syntaxtree;

import sysy.lexer.LexType;
import sysy.parser.syntaxtree.symbol.NonTerminalSymbol;
import sysy.parser.syntaxtree.symbol.TerminalSymbol;

import java.util.function.Consumer;

public class AddExpNodeForDouble extends AddExpNode {
    public AddExpNode addExp;
    public LexType op;
    public MulExpNode mulExp;

    @Override
    public void walk(Consumer<TerminalSymbol> terminalConsumer, Consumer<NonTerminalSymbol> nonTerminalConsumer) {
        addExp.walk(terminalConsumer, nonTerminalConsumer);
        terminalConsumer.accept(new TerminalSymbol(op));
        mulExp.walk(terminalConsumer, nonTerminalConsumer);

        nonTerminalConsumer.accept(new NonTerminalSymbol(this));
    }
}
