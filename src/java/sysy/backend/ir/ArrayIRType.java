package sysy.backend.ir;

import java.util.ArrayList;
import java.util.List;

public class ArrayIRType extends IRType {
    private final BasicIRType elmType;
    private final List<Integer> arrayDims = new ArrayList<>();

    public ArrayIRType(BasicIRType type, List<Integer> arrayDims) {
        this.elmType = type;
        this.arrayDims.addAll(arrayDims);
    }

    public int getTotalSize() {
        int total = 1;
        for (var dim : arrayDims) {
            total *= dim;
        }
        return total;
    }

    @Override
    public ArrayIRType ptr(int num) {
        super.ptr(num);
        return this;
    }

    public BasicIRType getElmType() {
        return elmType;
    }

    @Override
    public List<Integer> getArrayDims() {
        return arrayDims;
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
        sb.append(elmType);
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
                sb.append(elmType).append(" ").append(initVals.get(i));
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
            sb.append(String.format("[%d x ", dim));
        }
        sb.append(elmType.toString());

        sb.append("]".repeat(arrayDims.size()));

        sb.append("*".repeat(getPtrNum()));
        return sb.toString();
    }

    @Override
    public IRTypeEnum getType() {
        return IRTypeEnum.ARRAY;
    }

    @Override
    public String toString() {
        return typeToString();
    }

    @Override
    public ArrayIRType clone() {
        var obj = new ArrayIRType(this.elmType, this.arrayDims);
        obj.ptr(obj.getPtrNum());
        return obj;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ArrayIRType other) {
            return this.elmType.equals(other.elmType) && this.arrayDims.equals(other.arrayDims) && this.getPtrNum() == other.getPtrNum();
        } else {
            return false;
        }
    }
}
