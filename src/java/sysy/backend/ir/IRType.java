package sysy.backend.ir;

import java.util.ArrayList;
import java.util.List;

public abstract class IRType implements Cloneable {
    private int ptrNum = 0;

    public IRType ptr(int num) {
        this.ptrNum = num;
        return this;
    }

    public int getPtrNum() {
        return ptrNum;
    }

    public static BasicIRType getInt() {
        return new BasicIRType(IRTypeEnum.INT);
    }

    public static BasicIRType getVoid() {
        return new BasicIRType(IRTypeEnum.VOID);
    }

    public static BasicIRType getChar() {
        return new BasicIRType(IRTypeEnum.CHAR);
    }

    public static BasicIRType getBool() {
        return new BasicIRType(IRTypeEnum.BOOL);
    }

    public List<Integer> getArrayDims() {
        return new ArrayList<>();
    }

    public abstract String initValsToString(List<Integer> initVals);

    public abstract IRTypeEnum getType();

    @Override
    public abstract IRType clone();
}
