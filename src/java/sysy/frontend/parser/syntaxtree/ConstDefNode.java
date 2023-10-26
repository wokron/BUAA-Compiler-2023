package sysy.frontend.parser.syntaxtree;

import sysy.frontend.lexer.LexType;
import sysy.frontend.parser.syntaxtree.symbol.NonTerminalSymbol;
import sysy.frontend.parser.syntaxtree.symbol.TerminalSymbol;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ConstDefNode extends SyntaxNode {
    public String ident;
    public List<ConstExpNode> dimensions = new ArrayList<>();
    public ConstInitValNode constInitVal;
    public int identLineNum = -1;

    @Override
    public void walk(Consumer<TerminalSymbol> terminalConsumer, Consumer<NonTerminalSymbol> nonTerminalConsumer) {
        terminalConsumer.accept(new TerminalSymbol(LexType.IDENFR, ident));

        for (var dimension : dimensions) {
            terminalConsumer.accept(new TerminalSymbol(LexType.LBRACK));

            dimension.walk(terminalConsumer, nonTerminalConsumer);

            terminalConsumer.accept(new TerminalSymbol(LexType.RBRACK));
        }

        terminalConsumer.accept(new TerminalSymbol(LexType.ASSIGN));

        constInitVal.walk(terminalConsumer, nonTerminalConsumer);

        nonTerminalConsumer.accept(new NonTerminalSymbol(this));
    }

    @Override
    public String getType() {
        return "ConstDef";
    }
}
