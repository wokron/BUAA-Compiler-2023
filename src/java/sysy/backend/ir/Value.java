package sysy.backend.ir;

import java.util.ArrayList;
import java.util.List;

public abstract class Value {
    private String name = null;
    private final IRType type;
    private final List<Use> useList = new ArrayList<>();

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

    public void addUse(User user, int pos) {
        useList.add(new Use(user, this, pos));
    }

    public List<Use> getUseList() {
        return useList;
    }

    public IRType getType() {
        return type;
    }

    @Override
    public String toString() {
        return getType() + " " + getName();
    }
}
