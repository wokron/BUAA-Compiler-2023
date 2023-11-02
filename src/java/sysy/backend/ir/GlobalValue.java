package sysy.backend.ir;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class GlobalValue extends Value {
    private final IRType dataType;
    private final List<Integer> initVals = new ArrayList<>();

    public GlobalValue(IRType dataType, List<Integer> initVals) {
        super(dataType.clone().ptr(dataType.getPtrNum()+1)); // the type of global var is actually the address of the data
        this.dataType = dataType;
        this.initVals.addAll(initVals);
    }

    public List<Integer> getInitVals() {
        return initVals;
    }

    @Override
    public String getName() {
        return "@" + super.getName();
    }

    public void dump(PrintStream out) {
        out.printf("%s = dso_local global %s\n", getName(), dataType.initValsToString(initVals));
    }
}
