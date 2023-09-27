package sysy.parser.syntaxtree;

import sysy.lexer.LexType;
import sysy.lexer.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class FuncFParamsNode extends SyntaxNode {
    public List<FuncFParamNode> params = new ArrayList<>();

    @Override
    public void walk(Consumer<Token> terminalConsumer, Consumer<SyntaxNode> nonTerminalConsumer) {
        boolean first = true;

        for (var param : params) {
            if (first) {
                first = false;
            } else {
                terminalConsumer.accept(new Token(null, LexType.COMMA, -1));
            }
            param.walk(terminalConsumer, nonTerminalConsumer);
        }

        nonTerminalConsumer.accept(this);
    }
}
