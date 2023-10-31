package sysy.frontend.parser.syntaxtree;

import sysy.frontend.lexer.LexType;
import sysy.frontend.parser.syntaxtree.symbol.NonTerminalSymbol;
import sysy.frontend.parser.syntaxtree.symbol.TerminalSymbol;

import java.util.function.Consumer;

public class EqExpNodeForDouble extends EqExpNode {
    public EqExpNode eqExp;
    public LexType op;
    public RelExpNode relExp;

    @Override
    public void walk(Consumer<TerminalSymbol> terminalConsumer, Consumer<NonTerminalSymbol> nonTerminalConsumer) {
        eqExp.walk(terminalConsumer, nonTerminalConsumer);

        terminalConsumer.accept(new TerminalSymbol(op));

        relExp.walk(terminalConsumer, nonTerminalConsumer);

        nonTerminalConsumer.accept(new NonTerminalSymbol(this));
    }
}
