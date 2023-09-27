package sysy.parser.syntaxtree;

import sysy.lexer.LexType;
import sysy.lexer.Token;

import java.util.function.Consumer;

public class FuncDefNode extends SyntaxNode {
    public FuncTypeNode funcType;
    public String ident;
    public FuncFParamsNode params;
    public BlockNode block;

    @Override
    public void walk(Consumer<Token> terminalConsumer, Consumer<SyntaxNode> nonTerminalConsumer) {
        funcType.walk(terminalConsumer, nonTerminalConsumer);

        terminalConsumer.accept(new Token(ident, LexType.IDENFR, -1));

        terminalConsumer.accept(new Token(null, LexType.LPARENT, -1));

        if (params != null) {
            params.walk(terminalConsumer, nonTerminalConsumer);
        }

        terminalConsumer.accept(new Token(null, LexType.RPARENT, -1));

        block.walk(terminalConsumer, nonTerminalConsumer);

        nonTerminalConsumer.accept(this);
    }
}
