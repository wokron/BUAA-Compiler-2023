package sysy.parser.syntaxtree;

import sysy.lexer.Token;

import java.util.function.Consumer;

public class BlockItemNodeForDecl extends BlockItemNode {
    public DeclNode decl;

    @Override
    public void walk(Consumer<Token> terminalConsumer, Consumer<SyntaxNode> nonTerminalConsumer) {
        decl.walk(terminalConsumer, nonTerminalConsumer);
        nonTerminalConsumer.accept(this);
    }
}
