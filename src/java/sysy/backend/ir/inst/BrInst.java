package sysy.backend.ir.inst;

import sysy.backend.ir.BasicBlock;
import sysy.backend.ir.Value;

import java.io.PrintStream;

public class BrInst extends Instruction {
    private final Value cond;
    private BasicBlock trueBranch;
    private BasicBlock falseBranch;
    private final BasicBlock dest;

    public BrInst(Value cond, BasicBlock ifTrue, BasicBlock ifFalse) {
        this.cond = cond;
        this.trueBranch = ifTrue;
        this.falseBranch = ifFalse;
        this.dest = null;
    }

    public BrInst(BasicBlock dest) {
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

    @Override
    public void dump(PrintStream out) {
        if (cond != null) {
            out.printf("  br i1 %s, label %s, label %s\n",
                    cond.getName(),
                    trueBranch.getName(),
                    falseBranch.getName());
        } else {
            out.printf("  br label %s\n", dest.getName());
        }
    }
}