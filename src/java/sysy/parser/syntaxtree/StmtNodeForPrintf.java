package sysy.parser.syntaxtree;

import sysy.lexer.LexType;
import sysy.parser.syntaxtree.symbol.NonTerminalSymbol;
import sysy.parser.syntaxtree.symbol.TerminalSymbol;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class StmtNodeForPrintf extends StmtNode {
    public String formatString;
    public List<ExpNode> exps = new ArrayList<>();

    @Override
    public void walk(Consumer<TerminalSymbol> terminalConsumer, Consumer<NonTerminalSymbol> nonTerminalConsumer) {
        terminalConsumer.accept(new TerminalSymbol(LexType.PRINTFTK));
        terminalConsumer.accept(new TerminalSymbol(LexType.LPARENT));

        terminalConsumer.accept(new TerminalSymbol(LexType.STRCON, formatString));

        for (var exp : exps) {
            terminalConsumer.accept(new TerminalSymbol(LexType.COMMA));
            exp.walk(terminalConsumer, nonTerminalConsumer);
        }

        terminalConsumer.accept(new TerminalSymbol(LexType.RPARENT));
        terminalConsumer.accept(new TerminalSymbol(LexType.SEMICN));

        nonTerminalConsumer.accept(new NonTerminalSymbol(this));
    }
}
