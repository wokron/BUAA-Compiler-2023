package sysy.backend.ir.inst;

import sysy.backend.ir.IRType;

import java.io.PrintStream;

public class AllocaInst extends Instruction {
    IRType dataType;

    public AllocaInst(IRType dataType) {
        super(dataType.clone().ptr(dataType.getPtrNum()+1)); // the type of alloca var is actually the address of the data
        this.dataType = dataType;
    }

    public IRType getDataType() {
        return dataType;
    }

    @Override
    public void dump(PrintStream out) {
        out.printf("  %s = alloca %s\n", getName(), dataType.toString());
    }
}
