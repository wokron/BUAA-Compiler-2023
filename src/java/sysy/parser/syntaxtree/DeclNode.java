package sysy.parser.syntaxtree;

public abstract class DeclNode extends SyntaxNode {
    @Override
    public String getType() {
        return "Decl";
    }
}
