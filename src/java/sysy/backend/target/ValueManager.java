package sysy.backend.target;

import sysy.backend.ir.*;
import sysy.backend.ir.inst.*;
import sysy.backend.target.value.*;

import java.util.*;

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
        return refactorManage(func);
//        return fastGlobalManage(func);
//        return simpleManage(func);
//        return fastManage(func);
    }

    public int refactorManage(Function func) {
        var registersName = List.of("s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7");
        return refactorRegisterManage(func, registersName.stream().map(Register.REGS::get).toList());
    }

    public List<Register> getRegistersInUse() {
        return localValueMap.values().stream().filter(elm -> elm instanceof Register).map(elm -> (Register)elm).toList();
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
                } else if (inst instanceof AllocaInst allocaInst
                        && allocaInst.getDataType().getArrayDims().isEmpty()
                        && !registers.isEmpty()
                        && currArgAlloca < 0) {
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

    private int refactorRegisterManage(Function func, List<Register> registersToAlloc) {
        int argNum = func.getArguments().size();
        List<AllocaInst> allAllocaInsts = new ArrayList<>(func.getFirstBasicBlock().getInstructions()
                .stream()
                .filter(inst -> inst instanceof AllocaInst)
                .map(inst -> (AllocaInst) inst)
                .toList());

        List<AllocaInst> argAllocaInsts = allAllocaInsts.subList(0, argNum); // argn to arg0
        Collections.reverse(argAllocaInsts); // arg0 to argn
        List<AllocaInst> varAllocaInsts = allAllocaInsts.subList(argNum, allAllocaInsts.size());

        for (int i = 0; i < argAllocaInsts.size() && i < 4; i++) {
            var inst = argAllocaInsts.get(i);
            localValueMap.put(inst, Register.REGS.get("a" + i));
        }

        Stack<Register> registers = new Stack<>();
        registers.addAll(registersToAlloc);

        for (var varInst : varAllocaInsts) {
            if (registers.isEmpty()) {
                break;
            }
            if (!varInst.getDataType().getArrayDims().isEmpty()) { // if is arrray
                continue;
            }

            localValueMap.put(varInst, registers.pop());
        }

        int numOfArgOnRegister = Math.min(argNum, 4);
        int memoryRequire = numOfArgOnRegister * 4;

        for (var block : func.getBasicBlocks()) {
            for (var inst : block.getInstructions()) {
                if ((inst instanceof StoreInst)
                        || (inst instanceof BrInst)
                        || (inst instanceof ReturnInst)
                        || (inst instanceof CallInst callInst && callInst.getType().getType() == IRTypeEnum.VOID)) {
                    continue;
                }
                if (localValueMap.containsKey(inst)) {
                    continue;
                }

                if (inst instanceof AllocaInst allocaInst
                        && allocaInst.getDataType() instanceof ArrayIRType arrayIRType
                        && arrayIRType.getPtrNum() == 0) {
                    memoryRequire += 4 * arrayIRType.getTotalSize();
                } else {
                    memoryRequire += 4;
                }
            }
        }

        int baseOffset = memoryRequire;
        var sp = Register.REGS.get("sp");

        for (int i = argAllocaInsts.size()-1; i >= 4; i--) {
            var argInst = argAllocaInsts.get(i);
            baseOffset -= 4;
            localValueMap.put(argInst, new Offset(sp, baseOffset));
        }

        baseOffset -= numOfArgOnRegister * 4;

        for (var block : func.getBasicBlocks()) {
            for (var inst : block.getInstructions()) {
                if ((inst instanceof StoreInst)
                        || (inst instanceof BrInst)
                        || (inst instanceof ReturnInst)
                        || (inst instanceof CallInst callInst && callInst.getType().getType() == IRTypeEnum.VOID)) {
                    continue;
                }
                if (localValueMap.containsKey(inst)) {
                    continue;
                }

                if (inst instanceof AllocaInst allocaInst
                        && allocaInst.getDataType() instanceof ArrayIRType arrayIRType
                        && arrayIRType.getPtrNum() == 0) {
                    baseOffset -= 4 * arrayIRType.getTotalSize();
                } else {
                    baseOffset -= 4;
                }

                localValueMap.put(inst, new Offset(sp, baseOffset));
            }

        }

        if (baseOffset != 0) {
            throw new RuntimeException();
        }

        return memoryRequire;
    }
}
