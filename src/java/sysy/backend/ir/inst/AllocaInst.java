package sysy.backend.ir.inst;

import sysy.backend.ir.IRType;
import sysy.backend.ir.Value;

import java.io.PrintStream;

public class AllocaInst extends Instruction {

    public AllocaInst(IRType dataType) {
        super(dataType.clone().ptr(dataType.getPtrNum()+1)); // the type of alloca var is actually the address of the data
    }

    public IRType getDataType() {
        var dataType = getType().clone().ptr(getType().getPtrNum()-1); // remove pointer
        return dataType;
    }

    @Override
    public void dump(PrintStream out) {
        var dataType = getType().clone().ptr(getType().getPtrNum()-1); // remove pointer
        out.printf("  %s = alloca %s\n", getName(), dataType);
    }

    @Override
    public void replaceOperand(int pos, Value newOperand) {
        super.replaceOperand(pos, newOperand);
    }
}
