package sysy.backend.ir;

import java.util.ArrayList;
import java.util.List;

public abstract class User extends Value {
    protected final List<Value> operands = new ArrayList<>();

    public User(IRType type, Value... operands) {
        super(type);

        for (var op : operands) {
            op.addUse(this);
            this.operands.add(op);
        }
    }

}
