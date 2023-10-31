package sysy.frontend.parser.syntaxtree;

import sysy.frontend.lexer.LexType;
import sysy.frontend.parser.syntaxtree.symbol.NonTerminalSymbol;
import sysy.frontend.parser.syntaxtree.symbol.TerminalSymbol;

import java.util.List;
import java.util.function.Consumer;

public class FuncFParamNode extends SyntaxNode {
    public BTypeNode type;
    public String ident;
    public List<ConstExpNode> dimensions;
    public int identLineNum = -1;

    @Override
    public void walk(Consumer<TerminalSymbol> terminalConsumer, Consumer<NonTerminalSymbol> nonTerminalConsumer) {
        type.walk(terminalConsumer, nonTerminalConsumer);

        terminalConsumer.accept(new TerminalSymbol(LexType.IDENFR, ident));

        if (dimensions != null) {
            terminalConsumer.accept(new TerminalSymbol(LexType.LBRACK));
            terminalConsumer.accept(new TerminalSymbol(LexType.RBRACK));
            for (var dim : dimensions) {
                terminalConsumer.accept(new TerminalSymbol(LexType.LBRACK));
                dim.walk(terminalConsumer, nonTerminalConsumer);
                terminalConsumer.accept(new TerminalSymbol(LexType.RBRACK));
            }
        }

        nonTerminalConsumer.accept(new NonTerminalSymbol(this));
    }

    @Override
    public String getType() {
        return "FuncFParam";
    }
}
