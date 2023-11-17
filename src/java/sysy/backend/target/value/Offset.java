package sysy.backend.target.value;

public class Offset extends TargetValue {
    private final Register base;

    private int offset;

    public Offset(Register base, int offset) {
        this.base = base;
        this.offset = offset;
    }

    public int getOffset() {
        return offset;
    }

    public Register getBase() {
        return base;
    }

    @Override
    public String toString() {
        return offset + "(" + base + ")";
    }
}
