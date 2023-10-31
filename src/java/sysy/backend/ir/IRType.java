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
