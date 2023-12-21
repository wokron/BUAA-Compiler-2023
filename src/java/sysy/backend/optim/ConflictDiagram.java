package sysy.backend.optim;

import sysy.backend.ir.inst.AllocaInst;

import java.util.*;

public class ConflictDiagram {
    private final Map<AllocaInst, Set<AllocaInst>> conflict = new HashMap<>();

    public ConflictDiagram(List<AllocaInst> elements) {
        for (var elm : elements) {
            conflict.put(elm, new HashSet<>());
        }
    }

    public void addConflict(AllocaInst a, AllocaInst b) {
        conflict.get(a).add(b);
        conflict.get(b).add(a);
    }

    public void removeNode(AllocaInst node) {
        conflict.remove(node);
        for (var otherNode : conflict.keySet()) {
            conflict.get(otherNode).remove(node);
        }
    }

    public ConflictDiagram copy() {
        var newDiagram = new ConflictDiagram(this.conflict.keySet().stream().toList());
        for (var node : this.conflict.keySet()) {
            var newSet = new HashSet<>(this.conflict.get(node));
            newDiagram.conflict.put(node, newSet);
        }
        return newDiagram;
    }

    public boolean isEmpty() {
        return conflict.isEmpty();
    }

    public Set<AllocaInst> getConflict(AllocaInst a) {
        return conflict.get(a);
    }

    public Set<AllocaInst> getNodes() {
        return conflict.keySet();
    }
}
