package sysy.parser.syntaxtree;

import sysy.lexer.LexType;

public class MulExpNodeForDouble extends MulExpNode {
    public MulExpNode mulExp;
    public LexType op;
    public UnaryExpNode unaryExp;
}
