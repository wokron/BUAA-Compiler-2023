package sysy.backend.ir;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class GlobalValue extends Value {
    private final IRType type;
    private final List<Integer> initVals = new ArrayList<>();

    public GlobalValue(IRType type, List<Integer> initVals) {
        this.type = type;
        this.initVals.addAll(initVals);
    }

    public IRType getType() {
        return type;
    }

    public List<Integer> getInitVals() {
        return initVals;
    }

    @Override
    public String getName() {
        return "@" + super.getName();
    }

    public void dump(PrintStream out) {
        out.printf("%s = dso_local global %s\n", getName(), type.initValsToString(initVals));
    }
}
