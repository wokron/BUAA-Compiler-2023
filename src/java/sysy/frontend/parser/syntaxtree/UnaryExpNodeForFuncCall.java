package sysy.frontend.parser.syntaxtree;

import sysy.frontend.lexer.LexType;
import sysy.frontend.parser.syntaxtree.symbol.NonTerminalSymbol;
import sysy.frontend.parser.syntaxtree.symbol.TerminalSymbol;

import java.util.function.Consumer;

public class UnaryExpNodeForFuncCall extends UnaryExpNode {
    public String ident;
    public FuncRParamsNode params;
    public int identLineNum = -1;

    @Override
    public void walk(Consumer<TerminalSymbol> terminalConsumer, Consumer<NonTerminalSymbol> nonTerminalConsumer) {
        terminalConsumer.accept(new TerminalSymbol(LexType.IDENFR, ident));
        terminalConsumer.accept(new TerminalSymbol(LexType.LPARENT));

        if (params != null) {
            params.walk(terminalConsumer, nonTerminalConsumer);
        }

        terminalConsumer.accept(new TerminalSymbol(LexType.RPARENT));

        nonTerminalConsumer.accept(new NonTerminalSymbol(this));
    }
}
