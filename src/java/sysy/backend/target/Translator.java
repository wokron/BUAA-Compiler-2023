package sysy.backend.target;

import sysy.backend.ir.*;
import sysy.backend.ir.Module;
import sysy.backend.ir.inst.*;
import sysy.backend.target.inst.TextComment;
import sysy.backend.target.inst.TextInst;
import sysy.backend.target.inst.TextLabel;
import sysy.backend.target.value.*;

import java.util.*;
import java.util.stream.Stream;

public class Translator {
    private final Target asmTarget = new Target();
    private final ValueManager valueManager = new ValueManager();
    private TempRegisterPool tempRegisterPool = new TempRegisterPool(
            asmTarget,
            Stream.of("t0", "t1", "t2", "t3", "t4", "t5", "t6").map(Register.REGS::get).toList());
    private int memorySizeForLocal = 0;
    private final Map<Value, Register> registerTempMap = new HashMap<>();

    public Target getAsmTarget() {
        return asmTarget;
    }

    public void translate(Module irModule) {
        for (var globalVal : irModule.getGlobalValues()) {
            translateGlobalValue(globalVal);
        }

        for (var func : irModule.getFunctions()) {
            translateFunction(func);
        }
    }

    private void translateGlobalValue(GlobalValue irGlobalValue) {
        var initVals = irGlobalValue.getInitVals();

        Data newDataEntry;
        if (initVals.isEmpty()) {
            if (irGlobalValue.getType() instanceof ArrayIRType arrayType) {
                newDataEntry = new Data(irGlobalValue.getName().substring(1), "space", List.of(4 * arrayType.getTotalSize()));
            } else {
                newDataEntry = new Data(irGlobalValue.getName().substring(1), "word", List.of(0));
            }
        } else {
            newDataEntry = new Data(irGlobalValue.getName().substring(1), "word", Arrays.asList(initVals.toArray()));
        }

        asmTarget.addData(newDataEntry);
        valueManager.putGlobal(irGlobalValue, newDataEntry.getLabel());
    }

    private void translateFunction(Function irFunction) {
        asmTarget.addText(new TextLabel(irFunction.getName().substring(1)));
        var totalMemorySize = valueManager.putLocals(irFunction);
        memorySizeForLocal = totalMemorySize - irFunction.calcParamSpace();

        if (memorySizeForLocal > 0) {
            var sp = Register.REGS.get("sp");
            asmTarget.addText(new TextInst("addiu", sp, sp, new Immediate(-memorySizeForLocal)));
        }

        Set<Register> tempRegisters = new HashSet<>(
                Stream.of("s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7",
                        "t0", "t1", "t2", "t3", "t4", "t5", "t6")
                        .map(Register.REGS::get)
                        .toList());
        valueManager.getRegistersInUse().forEach(tempRegisters::remove);
        tempRegisterPool = new TempRegisterPool(asmTarget, new ArrayList<>(tempRegisters));

        for (var block : irFunction.getBasicBlocks()) {
            tempRegisterPool.reset();
            registerTempMap.clear();
            translateBasicBlock(block);
        }

        valueManager.clearLocals();
    }

    private void translateBasicBlock(BasicBlock irBlock) {
        var label = new TextLabel(buildBlockLabelName(irBlock));
        asmTarget.addText(label);

        for (var inst : irBlock.getInstructions()) {
            translateInstruction(inst);
        }
    }

    private void translateInstruction(Instruction inst) {
        asmTarget.addText(new TextComment(inst));
        if (inst instanceof BinaryInst i) {
            translateBinaryInst(i);
        } else if (inst instanceof BrInst i) {
            translateBrInst(i);
        } else if (inst instanceof CallInst i) {
            translateCallInst(i);
        } else if (inst instanceof ICmpInst i) {
            translateICmpInst(i);
        } else if (inst instanceof LoadInst i) {
            translateLoadInst(i);
        } else if (inst instanceof StoreInst i) {
            translateStoreInst(i);
        } else if (inst instanceof ReturnInst i) {
            translateReturnInst(i);
        } else if (inst instanceof GetElementPtrInst i) {
            translateGetElementPtrInst(i);
        } else if (inst instanceof ZExtInst i) {
            translateZExtInst(i);
        } else if (inst instanceof AllocaInst i) {
            translateAllocaInst(i);
        }
        Register.freeAllTempRegisters();
    }

