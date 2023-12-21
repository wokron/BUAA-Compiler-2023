package sysy.backend.optim;

import sysy.backend.ir.BasicBlock;
import sysy.backend.ir.Function;
import sysy.backend.ir.FunctionArgument;
import sysy.backend.ir.inst.*;

import java.util.*;

public class LiveVariableAnalyzer {
    private final Map<BasicBlock, List<BasicBlock>> dataflowDiagram = new HashMap<>();
    private final Map<BasicBlock, Set<AllocaInst>> defSets = new HashMap<>();
    private final Map<BasicBlock, Set<AllocaInst>> inSets = new HashMap<>();
    private final Map<BasicBlock, Set<AllocaInst>> outSets = new HashMap<>();
    private final List<BasicBlock> basicBlocks = new ArrayList<>();
    private boolean updateInIterate = true;

    public LiveVariableAnalyzer(Function func) {
        basicBlocks.addAll(func.getBasicBlocks());

        buildDataflowDiagram(func);

        for (var block : func.getBasicBlocks()) {
            defSets.put(block, getDefSet(block));
            inSets.put(block, getUseSet(block)); // use set is useless, just use in set instead
            outSets.put(block, new HashSet<>());
        }
    }

    private void buildDataflowDiagram(Function func) {
        for (var block : func.getBasicBlocks()) {
            var nextBlocks = getNextBlocks(block);
            dataflowDiagram.put(block, nextBlocks);
        }
    }

    private List<BasicBlock> getNextBlocks(BasicBlock block) {
        List<BasicBlock> nextBlocks = new ArrayList<>();
        var insts = block.getInstructions();
        var lastInst = insts.get(insts.size()-1);

        if (lastInst instanceof BrInst brInst) {
            if (brInst.getDest() != null) {
                nextBlocks.add(brInst.getDest());
            } else {
                nextBlocks.add(brInst.getTrueBranch());
                nextBlocks.add(brInst.getFalseBranch());
            }
        }
        return nextBlocks;
    }

    private Set<AllocaInst> getUseSet(BasicBlock block) {
        Set<AllocaInst> useSet = new HashSet<>();

        for (var inst : block.getInstructions()) {
            if (inst instanceof LoadInst loadInst) {
                var ptr = loadInst.getPtr();
                if (ptr instanceof AllocaInst allocaInstPtr) {
                    useSet.add(allocaInstPtr);
                }
            }
        }

        return useSet;
    }

    private Set<AllocaInst> getDefSet(BasicBlock block) {
        Set<AllocaInst> defSet = new HashSet<>();

        for (var inst : block.getInstructions()) {
            if (inst instanceof StoreInst storeInst && !(storeInst.getValue() instanceof FunctionArgument)) {
                var ptr = storeInst.getPtr();
                if (ptr instanceof AllocaInst allocaInstPtr) {
                    defSet.add(allocaInstPtr);
                }
            }
        }

        return defSet;
    }

    public void analyze() {
        while (!canStop()) {
            updateInIterate = false;
            iterate();
        }
    }

    private boolean canStop() {
        return !updateInIterate;
    }

    private void iterate() {
        for (int i = basicBlocks.size()-1; i >= 0; i--) {
            var block = basicBlocks.get(i);
            updateOutSet(block);
            updateInSet(block);
        }
    }

    private void updateOutSet(BasicBlock block) {
        var nextBlocks = dataflowDiagram.get(block);

        var outSet = outSets.get(block);

        for (var nextBlock : nextBlocks) {
            int preSize = outSet.size();
            outSet.addAll(inSets.get(nextBlock));
            int nowSize = outSet.size();
            if (preSize != nowSize) {
                updateInIterate = true;
            }
        }
    }

    private void updateInSet(BasicBlock block) {
        var inSet = inSets.get(block);
        var outSet = outSets.get(block);
        var defSet = defSets.get(block);

        Set<AllocaInst> outSetCopy = new HashSet<>(outSet);
        outSetCopy.removeAll(defSet);
        var outWithoutDef = outSetCopy;

        inSet.addAll(outWithoutDef);
    }

    public Map<BasicBlock, Set<AllocaInst>> getInSets() {
        return inSets;
    }

    public Map<BasicBlock, Set<AllocaInst>> getOutSets() {
        return outSets;
    }

    public Map<BasicBlock, Set<AllocaInst>> getDefSets() {
        return defSets;
    }
}
