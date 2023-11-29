package sysy.backend.optim;

import sysy.backend.ir.BasicBlock;
import sysy.backend.ir.inst.AllocaInst;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ConflictDiagramBuilder {
    private final ConflictDiagram diagram;

    public ConflictDiagramBuilder(List<AllocaInst> allocaInstList, Map<BasicBlock, Set<AllocaInst>> defSets, Map<BasicBlock, Set<AllocaInst>> inSets) {
        var blocks = defSets.keySet();

        diagram = new ConflictDiagram(allocaInstList);

        for (var block : blocks) {
            var defSet = defSets.get(block);
            var inSet = inSets.get(block);
            for (var def : defSet) {
                for (var in : inSet) {
                    if (def != in) {
                        diagram.addConflict(def, in);
                    }
                }
            }
        }
    }

    public ConflictDiagram getDiagram() {
        return diagram;
    }
}
