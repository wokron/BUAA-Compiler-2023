package sysy.backend.ir;

public class ImmediateValue extends Value {
    private final int value;

    public ImmediateValue(int value) {
        this.value = value;
    }

    @Override
    public String getName() {
        return Integer.toString(value);
    }
}
