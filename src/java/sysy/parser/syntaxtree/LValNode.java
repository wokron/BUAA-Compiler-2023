package sysy.parser.syntaxtree;

import java.util.ArrayList;
import java.util.List;

public class LValNode extends SyntaxNode {
    public String ident;
    public List<ExpNode> dimensions = new ArrayList<>();
}
