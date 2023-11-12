package sysy.backend.target.value;

public class Immediate extends TargetValue {
    private final int value;

    public Immediate(int value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }
}
