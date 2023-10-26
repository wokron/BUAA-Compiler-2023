package sysy.frontend.parser.syntaxtree;

import sysy.frontend.lexer.LexType;
import sysy.frontend.parser.syntaxtree.symbol.NonTerminalSymbol;
import sysy.frontend.parser.syntaxtree.symbol.TerminalSymbol;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class VarDefNode extends SyntaxNode {
    public String ident;
    public List<ConstExpNode> dimensions = new ArrayList<>();
    public InitValNode initVal;
    public int identLineNum = -1;

    @Override
    public void walk(Consumer<TerminalSymbol> terminalConsumer, Consumer<NonTerminalSymbol> nonTerminalConsumer) {
        terminalConsumer.accept(new TerminalSymbol(LexType.IDENFR, ident));

        for (var dim : dimensions) {
            terminalConsumer.accept(new TerminalSymbol(LexType.LBRACK));

            dim.walk(terminalConsumer, nonTerminalConsumer);

            terminalConsumer.accept(new TerminalSymbol(LexType.RBRACK));
        }

        if (initVal != null) {
            terminalConsumer.accept(new TerminalSymbol(LexType.ASSIGN));
            initVal.walk(terminalConsumer, nonTerminalConsumer);
        }

        nonTerminalConsumer.accept(new NonTerminalSymbol(this));
    }

    @Override
    public String getType() {
        return "VarDef";
    }
}
