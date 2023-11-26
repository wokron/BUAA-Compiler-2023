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
        return fastGlobalManage(func);
//        return simpleManage(func);
//        return fastManage(func);
    }

    public void clearLocals() {
        localValueMap.clear();
    }

    private int simpleManage(Function func) {
        return fastRegisterManage(func, List.of());
    }

    private int fastManage(Function func) {
        var registersName = List.of(
                "t0", "t1", "t2", "t3", "t4",
                "s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7"
        );

        return fastRegisterManage(func, registersName.stream().map(Register.REGS::get).toList());
    }

    private int fastGlobalManage(Function func) {
        var registersName = List.of("s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7");
        return fastGlobalRegisterManage(func, registersName.stream().map(Register.REGS::get).toList());
    }

    private int fastRegisterManage(Function func, List<Register> registersToAlloc) {
        int baseOffset = 0;
        int currArgAlloca = func.getArguments().size()-1;
        for (var block : func.getBasicBlocks()) {
            Stack<Register> registers = new Stack<>();
            registers.addAll(registersToAlloc);

            for (var inst : block.getInstructions()) {
                if ((inst instanceof StoreInst)
                        || (inst instanceof BrInst)
                        || (inst instanceof ReturnInst)
                        || (inst instanceof CallInst callInst && callInst.getType().getType() == IRTypeEnum.VOID)) {
                    continue;
                }
                if (!(inst instanceof AllocaInst) && !registers.isEmpty()) {
                    localValueMap.put(inst, registers.pop());
                } else if (inst instanceof AllocaInst && currArgAlloca >= 0 && currArgAlloca < 4) { // if is top 4 arguments
                    localValueMap.put(inst, Register.REGS.get("a" + currArgAlloca));
                    baseOffset += 4;
                } else if (inst instanceof AllocaInst allocaInst
                        && allocaInst.getDataType() instanceof ArrayIRType arrayIRType
                        && arrayIRType.getPtrNum() == 0) {
                    baseOffset += 4 * arrayIRType.getTotalSize();
                } else {
                    baseOffset += 4;
                }
                currArgAlloca--;
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
                if (localValueMap.containsKey(inst)) {
                    if (inst instanceof AllocaInst) { // if is argument
                        baseOffset -= 4;
                    }
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


    private int fastGlobalRegisterManage(Function func, List<Register> registersToAlloc) {
        int baseOffset = 0;
        int currArgAlloca = func.getArguments().size()-1;
        for (var block : func.getBasicBlocks()) {
            Stack<Register> registers = new Stack<>();
            registers.addAll(registersToAlloc);

            for (var inst : block.getInstructions()) {
                if ((inst instanceof StoreInst)
                        || (inst instanceof BrInst)
                        || (inst instanceof ReturnInst)
                        || (inst instanceof CallInst callInst && callInst.getType().getType() == IRTypeEnum.VOID)) {
                    continue;
                }

                if (inst instanceof AllocaInst && currArgAlloca >= 0 && currArgAlloca < 4) { // if is top 4 arguments
                    localValueMap.put(inst, Register.REGS.get("a" + currArgAlloca));
                    baseOffset += 4;
                } else if (inst instanceof AllocaInst allocaInst && allocaInst.getDataType().getArrayDims().isEmpty() && !registers.isEmpty()) {
                    localValueMap.put(inst, registers.pop());
                } else if (inst instanceof AllocaInst allocaInst
                        && allocaInst.getDataType() instanceof ArrayIRType arrayIRType
                        && arrayIRType.getPtrNum() == 0) {
                    baseOffset += 4 * arrayIRType.getTotalSize();
                } else {
                    baseOffset += 4;
                }
                currArgAlloca--;
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
                if (localValueMap.containsKey(inst)) {
//                    if (inst instanceof AllocaInst) { // if is argument
//                        baseOffset -= 4;
//                    }
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
