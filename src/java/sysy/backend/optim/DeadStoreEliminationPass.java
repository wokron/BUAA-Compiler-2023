package sysy.backend.optim;

import sysy.backend.ir.*;
import sysy.backend.ir.Module;
import sysy.backend.ir.inst.AllocaInst;
import sysy.backend.ir.inst.LoadInst;
import sysy.backend.ir.inst.StoreInst;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DeadStoreEliminationPass {
    private final Module irModule;
    private Map<BasicBlock, Set<AllocaInst>> outSets;
    private Set<AllocaInst> varAllocaInstSet;

    public DeadStoreEliminationPass(Module irModule) {
        this.irModule = irModule;
    }

    public Module pass() {
        for (var func : irModule.getFunctions()) {
            passFunc(func);
        }

        return irModule;
    }

    private void passFunc(Function func) {
        var analyzer = new LiveVariableAnalyzer(func);
        analyzer.analyze();
        outSets = analyzer.getOutSets();

        varAllocaInstSet = new HashSet<>(func.getFirstBasicBlock().getInstructions()
                .stream()
                .filter(inst -> inst instanceof AllocaInst)
                .map(inst -> (AllocaInst) inst)
                .filter(allocaInst -> {
                    var dataType = allocaInst.getDataType();
                    return dataType.getPtrNum() == 0 && dataType.getArrayDims().isEmpty();
                })
                .toList());

        for (var block : func.getBasicBlocks()) {
            passBlock(block);
        }
    }

    private void passBlock(BasicBlock block) {
        var outSet = outSets.get(block);
        Set<AllocaInst> needStore = new HashSet<>(outSet);

        var insts = block.getInstructions();
        for (int i = insts.size()-1; i >= 0; i--) {
            var inst = insts.get(i);

            if (inst instanceof LoadInst loadInst) {
                var ptr = loadInst.getPtr();
                if (ptr instanceof AllocaInst allocaInstPtr && varAllocaInstSet.contains(allocaInstPtr)) {
                    needStore.add(allocaInstPtr);
                }
            } else if (inst instanceof StoreInst storeInst) {
                var ptr = storeInst.getPtr();
                if (ptr instanceof AllocaInst allocaInstPtr && varAllocaInstSet.contains(allocaInstPtr)) {
                    if (needStore.contains(allocaInstPtr)) {
                        needStore.remove(allocaInstPtr);
                    } else {
                        inst.replaceAllUseWith(null, false); // useList of store inst is empty, so null is safe
                    }
                }
            }
        }
    }
}
