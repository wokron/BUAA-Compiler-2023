package sysy.parser.syntaxtree;

import sysy.lexer.LexType;

public class AddExpNodeForDouble extends AddExpNode {
    public AddExpNode addExp;
    public LexType op;
    public MulExpNode mulExp;
}
