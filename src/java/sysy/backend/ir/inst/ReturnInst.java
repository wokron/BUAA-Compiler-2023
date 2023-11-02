package sysy.backend.ir.inst;

import sysy.backend.ir.IRType;
import sysy.backend.ir.Value;

import java.io.PrintStream;

public class ReturnInst extends Instruction {
    private final Value value;

    public ReturnInst(IRType type, Value value) {
        super(value == null ? IRType.getVoid() : value.getType());
        this.value = value;
    }

    @Override
    public void dump(PrintStream out) {
        out.print("  ret ");
        if (value == null) {
            out.print("void");
        } else {
            out.printf("%s", value.toString());
        }
        out.print("\n");
    }
}
