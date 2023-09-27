package sysy.parser.syntaxtree;

import java.util.ArrayList;
import java.util.List;

public class ConstDeclNode extends SyntaxNode {
    public BTypeNode type;
    public List<ConstDefNode> constDefs = new ArrayList<>();
}
