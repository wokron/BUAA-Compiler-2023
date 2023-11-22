package sysy.backend.ir.inst;

import sysy.backend.ir.BasicBlock;
import sysy.backend.ir.IRType;
import sysy.backend.ir.Value;

import java.io.PrintStream;

public class BrInst extends Instruction {
    private Value cond;
    private BasicBlock trueBranch;
    private BasicBlock falseBranch;
    private BasicBlock dest;

    public BrInst(Value cond, BasicBlock ifTrue, BasicBlock ifFalse) {
        super(IRType.getVoid(), cond); // TODO: basic block is used as well
        this.cond = cond;
        this.trueBranch = ifTrue;
        this.falseBranch = ifFalse;
        this.dest = null;
    }

    public BrInst(BasicBlock dest) {
        super(IRType.getVoid()); // TODO: basic block is used as well
        this.dest = dest;
        this.cond = null;
        this.trueBranch = null;
        this.falseBranch = null;
    }

    public void setTrueBranch(BasicBlock trueBranch) {
        this.trueBranch = trueBranch;
    }

    public void setFalseBranch(BasicBlock falseBranch) {
        this.falseBranch = falseBranch;
    }

    public void setDest(BasicBlock dest) {
        this.dest = dest;
    }

    public Value getCond() {
        return cond;
    }

    public BasicBlock getTrueBranch() {
        return trueBranch;
    }

    public BasicBlock getFalseBranch() {
        return falseBranch;
    }

    public BasicBlock getDest() {
        return dest;
    }

    @Override
    public void dump(PrintStream out) {
        if (cond != null) {
            out.printf("  br %s, %s, %s\n",
                    cond,
                    trueBranch.toString(),
                    falseBranch.toString());
        } else {
            out.printf("  br %s\n", dest.toString());
        }
    }

    @Override
    public void replaceOperand(int pos, Value newOperand) {
        super.replaceOperand(pos, newOperand);
        if (pos == 0) {
            cond = newOperand;
        }
    }
}
