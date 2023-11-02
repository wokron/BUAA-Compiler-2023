package sysy.backend.ir;

public abstract class Value {
    private String name = null;
    private final IRType type;

    public Value(IRType type) {
        this.type = type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        if (name == null) {
            name = NameAllocator.getInstance().alloc();
        }
        return name;
    }

    public IRType getType() {
        return type;
    }

    @Override
    public String toString() {
        return getType() + " " + getName();
    }
}
