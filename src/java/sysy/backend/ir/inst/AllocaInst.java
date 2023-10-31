package sysy.backend.ir.inst;

import sysy.backend.ir.IRType;

import java.io.PrintStream;

public class AllocaInst extends Instruction {
    IRType type;

    public AllocaInst(IRType type) {
        this.type = type;
    }

    public IRType getType() {
        return type;
    }

    @Override
    public void dump(PrintStream out) {
        out.printf("  %s = alloca %s\n", getName(), type.toString());
    }
}
