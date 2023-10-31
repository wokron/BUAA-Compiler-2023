package sysy.backend.ir.inst;

import sysy.backend.ir.IRType;
import sysy.backend.ir.Value;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class GetElementPtrInst extends Instruction {
    private final IRType type;
    private final Value elementBase;
    private final List<Value> offsets = new ArrayList<>();

    public GetElementPtrInst(IRType type, Value elementBase, List<Value> offsets) {
        this.type = type;
        this.elementBase = elementBase;
        this.offsets.addAll(offsets);
    }

    public List<Value> getOffsets() {
        return offsets;
    }

    public IRType getType() {
        return type;
    }

    @Override
    public void dump(PrintStream out) {
        out.printf("  %s = getelementptr %s, %s* %s",
                getName(),
                type,
                type,
                elementBase.getName());

        for (Value offset : offsets) {
            out.printf(", i32 %s", offset.getName());
        }
        out.print("\n");
    }
}
