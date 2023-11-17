package sysy.backend.ir.inst;

import sysy.backend.ir.IRType;
import sysy.backend.ir.Value;

import java.io.PrintStream;

public class ReturnInst extends Instruction {
    private final Value value;

    public ReturnInst(Value value) {
        super(value.getType(), value);
        this.value = value;
    }

    public ReturnInst() {
        super(IRType.getVoid());
        this.value = null;
    }

    public Value getValue() {
        return value;
    }

    @Override
    public void dump(PrintStream out) {
        out.print("  ret ");
        if (value == null) {
            out.print("void");
        } else {
            out.printf("%s", value);
        }
        out.print("\n");
    }
}
