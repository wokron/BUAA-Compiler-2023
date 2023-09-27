package sysy.parser.syntaxtree;

import sysy.lexer.Token;

import java.util.function.Consumer;

public class InitValNodeForExp extends InitValNode {
    public ExpNode exp;

    @Override
    public void walk(Consumer<Token> terminalConsumer, Consumer<SyntaxNode> nonTerminalConsumer) {
        exp.walk(terminalConsumer, nonTerminalConsumer);

        nonTerminalConsumer.accept(this);
    }
}
