package sysy.frontend.parser.syntaxtree;

public abstract class PrimaryExpNode extends SyntaxNode {
    @Override
    public String getType() {
        return "PrimaryExp";
    }
}
