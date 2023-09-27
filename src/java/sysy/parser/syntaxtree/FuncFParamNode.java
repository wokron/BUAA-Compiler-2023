package sysy.parser.syntaxtree;

import java.util.ArrayList;
import java.util.List;

public class FuncFParamNode extends SyntaxNode {
    public BTypeNode type;
    public String ident;
    public List<ConstExpNode> dimensions = new ArrayList<>();
}
