package sysy.backend.ir;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class GlobalValue extends Value {
    private final IRType type;
    private final List<Integer> initVals = new ArrayList<>();
    private final List<Integer> dims = new ArrayList<>();

    public GlobalValue(IRType type, List<Integer> initVals, List<Integer> dims) {
        this.type = type;
        this.initVals.addAll(initVals);
        this.dims.addAll(dims);
    }

    public IRType getType() {
        return type;
    }

    public List<Integer> getInitVals() {
        return initVals;
    }

    public List<Integer> getDims() {
        return dims;
    }

    @Override
    public String getName() {
        return "@" + super.getName();
    }

    public void dump(PrintStream out) {
        if (dims.isEmpty()) {
            out.printf("%s = dso_local global %s %d\n", getName(), type.toString(), !initVals.isEmpty() ? initVals.get(0) : 0);
        } else {
            // TODO: array global var
        }
    }
}
