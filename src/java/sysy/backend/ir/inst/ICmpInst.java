package sysy.backend.ir.inst;

import sysy.backend.ir.IRType;
import sysy.backend.ir.Value;

import java.io.PrintStream;

public class ICmpInst extends Instruction {
    private final ICmpInstCond cond;
    private final Value left, right;
    private final IRType type;

    public ICmpInst(ICmpInstCond cond, Value left, Value right, IRType type) {
        this.cond = cond;
        this.left = left;
        this.right = right;
        this.type = type;
    }

    @Override
    public void dump(PrintStream out) {
        out.printf("  %s = %s %s %s, %s\n",
                getName(),
                cond.name().toLowerCase(),
                type.toString(),
                left.getName(),
                right.getName());
    }
}
