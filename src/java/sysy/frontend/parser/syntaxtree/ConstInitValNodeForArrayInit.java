package sysy.frontend.parser.syntaxtree;

import sysy.frontend.lexer.LexType;
import sysy.frontend.parser.syntaxtree.symbol.NonTerminalSymbol;
import sysy.frontend.parser.syntaxtree.symbol.TerminalSymbol;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ConstInitValNodeForArrayInit extends ConstInitValNode {
    public List<ConstInitValNode> initValues = new ArrayList<>();

    @Override
    public void walk(Consumer<TerminalSymbol> terminalConsumer, Consumer<NonTerminalSymbol> nonTerminalConsumer) {
        terminalConsumer.accept(new TerminalSymbol(LexType.LBRACE));

        boolean first = true;
        for (var init : initValues) {
            if (first) {
                first = false;
            } else {
                terminalConsumer.accept(new TerminalSymbol(LexType.COMMA));
            }
            init.walk(terminalConsumer, nonTerminalConsumer);
        }

        terminalConsumer.accept(new TerminalSymbol(LexType.RBRACE));

        nonTerminalConsumer.accept(new NonTerminalSymbol(this));
    }
}
