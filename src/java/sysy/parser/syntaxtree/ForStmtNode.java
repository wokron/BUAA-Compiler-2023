package sysy.parser.syntaxtree;

import sysy.lexer.LexType;
import sysy.parser.syntaxtree.symbol.NonTerminalSymbol;
import sysy.parser.syntaxtree.symbol.TerminalSymbol;

import java.util.function.Consumer;

public class ForStmtNode extends SyntaxNode {
    public LValNode lVal;
    public ExpNode exp;

    @Override
    public void walk(Consumer<TerminalSymbol> terminalConsumer, Consumer<NonTerminalSymbol> nonTerminalConsumer) {
        lVal.walk(terminalConsumer, nonTerminalConsumer);

        terminalConsumer.accept(new TerminalSymbol(LexType.ASSIGN));

        exp.walk(terminalConsumer, nonTerminalConsumer);

        nonTerminalConsumer.accept(new NonTerminalSymbol(this));
    }

    @Override
    public String getType() {
        return "ForStmt";
    }
}