    private void translateBinaryInst(BinaryInst inst) {
        var left = tryGetTempRegister(inst.getLeft());

        var right = tryGetTempRegister(inst.getRight());

        var target = tryAllocTempRegisterForInst(inst);

        if (left instanceof Immediate) {
            asmTarget.addText(new TextInst("li", target, left));
            left = target;
        }

        if (right instanceof Immediate) {
            var tmpReg = Register.allocateTempRegister();
            asmTarget.addText(new TextInst("li", tmpReg, right));
            right = tmpReg;
        }

        switch (inst.getOp()) {
            case ADD:
            case SUB:
                // use addu and subu to avoid overflow
                asmTarget.addText(new TextInst(inst.getOp().name().toLowerCase() + "u", target, left, right));
                break;
            case MUL:
                asmTarget.addText(new TextInst(inst.getOp().name().toLowerCase(), target, left, right));
                break;
            case SDIV:
                asmTarget.addText(new TextInst("div", left, right));
                asmTarget.addText(new TextInst("mflo", target));
                break;
            case SREM:
                asmTarget.addText(new TextInst("div", left, right));
                asmTarget.addText(new TextInst("mfhi", target));
                break;
        }
    }

    private void translateReturnInst(ReturnInst inst) {
        if (inst.getValue() != null) {
            var value = tryGetTempRegister(inst.getValue());

            var v0 = Register.REGS.get("v0");

            if (value instanceof Immediate) {
                asmTarget.addText(new TextInst("li", v0, value));
            } else {
                asmTarget.addText(new TextInst("move", v0, value));
            }
        }

        if (memorySizeForLocal > 0) {
            var sp = Register.REGS.get("sp");
            asmTarget.addText(new TextInst("addiu", sp, sp, new Immediate(memorySizeForLocal)));
        }

        asmTarget.addText(new TextInst("jr", Register.REGS.get("ra")));
    }

    private void translateLoadInst(LoadInst inst) {
        if (inst.getPtr() instanceof GetElementPtrInst) {
            var ptr = tryGetTempRegister(inst.getPtr());

            var registerPtr = convertToRegister(ptr);
            var target = tryAllocTempRegisterForInst(inst);

            asmTarget.addText(new TextInst("lw", target, new Offset(registerPtr, 0)));

        } else {
            var ptr = valueManager.getTargetValue(inst.getPtr());

            if (ptr instanceof Register regPtr) {
                registerTempMap.put(inst, regPtr);
                return;
            }

            var target = tryAllocTempRegisterForInst(inst);

            if (ptr instanceof Offset || ptr instanceof Label) {
                asmTarget.addText(new TextInst("lw", target, ptr));
            } else {
                throw new RuntimeException(); // impossible
            }
        }
    }

    private void translateStoreInst(StoreInst inst) {
        if (inst.getValue() instanceof FunctionArgument) {
            return;
        }

        if (inst.getPtr() instanceof GetElementPtrInst) {
            var ptr = tryGetTempRegister(inst.getPtr());

            var value = tryGetTempRegister(inst.getValue());

            var registerValue = convertToRegister(value);

            Register registerPtr = (Register) ptr;

            asmTarget.addText(new TextInst("sw", registerValue, new Offset(registerPtr, 0)));
        } else {
            var ptr = valueManager.getTargetValue(inst.getPtr());

            var value = tryGetTempRegister(inst.getValue());

            var registerValue = convertToRegister(value);

            if (ptr instanceof Offset || ptr instanceof Label) {
                asmTarget.addText(new TextInst("sw", registerValue, ptr));
            } else if (ptr instanceof Register) {
                asmTarget.addText(new TextInst("move", ptr, registerValue));
            } else {
                throw new RuntimeException(); // impossible
            }

        }

    }

    private void translateICmpInst(ICmpInst inst) {
        var left = tryGetTempRegister(inst.getLeft());

        var right = tryGetTempRegister(inst.getRight());

        var target = tryAllocTempRegisterForInst(inst);

        if (left instanceof Immediate) {
            asmTarget.addText(new TextInst("li", target, left));
            left = target;
        }

        if (right instanceof Immediate) {
            var tmpReg = Register.allocateTempRegister();
            asmTarget.addText(new TextInst("li", tmpReg, right));
            right = tmpReg;
        }

        String instName = switch (inst.getCond()) {
            case EQ -> "seq";
            case NE -> "sne";
            case SGE -> "sge";
            case SGT -> "sgt";
            case SLE -> "sle";
            case SLT -> "slt";
        };

        asmTarget.addText(new TextInst(instName, target, left, right));
    }

    private void translateBrInst(BrInst inst) {
        var nextBlock = inst.getBasicBlock().getNextBasicBlock();
        if (inst.getCond() != null) {
            var cond = tryGetTempRegister(inst.getCond());

            var registerCond = convertToRegister(cond);
            var falseBranch = inst.getFalseBranch();
            var falseBranchName = buildBlockLabelName(falseBranch);
            var trueBranch = inst.getTrueBranch();
            var trueBranchName = buildBlockLabelName(trueBranch);

//            tempRegisterPool.writeBackToMemoryForAll();

            if (nextBlock != falseBranch) {
                asmTarget.addText(new TextInst("beqz", registerCond, new Label(falseBranchName)));
            }
            if (nextBlock != trueBranch) {
                asmTarget.addText(new TextInst("bnez", registerCond, new Label(trueBranchName)));
            }
        } else {
            var destBranch = inst.getDest();
            var destBranchName = buildBlockLabelName(destBranch);

//            tempRegisterPool.writeBackToMemoryForAll();

            if (nextBlock != destBranch) {
                asmTarget.addText(new TextInst("j", new Label(destBranchName)));
            }
        }
    }

