package sysy.backend.ir.inst;

import sysy.backend.ir.IRType;
import sysy.backend.ir.Value;

import java.io.PrintStream;

public class ZExtInst extends Instruction {
    private final IRType dstType;
    private final IRType srcType;

    private final Value value;

    public ZExtInst(IRType dstType, IRType srcType, Value value) {
        this.dstType = dstType;
        this.srcType = srcType;
        this.value = value;
    }

    @Override
    public void dump(PrintStream out) {
        out.printf("  %s = zext %s %s to %s\n", getName(), srcType, value.getName(), dstType);
    }
}
