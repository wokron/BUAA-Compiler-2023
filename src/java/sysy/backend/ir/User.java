package sysy.backend.ir;

import java.util.ArrayList;
import java.util.List;

public abstract class User extends Value {
    protected final List<Value> operands = new ArrayList<>();

    public User(IRType type, Value... operands) {
        super(type);

        int pos = 0;
        for (var op : operands) {
            op.addUse(this, pos);
            this.operands.add(op);
            pos++;
        }
    }

    public void replaceOperand(int pos, Value newOperand) {
        operands.set(pos, newOperand);
    }
}
