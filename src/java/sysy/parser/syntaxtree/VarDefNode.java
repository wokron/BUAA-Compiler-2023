package sysy.parser.syntaxtree;

import sysy.lexer.LexType;
import sysy.parser.syntaxtree.symbol.NonTerminalSymbol;
import sysy.parser.syntaxtree.symbol.TerminalSymbol;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class VarDefNode extends SyntaxNode {
    public String ident;
    public List<ConstExpNode> dimensions = new ArrayList<>();
    public InitValNode initVal;

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
}
