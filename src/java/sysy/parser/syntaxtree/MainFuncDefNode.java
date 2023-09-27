package sysy.parser.syntaxtree;

import sysy.lexer.LexType;
import sysy.parser.syntaxtree.symbol.NonTerminalSymbol;
import sysy.parser.syntaxtree.symbol.TerminalSymbol;

import java.util.function.Consumer;

public class MainFuncDefNode extends SyntaxNode {
    public BlockNode mainBlock;

    @Override
    public void walk(Consumer<TerminalSymbol> terminalConsumer, Consumer<NonTerminalSymbol> nonTerminalConsumer) {
        terminalConsumer.accept(new TerminalSymbol(LexType.INTTK));
        terminalConsumer.accept(new TerminalSymbol(LexType.MAINTK));
        terminalConsumer.accept(new TerminalSymbol(LexType.LPARENT));
        terminalConsumer.accept(new TerminalSymbol(LexType.RPARENT));

        mainBlock.walk(terminalConsumer, nonTerminalConsumer);

        nonTerminalConsumer.accept(new NonTerminalSymbol(this));
    }

    @Override
    public String getType() {
        return "MainFuncDef";
    }
}
