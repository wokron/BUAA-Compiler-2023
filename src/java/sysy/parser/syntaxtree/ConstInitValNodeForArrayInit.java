package sysy.parser.syntaxtree;

import sysy.lexer.LexType;
import sysy.lexer.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ConstInitValNodeForArrayInit extends ConstInitValNode {
    public List<ConstInitValNode> initValues = new ArrayList<>();

    @Override
    public void walk(Consumer<Token> terminalConsumer, Consumer<SyntaxNode> nonTerminalConsumer) {
        terminalConsumer.accept(new Token(null, LexType.LBRACE, -1));

        boolean first = true;
        for (var init : initValues) {
            if (first) {
                first = false;
            } else {
                terminalConsumer.accept(new Token(null, LexType.COMMA, -1));
            }
            init.walk(terminalConsumer, nonTerminalConsumer);
        }

        terminalConsumer.accept(new Token(null, LexType.RBRACE, -1));
    }
}
