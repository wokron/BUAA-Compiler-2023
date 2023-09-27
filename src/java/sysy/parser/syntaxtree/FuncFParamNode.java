package sysy.parser.syntaxtree;

import sysy.lexer.LexType;
import sysy.lexer.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class FuncFParamNode extends SyntaxNode {
    public BTypeNode type;
    public String ident;
    public List<ConstExpNode> dimensions;

    @Override
    public void walk(Consumer<Token> terminalConsumer, Consumer<SyntaxNode> nonTerminalConsumer) {
        type.walk(terminalConsumer, nonTerminalConsumer);

        terminalConsumer.accept(new Token(ident, LexType.IDENFR, -1));

        if (dimensions != null) {
            terminalConsumer.accept(new Token(null, LexType.LBRACK, -1));
            terminalConsumer.accept(new Token(ident, LexType.RBRACK, -1));
            for (var dim : dimensions) {
                terminalConsumer.accept(new Token(null, LexType.LBRACK, -1));
                dim.walk(terminalConsumer, nonTerminalConsumer);
                terminalConsumer.accept(new Token(ident, LexType.RBRACK, -1));
            }
        }

        nonTerminalConsumer.accept(this);
    }
}
