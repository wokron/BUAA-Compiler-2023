package sysy.backend.optim;

import sysy.backend.ir.BasicBlock;
import sysy.backend.ir.Function;
import sysy.backend.ir.ImmediateValue;
import sysy.backend.ir.Module;
import sysy.backend.ir.inst.BinaryInst;
import sysy.backend.ir.inst.BinaryInstOp;
import sysy.backend.ir.inst.Instruction;

public class PeepHolePass {
    private final Module irModule;

    public PeepHolePass(Module irModule) {
        this.irModule = irModule;
    }

    public Module pass() {
        for (var func : irModule.getFunctions()) {
            passFunc(func);
        }

        return irModule;
    }

    private void passFunc(Function func) {
        for (var block : func.getBasicBlocks()) {
            passBlock(block);
        }
    }

    private void passBlock(BasicBlock block) {
        var insts = block.getInstructions();
        for (int i = 0; i < insts.size(); i++) {
            var inst = insts.get(i);
            if (inst instanceof BinaryInst binst) {
                if (binst.getOp() == BinaryInstOp.ADD || binst.getOp() == BinaryInstOp.SUB) {
                    if (binst.getOp() != BinaryInstOp.SUB && binst.getLeft() instanceof ImmediateValue ileft && ileft.getValue() == 0) { // 0 + x
                        inst.replaceAllUseWith((Instruction) binst.getRight(), false);
                    } else if (binst.getRight() instanceof ImmediateValue iright && iright.getValue() == 0) { // x + 0 or x - 0
                        inst.replaceAllUseWith((Instruction) binst.getLeft(), false);
                    }
                } else if (binst.getOp() == BinaryInstOp.MUL || binst.getOp() == BinaryInstOp.SDIV) {
                    if (binst.getOp() != BinaryInstOp.SDIV && binst.getLeft() instanceof ImmediateValue ileft && ileft.getValue() == 1) { // 1 * x
                        inst.replaceAllUseWith((Instruction) binst.getRight(), false);
                    } else if (binst.getRight() instanceof ImmediateValue iright && iright.getValue() == 1) { // x * 1 or x / 1
                        inst.replaceAllUseWith((Instruction) binst.getLeft(), false);
                    }
                }
            }
        }
    }
}
