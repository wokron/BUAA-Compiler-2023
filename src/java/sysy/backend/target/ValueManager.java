package sysy.backend.target;

import sysy.backend.ir.*;
import sysy.backend.ir.inst.*;
import sysy.backend.target.value.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

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

    public int putLocals(Function func) {
        // TODO: need dataflow analysis
//        return simpleManage(func);
        return fastManage(func);
    }

    public void clearLocals() {
        localValueMap.clear();
    }

    private int simpleManage(Function func) {
        int baseOffset = 0;
        for (var block : func.getBasicBlocks()) {
            for (var inst : block.getInstructions()) {
                if ((inst instanceof StoreInst)
                        || (inst instanceof BrInst)
                        || (inst instanceof ReturnInst)
                        || (inst instanceof CallInst callInst && callInst.getType().getType() == IRTypeEnum.VOID)) {
                    continue;
                }
                if (inst instanceof AllocaInst allocaInst
                        && allocaInst.getDataType() instanceof ArrayIRType arrayIRType
                        && arrayIRType.getPtrNum() == 0) {
                    baseOffset += 4 * arrayIRType.getTotalSize();
                } else {
                    baseOffset += 4;
                }
            }
        }

        int memorySize = baseOffset;

        for (var block : func.getBasicBlocks()) {
            for (var inst : block.getInstructions()) {
                if ((inst instanceof StoreInst)
                        || (inst instanceof BrInst)
                        || (inst instanceof ReturnInst)
                        || (inst instanceof CallInst callInst && callInst.getType().getType() == IRTypeEnum.VOID)) {
                    continue;
                }
                if (inst instanceof AllocaInst allocaInst
                        && allocaInst.getDataType() instanceof ArrayIRType arrayIRType
                        && arrayIRType.getPtrNum() == 0) {
                    baseOffset -= 4 * arrayIRType.getTotalSize();
                } else {
                    baseOffset -= 4;
                }
                localValueMap.put(inst, new Offset(Register.REGS.get("sp"), baseOffset));
            }
        }

        return memorySize;
    }

    private int fastManage(Function func) {
        var registersName = List.of(
                "t0", "t1", "t2", "t3", "t4",
                "s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7"
        );

        int baseOffset = 0;
        for (var block : func.getBasicBlocks()) {
            Stack<Register> registers = new Stack<>();
            for (var name : registersName) {
                registers.add(Register.REGS.get(name));
            }

            for (var inst : block.getInstructions()) {
                if ((inst instanceof StoreInst)
                        || (inst instanceof BrInst)
                        || (inst instanceof ReturnInst)
                        || (inst instanceof CallInst callInst && callInst.getType().getType() == IRTypeEnum.VOID)) {
                    continue;
                }
                if (!(inst instanceof AllocaInst) && !registers.isEmpty()) {
                    localValueMap.put(inst, registers.pop());
                } else if (inst instanceof AllocaInst allocaInst
                        && allocaInst.getDataType() instanceof ArrayIRType arrayIRType
                        && arrayIRType.getPtrNum() == 0) {
                    baseOffset += 4 * arrayIRType.getTotalSize();
                } else {
                    baseOffset += 4;
                }
            }
        }

        int memorySize = baseOffset;

        for (var block : func.getBasicBlocks()) {
            for (var inst : block.getInstructions()) {
                if ((inst instanceof StoreInst)
                        || (inst instanceof BrInst)
                        || (inst instanceof ReturnInst)
                        || (inst instanceof CallInst callInst && callInst.getType().getType() == IRTypeEnum.VOID)) {
                    continue;
                }
                if (!(inst instanceof AllocaInst) && localValueMap.containsKey(inst)) {
                    continue;
                } else if (inst instanceof AllocaInst allocaInst
                        && allocaInst.getDataType() instanceof ArrayIRType arrayIRType
                        && arrayIRType.getPtrNum() == 0) {
                    baseOffset -= 4 * arrayIRType.getTotalSize();
                } else {
                    baseOffset -= 4;
                }
                localValueMap.put(inst, new Offset(Register.REGS.get("sp"), baseOffset));
            }
        }

        return memorySize;
    }
}
