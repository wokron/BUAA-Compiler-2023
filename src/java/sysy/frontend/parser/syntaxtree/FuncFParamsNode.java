package sysy.frontend.parser.syntaxtree;

import sysy.frontend.lexer.LexType;
import sysy.frontend.parser.syntaxtree.symbol.NonTerminalSymbol;
import sysy.frontend.parser.syntaxtree.symbol.TerminalSymbol;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class FuncFParamsNode extends SyntaxNode {
    public List<FuncFParamNode> params = new ArrayList<>();

    @Override
    public void walk(Consumer<TerminalSymbol> terminalConsumer, Consumer<NonTerminalSymbol> nonTerminalConsumer) {
        boolean first = true;

        for (var param : params) {
            if (first) {
                first = false;
            } else {
                terminalConsumer.accept(new TerminalSymbol(LexType.COMMA));
            }
            param.walk(terminalConsumer, nonTerminalConsumer);
        }

        nonTerminalConsumer.accept(new NonTerminalSymbol(this));
    }

    @Override
    public String getType() {
        return "FuncFParams";
    }
}
