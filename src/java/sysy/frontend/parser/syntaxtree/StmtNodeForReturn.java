package sysy.frontend.parser.syntaxtree;

import sysy.frontend.lexer.LexType;
import sysy.frontend.parser.syntaxtree.symbol.NonTerminalSymbol;
import sysy.frontend.parser.syntaxtree.symbol.TerminalSymbol;

import java.util.function.Consumer;

public class StmtNodeForReturn extends StmtNode {
    public ExpNode exp;
    public int returnLineNum = -1;

    @Override
    public void walk(Consumer<TerminalSymbol> terminalConsumer, Consumer<NonTerminalSymbol> nonTerminalConsumer) {
        terminalConsumer.accept(new TerminalSymbol(LexType.RETURNTK));

        if (exp != null) {
            exp.walk(terminalConsumer, nonTerminalConsumer);
        }

        terminalConsumer.accept(new TerminalSymbol(LexType.SEMICN));

        nonTerminalConsumer.accept(new NonTerminalSymbol(this));
    }
}
