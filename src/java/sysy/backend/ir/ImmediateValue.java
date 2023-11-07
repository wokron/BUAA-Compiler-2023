package sysy.backend.ir;

public class ImmediateValue extends Value {
    private final int value;

    public ImmediateValue(int value) {
        super(IRType.getInt());
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String getName() {
        return Integer.toString(value);
    }
}
