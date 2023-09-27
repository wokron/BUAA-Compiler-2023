package sysy.parser.syntaxtree;

import sysy.lexer.LexType;
import sysy.lexer.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class InitValNodeForArray extends InitValNode {
    public List<InitValNode> initVals = new ArrayList<>();

    @Override
    public void walk(Consumer<Token> terminalConsumer, Consumer<SyntaxNode> nonTerminalConsumer) {
        terminalConsumer.accept(new Token(null, LexType.LBRACE, -1));

        boolean first = true;
        for (var init : initVals) {
            if (first) {
                first = false;
            } else {
                terminalConsumer.accept(new Token(null, LexType.COMMA, -1));
            }
            init.walk(terminalConsumer, nonTerminalConsumer);
        }

        nonTerminalConsumer.accept(this);
    }
}
