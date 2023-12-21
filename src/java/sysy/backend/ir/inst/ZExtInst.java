package sysy.backend.ir.inst;

import sysy.backend.ir.IRType;
import sysy.backend.ir.Value;

import java.io.PrintStream;

public class ZExtInst extends Instruction {
    private final IRType dstType;

    private Value value;

    public ZExtInst(IRType dstType, Value value) {
        super(dstType, value);
        this.dstType = dstType;
        this.value = value;
    }

    public Value getValue() {
        return value;
    }

    @Override
    public void dump(PrintStream out) {
        out.printf("  %s = zext %s to %s\n", getName(), value.toString(), dstType);
    }

    @Override
    public void replaceOperand(int pos, Value newOperand) {
        super.replaceOperand(pos, newOperand);
        if (pos == 0) {
            value = newOperand;
        }
    }
}
