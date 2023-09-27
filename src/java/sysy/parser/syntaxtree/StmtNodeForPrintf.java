package sysy.parser.syntaxtree;

import java.util.ArrayList;
import java.util.List;

public class StmtNodeForPrintf extends StmtNode {
    public String formatString;
    public List<ExpNode> exps = new ArrayList<>();
}
