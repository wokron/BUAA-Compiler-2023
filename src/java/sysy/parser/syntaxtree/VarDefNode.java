package sysy.parser.syntaxtree;

import sysy.lexer.LexType;
import sysy.lexer.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class VarDefNode extends SyntaxNode {
    public String ident;
    public List<ConstExpNode> dimensions = new ArrayList<>();
    public InitValNode initVal;

    @Override
    public void walk(Consumer<Token> terminalConsumer, Consumer<SyntaxNode> nonTerminalConsumer) {
        terminalConsumer.accept(new Token(ident, LexType.IDENFR, -1));

        for (var dim : dimensions) {
            terminalConsumer.accept(new Token(null, LexType.LBRACK, -1));

            dim.walk(terminalConsumer, nonTerminalConsumer);

            terminalConsumer.accept(new Token(null, LexType.RBRACK, -1));
        }

        if (initVal != null) {
            terminalConsumer.accept(new Token(null, LexType.ASSIGN, -1));
            initVal.walk(terminalConsumer, nonTerminalConsumer);
        }

        nonTerminalConsumer.accept(this);
    }
}
