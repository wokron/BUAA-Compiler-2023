package sysy.parser.syntaxtree;

import sysy.lexer.LexType;
import sysy.lexer.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ConstDefNode extends SyntaxNode {
    public String ident;
    public List<ConstExpNode> dimensions = new ArrayList<>();
    public ConstInitValNode constInitVal;

    @Override
    public void walk(Consumer<Token> terminalConsumer, Consumer<SyntaxNode> nonTerminalConsumer) {
        terminalConsumer.accept(new Token(ident, LexType.IDENFR, -1));

        for (var dimension : dimensions) {
            terminalConsumer.accept(new Token(null, LexType.LBRACK, -1));

            dimension.walk(terminalConsumer, nonTerminalConsumer);

            terminalConsumer.accept(new Token(null, LexType.RBRACK, -1));
        }

        terminalConsumer.accept(new Token(null, LexType.ASSIGN, -1));

        constInitVal.walk(terminalConsumer, nonTerminalConsumer);

        nonTerminalConsumer.accept(this);
    }
}
