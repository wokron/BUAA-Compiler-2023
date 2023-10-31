package sysy.backend.ir;

import java.util.ArrayList;
import java.util.List;

public class IRType {

    private final IRTypeEnum type;
    private int ptrNum;
    private final List<Integer> arrayDims = new ArrayList<>();

    public IRType(IRTypeEnum type) {
        this.type = type;
    }

    public IRType(IRTypeEnum type, int ptrNum) {
        this(type);
        this.ptrNum = ptrNum;
    }

    public IRType(IRTypeEnum type, int ptrNum, List<Integer> arrayDims) {
        this(type, ptrNum);
        this.arrayDims.addAll(arrayDims);
    }

    public IRType ptr(int num) {
        ptrNum = num;
        return this;
    }

    public IRType dims(List<Integer> dims) {
        arrayDims.addAll(dims);
        return this;
    }

    public IRTypeEnum getType() {
        return type;
    }

    public static IRType getInt() {
        return new IRType(IRTypeEnum.INT);
    }

    public static IRType getVoid() {
        return new IRType(IRTypeEnum.VOID);
    }

    public static IRType getChar() {
        return new IRType(IRTypeEnum.CHAR);
    }

    public String initValsToString(List<Integer> initVals) {
        return initValsToString(this.arrayDims, initVals);
    }

    private boolean isAllZero(List<Integer> vals) {
        for (var val : vals) {
            if (val != 0) {
                return false;
            }
        }
        return true;
    }

    public String initValsToString(List<Integer> dims, List<Integer> initVals) {
        StringBuilder sb = new StringBuilder();
        for (var dim : dims) {
            sb.append("[").append(dim).append(" x ");
        }
        sb.append(type.toString()).append("*".repeat(ptrNum));
        sb.append("]".repeat(dims.size())).append(" ");
        if (isAllZero(initVals)) {
            sb.append("zeroinitializer");
            return sb.toString();
        }
        if (dims.isEmpty()) {
            sb.append(initVals.isEmpty() ? 0 : initVals.get(0));
            return sb.toString();
        } else if (dims.size() == 1) {
            sb.append("[");
            for (int i = 0; i < dims.get(0); i++) {
                if (i != 0) {
                    sb.append(", ");
                }
                sb.append(type.toString()).append("*".repeat(ptrNum)).append(" ").append(initVals.get(i));
            }
            sb.append("]");
            return sb.toString();
        } else {
            sb.append("[");
            int stride = 1;
            for (int i = 1; i < dims.size(); i++) {
                stride *= dims.get(i);
            }

            for (int i = 0; i < dims.get(0); i++) {
                if (i != 0) {
                    sb.append(", ");
                }
                var nextDim = dims.subList(1, dims.size());
                var nextInitVals = initVals.subList(stride * i, stride * (i + 1));
                sb.append(initValsToString(nextDim, nextInitVals));
            }
            sb.append("]");

            return sb.toString();
        }
    }

    private String typeToString() {
        StringBuilder sb = new StringBuilder();
        for (var dim : arrayDims) {
            if (dim == null) {
                continue;
            }
            sb.append(String.format("[%d x ", dim));
        }
        sb.append(type.toString()).append("*".repeat(ptrNum));

        if (!arrayDims.isEmpty() && arrayDims.get(0) == null) {
            sb.append("]".repeat(arrayDims.size()-1));
            sb.append("*");
        } else {
            sb.append("]".repeat(arrayDims.size()));
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return typeToString();
    }
}
