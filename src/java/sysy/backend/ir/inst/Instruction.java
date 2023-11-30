package sysy.backend.ir.inst;

import sysy.backend.ir.BasicBlock;
import sysy.backend.ir.IRType;
import sysy.backend.ir.User;
import sysy.backend.ir.Value;

import java.io.PrintStream;

public abstract class Instruction extends User {
    private BasicBlock basicBlock;

    public Instruction(IRType type, Value... operands) {
        super(type, operands);
    }

    public BasicBlock getBasicBlock() {
        return basicBlock;
    }

    public void setBasicBlock(BasicBlock basicBlock) {
        this.basicBlock = basicBlock;
    }

    public void replaceAllUseWith(Value newValue, boolean needInsert) {
        var idx = basicBlock.getInstructions().indexOf(this);
        if (idx < 0) {
            throw new RuntimeException(); // impossible
        }
        if (needInsert) {
            if (newValue instanceof Instruction newInst) {
                basicBlock.getInstructions().set(idx, newInst);
            } else {
                throw new RuntimeException();
            }
        } else {
            basicBlock.getInstructions().remove(idx);
        }

        for (var use : getUseList()) {
            use.getUser().replaceOperand(use.getPos(), newValue);
        }
    }

    @Override
    public String getName() {
        return "%t" + super.getName();
    }

    public void dump(PrintStream out) {
        out.printf("  %%%s = undefined\n", getName());
    }
}
