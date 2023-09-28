package sysy.parser.syntaxtree;

import sysy.lexer.LexType;
import sysy.parser.syntaxtree.symbol.NonTerminalSymbol;
import sysy.parser.syntaxtree.symbol.TerminalSymbol;

import java.util.function.Consumer;

public class NumberNode extends SyntaxNode {
    public String intConst;

    @Override
    public void walk(Consumer<TerminalSymbol> terminalConsumer, Consumer<NonTerminalSymbol> nonTerminalConsumer) {
        terminalConsumer.accept(new TerminalSymbol(LexType.INTCON, intConst));

        nonTerminalConsumer.accept(new NonTerminalSymbol(this));
    }

    @Override
    public String getType() {
        return "Number";
    }
}
