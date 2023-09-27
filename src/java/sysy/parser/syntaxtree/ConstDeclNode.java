package sysy.parser.syntaxtree;

import sysy.lexer.LexType;
import sysy.lexer.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ConstDeclNode extends SyntaxNode {
    public BTypeNode type;
    public List<ConstDefNode> constDefs = new ArrayList<>();

    @Override
    public void walk(Consumer<Token> terminalConsumer, Consumer<SyntaxNode> nonTerminalConsumer) {
        terminalConsumer.accept(new Token(null, LexType.CONSTTK, -1));
        type.walk(terminalConsumer, nonTerminalConsumer);
        boolean first = true;
        for (var constDef : constDefs) {
            if (first) {
                first = false;
            } else {
                terminalConsumer.accept(new Token(null, LexType.COMMA, -1));
            }
            constDef.walk(terminalConsumer, nonTerminalConsumer);
        }
        terminalConsumer.accept(new Token(null, LexType.SEMICN, -1));
        nonTerminalConsumer.accept(this);
    }
}
