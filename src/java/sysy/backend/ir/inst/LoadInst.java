package sysy.backend.ir.inst;

import sysy.backend.ir.Value;

import java.io.PrintStream;

public class LoadInst extends Instruction {
    Value ptr;

    public LoadInst(Value ptr) {
        super(ptr.getType().clone().ptr(ptr.getType().getPtrNum()-1));
        this.ptr = ptr;
    }

    @Override
    public void dump(PrintStream out) {
        var dataType = ptr.getType().clone().ptr(ptr.getType().getPtrNum()-1); // remove the pointer
        out.printf("  %s = load %s, %s\n", getName(), dataType.toString(), ptr.toString());
    }
}
