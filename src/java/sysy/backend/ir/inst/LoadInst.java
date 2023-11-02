package sysy.backend.ir.inst;

import sysy.backend.ir.IRType;
import sysy.backend.ir.Value;

import java.io.PrintStream;

public class LoadInst extends Instruction {
    IRType dataType;
    Value ptr;

    public LoadInst(IRType dataType, Value ptr) {
        super(dataType.clone());
        this.dataType = dataType;
        this.ptr = ptr;
    }

    public IRType getDataType() {
        return dataType;
    }

    @Override
    public void dump(PrintStream out) {
        out.printf("  %s = load %s, %s* %s\n", getName(), dataType.toString(), dataType.toString(), ptr.getName());
    }
}
