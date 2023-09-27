package sysy.parser.syntaxtree;

import sysy.parser.syntaxtree.symbol.NonTerminalSymbol;
import sysy.parser.syntaxtree.symbol.TerminalSymbol;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class BlockNode extends SyntaxNode {
    public List<BlockItemNode> blockItems = new ArrayList<>();

    @Override
    public void walk(Consumer<TerminalSymbol> terminalConsumer, Consumer<NonTerminalSymbol> nonTerminalConsumer) {
        for (var blockItem : blockItems) {
            blockItem.walk(terminalConsumer, nonTerminalConsumer);
        }
        nonTerminalConsumer.accept(new NonTerminalSymbol(this));
    }
}
