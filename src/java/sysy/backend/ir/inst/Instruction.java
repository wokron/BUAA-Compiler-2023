package sysy.backend.ir.inst;

import sysy.backend.ir.Value;

import java.io.PrintStream;

public abstract class Instruction extends Value {
    @Override
    public String getName() {
        return "%" + super.getName();
    }

    public void dump(PrintStream out) {
        out.printf("  %%%s = undefined\n", getName());
    }
}
