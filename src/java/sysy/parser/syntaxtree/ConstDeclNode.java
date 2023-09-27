package sysy.parser.syntaxtree;

import sysy.lexer.LexType;
import sysy.parser.syntaxtree.symbol.NonTerminalSymbol;
import sysy.parser.syntaxtree.symbol.TerminalSymbol;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ConstDeclNode extends SyntaxNode {
    public BTypeNode type;
    public List<ConstDefNode> constDefs = new ArrayList<>();

    @Override
    public void walk(Consumer<TerminalSymbol> terminalConsumer, Consumer<NonTerminalSymbol> nonTerminalConsumer) {
        terminalConsumer.accept(new TerminalSymbol(LexType.CONSTTK));
        type.walk(terminalConsumer, nonTerminalConsumer);
        boolean first = true;
        for (var constDef : constDefs) {
            if (first) {
                first = false;
            } else {
                terminalConsumer.accept(new TerminalSymbol(LexType.COMMA));
            }
            constDef.walk(terminalConsumer, nonTerminalConsumer);
        }
        terminalConsumer.accept(new TerminalSymbol(LexType.SEMICN));
        nonTerminalConsumer.accept(new NonTerminalSymbol(this));
    }
}
