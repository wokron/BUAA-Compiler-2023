package sysy.backend.ir.inst;

import sysy.backend.ir.IRType;
import sysy.backend.ir.Value;

import java.io.PrintStream;

public class StoreInst extends Instruction {
    IRType type;
    Value value;
    Value ptr;

    public StoreInst(IRType type, Value value, Value ptr) {
        this.type = type;
        this.value = value;
        this.ptr = ptr;
    }

    @Override
    public void dump(PrintStream out) {
        out.printf("  store %s %s, %s* %s\n", type.toString(), value.getName(), type.toString(), ptr.getName());
    }
}
