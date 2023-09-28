package sysy.parser.syntaxtree;

import sysy.lexer.LexType;
import sysy.parser.syntaxtree.symbol.NonTerminalSymbol;
import sysy.parser.syntaxtree.symbol.TerminalSymbol;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class BlockNode extends SyntaxNode {
    public List<BlockItemNode> blockItems = new ArrayList<>();

    @Override
    public void walk(Consumer<TerminalSymbol> terminalConsumer, Consumer<NonTerminalSymbol> nonTerminalConsumer) {
        terminalConsumer.accept(new TerminalSymbol(LexType.LBRACE));

        for (var blockItem : blockItems) {
            blockItem.walk(terminalConsumer, nonTerminalConsumer);
        }

        terminalConsumer.accept(new TerminalSymbol(LexType.RBRACE));

        nonTerminalConsumer.accept(new NonTerminalSymbol(this));
    }

    @Override
    public String getType() {
        return "Block";
    }
}
