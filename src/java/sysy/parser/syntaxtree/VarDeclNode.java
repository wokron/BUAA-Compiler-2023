package sysy.parser.syntaxtree;

import java.util.ArrayList;
import java.util.List;

public class VarDeclNode extends SyntaxNode {
    public BTypeNode type;
    public List<VarDefNode> varDefs;
}
