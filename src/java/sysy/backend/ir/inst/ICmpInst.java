package sysy.backend.ir.inst;

import sysy.backend.ir.IRType;
import sysy.backend.ir.Value;

import java.io.PrintStream;

public class ICmpInst extends Instruction {
    private final ICmpInstCond cond;
    private Value left;
    private Value right;

    public ICmpInst(ICmpInstCond cond, Value left, Value right) {
        super(IRType.getBool(), left, right);
        this.cond = cond;
        this.left = left;
        this.right = right;
        assert left.getType().equals(right.getType());
    }

    public ICmpInstCond getCond() {
        return cond;
    }

    public Value getLeft() {
        return left;
    }

    public Value getRight() {
        return right;
    }

    @Override
    public void dump(PrintStream out) {
        var type = left.getType();
        out.printf("  %s = icmp %s %s %s, %s\n",
                getName(),
                cond.name().toLowerCase(),
                type.toString(),
                left.getName(),
                right.getName());
    }

    @Override
    public void replaceOperand(int pos, Value newOperand) {
        super.replaceOperand(pos, newOperand);
        switch (pos) {
            case 0:
                left = newOperand;
                break;
            case 1:
                right = newOperand;
                break;
        }
    }
}
