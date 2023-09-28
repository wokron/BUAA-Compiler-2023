package sysy.parser.syntaxtree;

import sysy.lexer.LexType;
import sysy.parser.syntaxtree.symbol.NonTerminalSymbol;
import sysy.parser.syntaxtree.symbol.TerminalSymbol;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class VarDeclNode extends SyntaxNode {
    public BTypeNode type;
    public List<VarDefNode> varDefs = new ArrayList<>();

    @Override
    public void walk(Consumer<TerminalSymbol> terminalConsumer, Consumer<NonTerminalSymbol> nonTerminalConsumer) {
        type.walk(terminalConsumer, nonTerminalConsumer);

        boolean first = true;
        for (var varDef : varDefs) {
            if (first) {
                first = false;
            } else {
                terminalConsumer.accept(new TerminalSymbol(LexType.COMMA));
            }
            varDef.walk(terminalConsumer, nonTerminalConsumer);
        }
        terminalConsumer.accept(new TerminalSymbol(LexType.SEMICN));

        nonTerminalConsumer.accept(new NonTerminalSymbol(this));
    }

    @Override
    public String getType() {
        return "VarDecl";
    }
}
