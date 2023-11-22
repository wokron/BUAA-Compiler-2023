package sysy.backend.ir.inst;

import sysy.backend.ir.Value;

import java.io.PrintStream;

public class BinaryInst extends Instruction {
    private final BinaryInstOp op;
    private Value left;
    private Value right;

    public BinaryInst(BinaryInstOp op, Value left, Value right) {
        super(left.getType(), left, right);
        assert left.getType().equals(right.getType());

        this.op = op;
        this.left = left;
        this.right = right;
    }

    public BinaryInstOp getOp() {
        return op;
    }

    public Value getLeft() {
        return left;
    }

    public Value getRight() {
        return right;
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
