package sysy.backend.optim;

import sysy.backend.ir.BasicBlock;
import sysy.backend.ir.Function;
import sysy.backend.ir.Module;
import sysy.backend.ir.inst.*;

import java.util.HashSet;
import java.util.Set;

public class DeadCodeEliminationPass {
    private final Module irModule;

    public DeadCodeEliminationPass(Module irModule) {
        this.irModule = irModule;
    }

    public Module pass() {
        for (var func : irModule.getFunctions()) {
            passFunc(func);
        }

        return irModule;
    }

    private void passFunc(Function func) {
        Set<Instruction> initialUsefulSet = new HashSet<>();

        for (var block : func.getBasicBlocks()) {
            for (var inst : block.getInstructions()) {
                if (inst instanceof ReturnInst
                        || inst instanceof BrInst
                        || inst instanceof CallInst
                        || inst instanceof StoreInst) {
                    initialUsefulSet.add(inst);
                }
            }
        }

        var usefulSet = getUsefulClosure(initialUsefulSet);
        for (var block : func.getBasicBlocks()) {
            var insts = block.getInstructions();
            for (int i = 0; i < insts.size(); i++) {
                var inst = insts.get(i);
                if (!usefulSet.contains(inst) && !(inst instanceof AllocaInst)) {
                    inst.replaceAllUseWith(null, false);
                    i--;
                }
            }
        }
    }

    private Set<Instruction> getUsefulClosure(Set<Instruction> instSet) {
        Set<Instruction> usefulSet = new HashSet<>();
        for (var inst : instSet) {
            usefulSet.addAll(getUsefulClosure(inst));
        }
        return usefulSet;
    }

    private Set<Instruction> getUsefulClosure(Instruction inst) {
        Set<Instruction> usefulSet = new HashSet<>();
        usefulSet.add(inst);
        for (var operand : inst.getOperands()) {
            if (operand instanceof Instruction instOperand) {
                usefulSet.addAll(getUsefulClosure(instOperand));
            }
        }
        return usefulSet;
    }

    private void passBlock(BasicBlock block) {
        var insts = block.getInstructions();
        for (int i = insts.size() - 1; i >= 0; i--) {
            var inst = insts.get(i);
        }
    }
}
