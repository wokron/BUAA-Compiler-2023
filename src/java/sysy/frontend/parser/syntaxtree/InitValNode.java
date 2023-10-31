package sysy.frontend.parser.syntaxtree;

public abstract class InitValNode extends SyntaxNode {
    @Override
    public String getType() {
        return "InitVal";
    }
}
