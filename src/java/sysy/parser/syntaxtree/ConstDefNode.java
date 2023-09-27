package sysy.parser.syntaxtree;

import java.util.ArrayList;
import java.util.List;

public class ConstDefNode extends SyntaxNode {
    public String ident;
    public List<ConstExpNode> dimensions = new ArrayList<>();
    public ConstInitValNode constInitVal;
}
