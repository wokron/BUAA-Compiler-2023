package sysy.backend.ir;

import java.io.PrintStream;

public class FunctionArgument extends Value {

    public FunctionArgument(IRType type) {
        super(type);
    }

    @Override
    public String getName() {
        return "%" + super.getName();
    }

    public void dump(PrintStream out) {
        out.printf("%s %s", getType().toString(), getName());
    }
}
