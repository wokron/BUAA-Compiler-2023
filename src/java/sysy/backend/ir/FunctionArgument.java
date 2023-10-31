package sysy.backend.ir;

import java.io.PrintStream;

public class FunctionArgument extends Value {
    private final IRType type;

    public FunctionArgument(IRType type) {
        this.type = type;
    }

    public IRType getType() {
        return type;
    }

    @Override
    public String getName() {
        return "%" + super.getName();
    }

    public void dump(PrintStream out) {
        out.printf("%s %s", type.toString(), getName());
    }
}
