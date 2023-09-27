package sysy.parser.syntaxtree;

import sysy.lexer.Token;

import java.util.function.Consumer;

public class BlockItemNodeForStmt extends BlockItemNode {
    public StmtNode stmt;

    @Override
    public void walk(Consumer<Token> terminalConsumer, Consumer<SyntaxNode> nonTerminalConsumer) {
        stmt.walk(terminalConsumer, nonTerminalConsumer);
        nonTerminalConsumer.accept(this);
    }
}
