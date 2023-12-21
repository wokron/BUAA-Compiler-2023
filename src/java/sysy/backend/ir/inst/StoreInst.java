package sysy.backend.ir.inst;

import sysy.backend.ir.IRType;
import sysy.backend.ir.Value;

import java.io.PrintStream;

public class StoreInst extends Instruction {
    Value value;
    Value ptr;

    public StoreInst(Value value, Value ptr) {
        super(IRType.getVoid(), value, ptr);
        this.value = value;
        this.ptr = ptr;
    }

    public Value getValue() {
        return value;
    }

    public Value getPtr() {
        return ptr;
    }

    @Override
    public void dump(PrintStream out) {
        out.printf("  store %s, %s\n", value.toString(), ptr.toString());
    }

    @Override
    public void replaceOperand(int pos, Value newOperand) {
        super.replaceOperand(pos, newOperand);
        switch (pos) {
            case 0:
                value = newOperand;
                break;
            case 1:
                ptr = newOperand;
                break;
        }
    }
}
