package sysy.backend.ir.inst;

import sysy.backend.ir.IRType;
import sysy.backend.ir.Value;

import java.io.PrintStream;

public class ZExtInst extends Instruction {
    private final IRType dstType;

    private final Value value;

    public ZExtInst(IRType dstType, Value value) {
        super(dstType);
        this.dstType = dstType;
        this.value = value;
    }

    @Override
    public void dump(PrintStream out) {
        out.printf("  %s = zext %s to %s\n", getName(), value.toString(), dstType);
    }
}
