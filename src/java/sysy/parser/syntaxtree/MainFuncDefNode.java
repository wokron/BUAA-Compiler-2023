package sysy.parser.syntaxtree;

import sysy.lexer.Token;

import java.util.function.Consumer;

public class MainFuncDefNode extends SyntaxNode {
    public BlockNode mainBlock;

    @Override
    public void walk(Consumer<Token> terminalConsumer, Consumer<SyntaxNode> nonTerminalConsumer) {
        mainBlock.walk(terminalConsumer, nonTerminalConsumer);

        nonTerminalConsumer.accept(this);
    }
}
