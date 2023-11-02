package sysy.backend.ir;

import java.util.List;

public class BasicIRType extends IRType {
    private final IRTypeEnum type;

    public BasicIRType(IRTypeEnum type) {
        this.type = type;
    }

    @Override
    public BasicIRType ptr(int num) {
        super.ptr(num);
        return this;
    }

    @Override
    public IRTypeEnum getType() {
        return type;
    }

    public ArrayIRType dims(List<Integer> arrayDims) {
        return new ArrayIRType(this, arrayDims);
    }

    @Override
    public String initValsToString(List<Integer> initVals) {
        String sb = this + " " +
                (initVals.isEmpty() ? 0 : initVals.get(0));
        return sb;
    }


    @Override
    public String toString() {
        return type.toString() + "*".repeat(getPtrNum());
    }
}
