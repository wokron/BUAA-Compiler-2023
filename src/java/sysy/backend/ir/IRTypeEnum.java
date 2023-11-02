package sysy.backend.ir;

public enum IRTypeEnum {
    INT("i32"),
    VOID("void"),
    CHAR("i8"),
    BOOL("i1"),
    ARRAY("[]"),
    LABEL("label");

    private final String value;

    IRTypeEnum(String s) {
        value = s;
    }

    @Override
    public String toString() {
        return value;
    }
}
