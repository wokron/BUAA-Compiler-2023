package sysy.backend.ir.inst;

import sysy.backend.ir.IRType;
import sysy.backend.ir.Value;

import java.io.PrintStream;

public class GetElementPtrInst extends Instruction {
    private final IRType type;
    private final Value elementBase;
    private final Value offset;

    public GetElementPtrInst(IRType type, Value elementBase, Value offset) {
        this.type = type;
        this.elementBase = elementBase;
        this.offset = offset;
    }

    @Override
    public void dump(PrintStream out) {
        out.printf("  %s = getelementptr %s, %s* %s, i32 0, i32 %s\n",
                getName(),
                type.toString(),
                type.toString(),
                elementBase.getName(),
                offset.getName());
    }
}
