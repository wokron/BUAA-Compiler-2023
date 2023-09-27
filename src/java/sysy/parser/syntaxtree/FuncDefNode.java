package sysy.parser.syntaxtree;

public class FuncDefNode extends SyntaxNode {
    public FuncTypeNode funcType;
    public String ident;
    public FuncFParamsNode params;
    public BlockNode block;
}
