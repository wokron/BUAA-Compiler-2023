package sysy.backend.ir.inst;

import sysy.backend.ir.IRType;
import sysy.backend.ir.Value;

import java.io.PrintStream;

public class StoreInst extends Instruction {
    Value value;
    Value ptr;

    public StoreInst(Value value, Value ptr) {
        super(IRType.getVoid());
        this.value = value;
        this.ptr = ptr;
    }

    @Override
    public void dump(PrintStream out) {
        out.printf("  store %s, %s\n", value.toString(), ptr.toString());
    }
}
