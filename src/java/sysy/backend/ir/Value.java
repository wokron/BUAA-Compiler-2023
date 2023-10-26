package sysy.backend.ir;

public abstract class Value {
    private String name = null;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        if (name == null) {
            name = NameAllocator.getInstance().alloc();
        }
        return name;
    }
}
