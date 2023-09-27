package sysy.parser.syntaxtree;

import java.util.ArrayList;
import java.util.List;

public class CompUnitNode extends SyntaxNode{
    public final List<DeclNode> declares = new ArrayList<>();
    public final List<FuncDefNode> funcs = new ArrayList<>();
    public MainFuncDefNode mainFunc;
}
