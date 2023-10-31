package sysy.frontend.parser.syntaxtree.symbol;

import sysy.frontend.parser.syntaxtree.SyntaxNode;

public class NonTerminalSymbol {
    private final String type;
    private final SyntaxNode node;

    public NonTerminalSymbol(SyntaxNode node) {
        this.type = node.getType();
        this.node = node;
    }

    public String getType() {
        return type;
    }

    public SyntaxNode getNode() {
        return node;
    }

    @Override
    public String toString() {
        return "<%s>".formatted(type);
    }
}
