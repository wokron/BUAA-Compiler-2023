package sysy.parser.syntaxtree;

import sysy.parser.syntaxtree.symbol.NonTerminalSymbol;
import sysy.parser.syntaxtree.symbol.TerminalSymbol;

import java.util.function.Consumer;

public class BlockItemNodeForDecl extends BlockItemNode {
    public DeclNode decl;

    @Override
    public void walk(Consumer<TerminalSymbol> terminalConsumer, Consumer<NonTerminalSymbol> nonTerminalConsumer) {
        decl.walk(terminalConsumer, nonTerminalConsumer);
        nonTerminalConsumer.accept(new NonTerminalSymbol(this));
    }
}