    private void translateCallInst(CallInst inst) {
        var func = inst.getFunc();
        if (func == Function.BUILD_IN_PUTINT || func == Function.BUILD_IN_PUTCH) {
            asmTarget.addText(new TextInst("li", Register.REGS.get("v0"), new Immediate(func == Function.BUILD_IN_PUTINT ? 1 : 11)));

            var inputVal = inst.getParams().get(0);
            var inputTargetValue = tryGetTempRegister(inputVal);

            var a0 = Register.REGS.get("a0");
            var t7 = Register.REGS.get("t7");
            asmTarget.addText(new TextInst("move", t7, a0));

            if (inputTargetValue instanceof Immediate) {
                asmTarget.addText(new TextInst("li", a0, inputTargetValue));
            } else {
                asmTarget.addText(new TextInst("move", a0, inputTargetValue));
            }

            asmTarget.addText(new TextInst("syscall"));

            asmTarget.addText(new TextInst("move", a0, t7));

        } else if (func == Function.BUILD_IN_GETINT) {
            asmTarget.addText(new TextInst("li", Register.REGS.get("v0"), new Immediate(5)));
            asmTarget.addText(new TextInst("syscall"));

            var target = tryAllocTempRegisterForInst(inst);

            asmTarget.addText(new TextInst("move", target, Register.REGS.get("v0")));

        } else { // common func
            translateCommonFuncCall(inst);
        }
    }

    private void translateCommonFuncCall(CallInst inst) {



        List<Register> registerToReserve = new ArrayList<>();
        registerToReserve.addAll(Stream.of(
//                "t0", "t1", "t2", "t3", "t4",
//                "s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7",
//                "a0", "a1", "a2", "a3",
                "ra"
        ).map(Register.REGS::get).toList());
        registerToReserve.addAll(valueManager.getRegistersInUse());

        var func = inst.getFunc();
        var sp = Register.REGS.get("sp");

        int registerByteSize = 4 * registerToReserve.size();
        int paramByteSize = func.calcParamSpace();
        int newAllocByteSize = registerByteSize + paramByteSize;

        tempRegisterPool.writeBackToMemoryForAll();

        asmTarget.addText(new TextInst("addiu", sp, sp, new Immediate(-newAllocByteSize)));

        reserveRegistersInFuncCall(registerToReserve, paramByteSize);

        if (paramByteSize > 0) {
            int base = 0;

            for (int paramCount = 0; paramCount < inst.getParams().size(); paramCount++, base += 4) {
                var param = inst.getParams().get(paramCount);
                var targetParam = valueManager.getTargetValue(param);

                if (registerTempMap.containsKey(param)) {
                    var reg = registerTempMap.get(param);
                    if (!registerToReserve.contains(reg)) {
                        throw new RuntimeException(); // impossible
                    }
                    var savedRegisterOffset = registerToReserve.indexOf(reg) * 4 + paramByteSize;
                    targetParam = new Offset(sp, savedRegisterOffset);
                } else {
                    if (targetParam instanceof Offset offsetParam) { // sp has changed, so offset change as well
                        if (tempRegisterPool.getRegister(offsetParam) != null) {
                            targetParam = tempRegisterPool.getRegister(offsetParam);
                        } else {
                            targetParam = new Offset(offsetParam.getBase(), offsetParam.getOffset() + newAllocByteSize);
                        }
                    }
                }

                if (paramCount < 4) {
                    var argReg = Register.REGS.get("a" + paramCount);

                    assignToRegister(argReg, targetParam);
                } else {
                    var registerParam = convertToRegister(targetParam);
                    asmTarget.addText(new TextInst("sw", registerParam, new Offset(sp, base)));

                    Register.freeAllTempRegisters(); // TODO: maybe wrong
                }
            }
        }

        tempRegisterPool.reset();

        asmTarget.addText(new TextInst("jal", new Label(func.getName().substring(1))));

        recoverRegistersInFuncCall(registerToReserve, paramByteSize);

        asmTarget.addText(new TextInst("addiu", sp, sp, new Immediate(newAllocByteSize)));

        if (func.getRetType().getType() != IRTypeEnum.VOID) {
            var target = tryAllocTempRegisterForInst(inst);

            asmTarget.addText(new TextInst("move", target, Register.REGS.get("v0")));
        }
    }

