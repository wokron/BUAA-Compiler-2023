package sysy.parser.syntaxtree;

public abstract class UnaryExpNode extends SyntaxNode {
    @Override
    public String getType() {
        return "UnaryExp";
    }
}
