package sysy.frontend.parser.syntaxtree;

public abstract class StmtNode extends SyntaxNode {
    @Override
    public String getType() {
        return "Stmt";
    }
}
