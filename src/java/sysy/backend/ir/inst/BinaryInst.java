package sysy.backend.ir.inst;

import sysy.backend.ir.IRType;
import sysy.backend.ir.Value;

import java.io.PrintStream;

public class BinaryInst extends Instruction {
    private final BinaryInstOp op;
    private final Value left, right;

    public BinaryInst(BinaryInstOp op, Value left, Value right, IRType type) {
        super(left.getType());
        assert left.getType().equals(right.getType());

        this.op = op;
        this.left = left;
        this.right = right;
    }

    @Override
    public void dump(PrintStream out) {
        out.printf("  %s = %s %s %s, %s\n",
                getName(),
                op.name().toLowerCase(),
                getType().toString(),
                left.getName(),
                right.getName());
    }
}
