package sysy.backend.optim;

import sysy.backend.ir.*;
import sysy.backend.ir.Module;
import sysy.backend.ir.inst.*;

import java.util.HashMap;
import java.util.Map;

public class ConstPropagatePass {
    private final Module irModule;
    private final Map<Value, ImmediateValue> immediateMap = new HashMap<>();
    private boolean improve = false;

    public ConstPropagatePass(Module irModule) {
        this.irModule = irModule;
    }

    public boolean isImprove() {
        return improve;
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
        immediateMap.clear();

        var insts = block.getInstructions();
        for (int i = 0; i < insts.size(); i++) {
            var inst = insts.get(i);
            if (inst instanceof StoreInst storeInst) {
                if (storeInst.getValue() instanceof ImmediateValue immediateValue
                        && storeInst.getPtr() instanceof AllocaInst allocaInst) {
                    immediateMap.put(allocaInst, immediateValue);
                } else {
                    immediateMap.remove(storeInst.getPtr());
                }
            } else if (inst instanceof LoadInst loadInst) {
                if (immediateMap.containsKey(loadInst.getPtr())) {
                    inst.replaceAllUseWith(immediateMap.get(loadInst.getPtr()), false);
                    improve = true;
                    i--;
                }
            }
        }
    }
}
