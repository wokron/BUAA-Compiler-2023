package sysy.backend.ir;

public class IRType {

    private final IRTypeEnum type;
    private int ptrNum;

    public IRType(IRTypeEnum type) {
        this.type = type;
    }

    public IRType(IRTypeEnum type, int ptrNum) {
        this(type);
        this.ptrNum = ptrNum;
    }

    public IRType ptr(int num) {
        ptrNum = num;
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

    @Override
    public String toString() {
        return type.toString() + "*".repeat(ptrNum);
    }
}
