package sysy.parser.syntaxtree;

import sysy.lexer.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class BlockNode extends SyntaxNode {
    public List<BlockItemNode> blockItems = new ArrayList<>();

    @Override
    public void walk(Consumer<Token> terminalConsumer, Consumer<SyntaxNode> nonTerminalConsumer) {
        for (var blockItem : blockItems) {
            blockItem.walk(terminalConsumer, nonTerminalConsumer);
        }
        nonTerminalConsumer.accept(this);
    }
}
