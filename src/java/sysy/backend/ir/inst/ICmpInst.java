package sysy.backend.ir.inst;

import sysy.backend.ir.IRType;
import sysy.backend.ir.Value;

import java.io.PrintStream;

public class ICmpInst extends Instruction {
    private final ICmpInstCond cond;
    private final Value left, right;

    public ICmpInst(ICmpInstCond cond, Value left, Value right, IRType type) {
        super(IRType.getBool());
        this.cond = cond;
        this.left = left;
        this.right = right;
        assert left.getType().equals(right.getType());
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
}
