package sysy.backend.ir;

import java.io.PrintStream;

public abstract class Instruction extends Value {
    public void dump(PrintStream out) {
        out.printf("  %%%s = undefined\n", getName());
    }
}
