package sysy.backend.target;

import sysy.backend.ir.*;
import sysy.backend.ir.inst.BrInst;
import sysy.backend.ir.inst.CallInst;
import sysy.backend.ir.inst.ReturnInst;
import sysy.backend.ir.inst.StoreInst;
import sysy.backend.target.value.*;

import java.util.HashMap;
import java.util.Map;

public class ValueManager {
    private final Map<Value, TargetValue> globalValueMap = new HashMap<>();
    private final Map<Value, TargetValue> localValueMap = new HashMap<>();

    public TargetValue getTargetValue(Value value) {
        if (value instanceof ImmediateValue immediateValue) {
            return new Immediate(immediateValue.getValue());
        }
        return globalValueMap.getOrDefault(value, localValueMap.getOrDefault(value, null));
    }

    public void putGlobal(Value value, Label label) {
        globalValueMap.put(value, label);
    }

    public void putLocals(Function func) {
        // TODO: need dataflow analysis
        simpleManage(func);

    }

    public void clearLocals() {
        localValueMap.clear();
    }

    private void simpleManage(Function func) {
        int baseOffset = 0;
        for (var block : func.getBasicBlocks()) {
            for (var inst : block.getInstructions()) {
                if ((inst instanceof StoreInst)
                        || (inst instanceof BrInst)
                        || (inst instanceof ReturnInst)
                        || (inst instanceof CallInst callInst && callInst.getType().getType() == IRTypeEnum.VOID)) {
                    continue;
                }
                var type = inst.getType();
                if (type.getPtrNum() == 0 && type instanceof ArrayIRType arrayIRType) {
                    baseOffset += 4 * arrayIRType.getTotalSize();
                } else {
                    baseOffset += 4;
                }
            }
        }

        for (var block : func.getBasicBlocks()) {
            for (var inst : block.getInstructions()) {
                if ((inst instanceof StoreInst)
                        || (inst instanceof BrInst)
                        || (inst instanceof ReturnInst)
                        || (inst instanceof CallInst callInst && callInst.getType().getType() == IRTypeEnum.VOID)) {
                    continue;
                }
                var type = inst.getType();
                if (type.getPtrNum() == 0 && type instanceof ArrayIRType arrayIRType) {
                    baseOffset -= 4 * arrayIRType.getTotalSize();
                } else {
                    baseOffset -= 4;
                }
                localValueMap.put(inst, new Offset(Register.REGS.get("sp"), baseOffset));
            }
        }
    }
}
