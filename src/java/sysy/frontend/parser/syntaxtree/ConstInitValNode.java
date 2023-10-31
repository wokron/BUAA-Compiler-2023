package sysy.frontend.parser.syntaxtree;

public abstract class ConstInitValNode extends SyntaxNode {
    @Override
    public String getType() {
        return "ConstInitVal";
    }
}