    private void assignToRegister(Register reg, TargetValue value) {
        if (value instanceof Immediate) {
            asmTarget.addText(new TextInst("li", reg, value));
        } else if (value instanceof Offset) {
            asmTarget.addText(new TextInst("lw", reg, value));
        } else if (value instanceof Register) {
            asmTarget.addText(new TextInst("move", reg, value));
        } else {
            throw new RuntimeException(); //impossible
        }
    }

    private void reserveRegistersInFuncCall(List<Register> registersToReserve, int baseOffset) {
        var sp = Register.REGS.get("sp");
        int offset = 0;
        for (var register : registersToReserve) {
            asmTarget.addText(new TextInst("sw", register, new Offset(sp, baseOffset + offset)));
            offset += 4;
        }
    }

    private void recoverRegistersInFuncCall(List<Register> registersToRecover, int baseOffset) {
        var sp = Register.REGS.get("sp");
        int offset = 0;
        for (var register : registersToRecover) {
            asmTarget.addText(new TextInst("lw", register, new Offset(sp, baseOffset + offset)));
            offset += 4;
        }
    }

    private void translateGetElementPtrInst(GetElementPtrInst inst) {
        var base = inst.getElementBase();
        var offsets = inst.getOffsets();
        var dims = base.getType().getArrayDims();

        var target = tryAllocTempRegisterForInst(inst);
        Register registerBase = (Register) target; // registerBase is just target

        if (base instanceof LoadInst || base instanceof GetElementPtrInst) { // if is pointer
            var baseVal = tryGetTempRegister(base);

            asmTarget.addText(new TextInst("move", registerBase, baseVal));

        } else { // if is address
            asmTarget.addText(new TextInst("la", registerBase, valueManager.getTargetValue(base)));
        }

        var registerTemp = Register.allocateTempRegister();
        int currDim = 0;
        for (var offset : offsets) {
            if (offset instanceof ImmediateValue immediate && immediate.getValue() == 0) {
                currDim++;
                continue;
            }
            int memSize = 4;
            for (int i = currDim; i < dims.size(); i++) {
                memSize *= dims.get(i);
            }

            var offsetVal = tryGetTempRegister(offset);

            if (offsetVal instanceof Immediate) {
                asmTarget.addText(new TextInst("li", registerTemp, offsetVal));
                asmTarget.addText(new TextInst("mul", registerTemp, registerTemp, new Immediate(memSize)));
            } else {
                asmTarget.addText(new TextInst("mul", registerTemp, offsetVal, new Immediate(memSize)));
            }

            asmTarget.addText(new TextInst("addu", registerBase, registerBase, registerTemp));
            currDim++;
        }
    }

    private void translateZExtInst(ZExtInst inst) {
        var value = tryGetTempRegister(inst.getValue());

        var target = tryAllocTempRegisterForInst(inst);

        if (value instanceof Immediate) {
            asmTarget.addText(new TextInst("li", target, value));
        } else {
            asmTarget.addText(new TextInst("move", target, value));
        }
    }

    private void translateAllocaInst(AllocaInst inst) {
        var targetValue = valueManager.getTargetValue(inst);
        asmTarget.addText(new TextComment(inst.getName() + ": " + targetValue));
    }

    private static String buildBlockLabelName(BasicBlock block) {
        return block.getFunction().getName().substring(1) + "." + block.getName().substring(1);
    }

    private TargetValue tryAllocTempRegisterForInst(Value inst) {
        if (registerTempMap.containsKey(inst)) {
            return registerTempMap.get(inst);
        }

        var instValue = valueManager.getTargetValue(inst);
        if (instValue instanceof Offset offsetInstValue) {
            return tempRegisterPool.allocTempRegister(offsetInstValue, true);
        } else {
            return instValue;
        }
    }

    private TargetValue tryGetTempRegister(Value value) {
        if (registerTempMap.containsKey(value)) {
            return registerTempMap.get(value);
        }

        var targetValue = valueManager.getTargetValue(value);
        if (targetValue instanceof Offset offsetValue) {
            return tempRegisterPool.allocTempRegister(offsetValue, false);
        } else {
            return targetValue;
        }
    }

    private Register convertToRegister(TargetValue targetValue) {
        if (isAddress(targetValue)) {
            var newReg = Register.allocateTempRegister();
            asmTarget.addText(new TextInst("lw", newReg, targetValue));
            return newReg;
        } else if (isImmediate(targetValue)) {
            var newReg = Register.allocateTempRegister();
            asmTarget.addText(new TextInst("li", newReg, targetValue));
            return newReg;
        } else if (targetValue instanceof Register register) {
            return register;
        } else {
            throw new RuntimeException(); // impossible
        }
    }

    private boolean isImmediate(TargetValue value) {
        return value instanceof Immediate;
    }

    private boolean isAddress(TargetValue value) {
        return value instanceof Label || value instanceof Offset;
    }
}
