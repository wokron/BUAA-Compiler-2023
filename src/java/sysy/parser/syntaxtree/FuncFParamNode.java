package sysy.parser.syntaxtree;

import sysy.lexer.LexType;
import sysy.parser.syntaxtree.symbol.NonTerminalSymbol;
import sysy.parser.syntaxtree.symbol.TerminalSymbol;

import java.util.List;
import java.util.function.Consumer;

public class FuncFParamNode extends SyntaxNode {
    public BTypeNode type;
    public String ident;
    public List<ConstExpNode> dimensions;

    @Override
    public void walk(Consumer<TerminalSymbol> terminalConsumer, Consumer<NonTerminalSymbol> nonTerminalConsumer) {
        type.walk(terminalConsumer, nonTerminalConsumer);

        terminalConsumer.accept(new TerminalSymbol(LexType.IDENFR, ident));

        if (dimensions != null) {
            terminalConsumer.accept(new TerminalSymbol(LexType.LBRACK));
            terminalConsumer.accept(new TerminalSymbol(LexType.RBRACK, ident));
            for (var dim : dimensions) {
                terminalConsumer.accept(new TerminalSymbol(LexType.LBRACK));
                dim.walk(terminalConsumer, nonTerminalConsumer);
                terminalConsumer.accept(new TerminalSymbol(LexType.RBRACK, ident));
            }
        }

        nonTerminalConsumer.accept(new NonTerminalSymbol(this));
    }
}
