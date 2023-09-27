package sysy.parser.syntaxtree;

import sysy.lexer.LexType;
import sysy.lexer.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class StmtNodeForPrintf extends StmtNode {
    public String formatString;
    public List<ExpNode> exps = new ArrayList<>();

    @Override
    public void walk(Consumer<Token> terminalConsumer, Consumer<SyntaxNode> nonTerminalConsumer) {
        terminalConsumer.accept(new Token(null, LexType.PRINTFTK, -1));
        terminalConsumer.accept(new Token(null, LexType.LPARENT, -1));

        terminalConsumer.accept(new Token(formatString, LexType.STRCON, -1));

        for (var exp : exps) {
            terminalConsumer.accept(new Token(null, LexType.COMMA, -1));
            exp.walk(terminalConsumer, nonTerminalConsumer);
        }

        terminalConsumer.accept(new Token(null, LexType.RPARENT, -1));
        terminalConsumer.accept(new Token(null, LexType.SEMICN, -1));

        nonTerminalConsumer.accept(this);
    }
}
