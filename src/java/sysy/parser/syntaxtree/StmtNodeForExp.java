package sysy.parser.syntaxtree;

import sysy.lexer.LexType;
import sysy.parser.syntaxtree.symbol.NonTerminalSymbol;
import sysy.parser.syntaxtree.symbol.TerminalSymbol;

import java.util.function.Consumer;

public class StmtNodeForExp extends StmtNode {
    public ExpNode exp;

    @Override
    public void walk(Consumer<TerminalSymbol> terminalConsumer, Consumer<NonTerminalSymbol> nonTerminalConsumer) {
        if (exp != null) {
            exp.walk(terminalConsumer, nonTerminalConsumer);
        }

        terminalConsumer.accept(new TerminalSymbol(LexType.SEMICN));

        nonTerminalConsumer.accept(new NonTerminalSymbol(this));
    }
}
