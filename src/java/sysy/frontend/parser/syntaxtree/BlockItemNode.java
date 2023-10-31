package sysy.frontend.parser.syntaxtree;

public abstract class BlockItemNode extends SyntaxNode {
    @Override
    public String getType() {
        return "BlockItem";
    }
}
