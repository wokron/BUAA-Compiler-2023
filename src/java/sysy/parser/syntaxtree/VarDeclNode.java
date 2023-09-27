package sysy.parser.syntaxtree;

import sysy.lexer.LexType;
import sysy.lexer.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class VarDeclNode extends SyntaxNode {
    public BTypeNode type;
    public List<VarDefNode> varDefs = new ArrayList<>();

    @Override
    public void walk(Consumer<Token> terminalConsumer, Consumer<SyntaxNode> nonTerminalConsumer) {
        type.walk(terminalConsumer, nonTerminalConsumer);

        boolean first = true;
        for (var varDef : varDefs) {
            if (first) {
                first = false;
            } else {
                terminalConsumer.accept(new Token(null, LexType.COMMA, -1));
            }
            varDef.walk(terminalConsumer, nonTerminalConsumer);
        }
        terminalConsumer.accept(new Token(null, LexType.SEMICN, -1));

        nonTerminalConsumer.accept(this);
    }
}
