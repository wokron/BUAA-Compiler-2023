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
        return basicManage(func);
    }

    public int basicManage(Function func) {
        var registersName = List.of("s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7");
        return manageMemory(func, registersName.stream().map(Register.REGS::get).toList(), this::refCountGlobalRegisterManage);
    }

    private void basicGlobalRegisterManage(List<Register> registers, List<AllocaInst> varInsts, Function func) {
        Stack<Register> registersAllocator = new Stack<>();
        registersAllocator.addAll(registers);

        for (var varInst : varInsts) {
            if (registersAllocator.isEmpty()) {
                break;
            }
            if (!varInst.getDataType().getArrayDims().isEmpty()) { // if is arrray
                continue;
            }

            localValueMap.put(varInst, registersAllocator.pop());
        }
    }

    private void refCountGlobalRegisterManage(List<Register> registers, List<AllocaInst> varInsts, Function func) {
        Stack<Register> registersAllocator = new Stack<>();
        registersAllocator.addAll(registers);

        var integerVarInsts = varInsts
                .stream()
                .filter(inst -> inst.getDataType().getArrayDims().isEmpty())
                .toList();

        Map<AllocaInst, Integer> refCounts = new HashMap<>();

        for (var inst : integerVarInsts) {
            int refCount = 0;
            for (var use  : inst.getUseList()) {
                var loopWeight = ((Instruction)use.getUser()).getBasicBlock().getLoopNum();
                if (loopWeight > 0) {
                    refCount += 5 * loopWeight;
                } else {
                    refCount++;
                }
            }
            refCounts.put(inst, refCount);
        }

        var varInstsOrderByRefCount = new ArrayList<>(refCounts.entrySet().stream().sorted(Map.Entry.comparingByValue()).map(Map.Entry::getKey).toList());
        Collections.reverse(varInstsOrderByRefCount);

        for (var inst : varInstsOrderByRefCount) {
            if (registersAllocator.isEmpty()) {
                break;
            }
            localValueMap.put(inst, registersAllocator.pop());
        }
    }

    public List<Register> getRegistersInUse() {
        return localValueMap.values().stream().filter(elm -> elm instanceof Register).map(elm -> (Register)elm).toList();
    }

    public void clearLocals() {
        localValueMap.clear();
    }


    private int manageMemory(Function func, List<Register> registersToAlloc, GlobalRegisterManager globalRegisterManager) {
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

        globalRegisterManager.manageGlobalRegister(registersToAlloc, varAllocaInsts, func);

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

interface GlobalRegisterManager {
    void manageGlobalRegister(List<Register> registers, List<AllocaInst> varInsts, Function func);
}