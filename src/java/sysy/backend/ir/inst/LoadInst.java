package sysy.backend.ir.inst;

import sysy.backend.ir.IRType;
import sysy.backend.ir.Value;

import java.io.PrintStream;

public class LoadInst extends Instruction {
    IRType type;
    Value ptr;

    public LoadInst(IRType type, Value ptr) {
        this.type = type;
        this.ptr = ptr;
    }

    public IRType getType() {
        return type;
    }

    @Override
    public void dump(PrintStream out) {
        out.printf("  %s = load %s, %s* %s\n", getName(), type.toString(), type.toString(), ptr.getName());
    }
}
