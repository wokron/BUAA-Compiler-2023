package sysy.backend.ir.inst;

import sysy.backend.ir.IRType;
import sysy.backend.ir.Value;

import java.io.PrintStream;

public class ReturnInst extends Instruction {
    private IRType type;
    private Value value;

    public ReturnInst(IRType type, Value value) {
        this.type = type;
        this.value = value;
    }

    @Override
    public void dump(PrintStream out) {
        out.printf("  ret %s%s\n", type.toString(), value == null ? "" : " " + value.getName());
    }
}
