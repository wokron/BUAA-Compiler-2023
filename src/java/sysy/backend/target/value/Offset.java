package sysy.backend.target.value;

public class Offset extends TargetValue {
    private final Register base;

    private int offset;

    public Offset(Register base, int offset) {
        this.base = base;
        this.offset = offset;
    }

    @Override
    public String toString() {
        return offset + "(" + base + ")";
    }
}
