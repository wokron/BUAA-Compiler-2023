package sysy.parser.syntaxtree;

import sysy.lexer.LexType;
import sysy.parser.syntaxtree.symbol.NonTerminalSymbol;
import sysy.parser.syntaxtree.symbol.TerminalSymbol;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ConstDefNode extends SyntaxNode {
    public String ident;
    public List<ConstExpNode> dimensions = new ArrayList<>();
    public ConstInitValNode constInitVal;

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
