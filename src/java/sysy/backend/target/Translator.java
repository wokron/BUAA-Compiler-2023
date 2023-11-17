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
    private int memorySizeForLocal = 0;

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

        for (var block : irFunction.getBasicBlocks()) {
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

        var left = valueManager.getTargetValue(inst.getLeft());
        var right = valueManager.getTargetValue(inst.getRight());
        var target = valueManager.getTargetValue(inst);

        var registerLeft = convertToRegister(left);
        var registerRight = convertToRegister(right);

        Register registerTarget;
        if (target instanceof Offset) {
            var newReg = Register.allocateTempRegister();
            registerTarget = newReg;
        } else if (target instanceof Register t) {
            registerTarget = t;
        } else {
            throw new RuntimeException(); // impossible
        }

        switch (inst.getOp()) {
            case ADD:
            case SUB:
                // use addu and subu to avoid overflow
                asmTarget.addText(new TextInst(inst.getOp().name().toLowerCase() + "u", registerTarget, registerLeft, registerRight));
                break;
            case MUL:
                asmTarget.addText(new TextInst(inst.getOp().name().toLowerCase(), registerTarget, registerLeft, registerRight));
                break;
            case SDIV:
                asmTarget.addText(new TextInst("div", registerLeft, registerRight));
                asmTarget.addText(new TextInst("mflo", registerTarget));
                break;
            case SREM:
                asmTarget.addText(new TextInst("div", registerLeft, registerRight));
                asmTarget.addText(new TextInst("mfhi", registerTarget));
                break;
        }

        if (target instanceof Offset) {
            asmTarget.addText(new TextInst("sw", registerTarget, target));
        }
    }

    private void translateReturnInst(ReturnInst inst) {
        if (inst.getValue() != null) {
            var value = valueManager.getTargetValue(inst.getValue());
            var registerValue = convertToRegister(value);
            asmTarget.addText(new TextInst("move", Register.REGS.get("v0"), registerValue));
        }

        if (memorySizeForLocal > 0) {
            var sp = Register.REGS.get("sp");
            asmTarget.addText(new TextInst("addiu", sp, sp, new Immediate(memorySizeForLocal)));
        }

        asmTarget.addText(new TextInst("jr", Register.REGS.get("ra")));
    }

    private void translateLoadInst(LoadInst inst) {
        if (inst.getPtr() instanceof GetElementPtrInst) {
            var ptr = valueManager.getTargetValue(inst.getPtr());
            var registerPtr = convertToRegister(ptr);
            var target = valueManager.getTargetValue(inst);

            if (target instanceof Register) {
                asmTarget.addText(new TextInst("lw", target, new Offset(registerPtr, 0)));
            } else if (target instanceof Offset) {
                var registerTemp = Register.allocateTempRegister();
                asmTarget.addText(new TextInst("lw", registerTemp, new Offset(registerPtr, 0)));
                asmTarget.addText(new TextInst("sw", registerTemp, target));
            } else {
                throw new RuntimeException(); // impossible
            }

        } else {
            var ptr = valueManager.getTargetValue(inst.getPtr());
            var target = valueManager.getTargetValue(inst);

            if (target instanceof Register) {
                if (ptr instanceof Register) {
                    asmTarget.addText(new TextInst("move", target, ptr));
                } else if (ptr instanceof Offset || ptr instanceof Label) {
                    asmTarget.addText(new TextInst("lw", target, ptr));
                } else {
                    throw new RuntimeException(); // impossible
                }
            } else if (target instanceof Offset) {
                if (ptr instanceof Register) {
                    asmTarget.addText(new TextInst("sw", ptr, target));
                } else if (ptr instanceof Offset || ptr instanceof Label) {
                    var tempReg = Register.allocateTempRegister();
                    asmTarget.addText(new TextInst("lw", tempReg, ptr));
                    asmTarget.addText(new TextInst("sw", tempReg, target));
                } else {
                    throw new RuntimeException(); // impossible
                }
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
            var ptr = valueManager.getTargetValue(inst.getPtr());
            var value = valueManager.getTargetValue(inst.getValue());

            var registerValue = convertToRegister(value);

            Register registerTemp;
            if (ptr instanceof Offset) {
                registerTemp = Register.allocateTempRegister();
                asmTarget.addText(new TextInst("lw", registerTemp, ptr)); // get addr
            } else if (ptr instanceof Register rptr) {
                registerTemp = rptr;
            } else {
                throw new RuntimeException(); // impossible
            }

            asmTarget.addText(new TextInst("sw", registerValue, new Offset(registerTemp, 0)));
        } else {
            var ptr = valueManager.getTargetValue(inst.getPtr());
            var value = valueManager.getTargetValue(inst.getValue());

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
        var left = valueManager.getTargetValue(inst.getLeft());
        var right = valueManager.getTargetValue(inst.getRight());
        var target = valueManager.getTargetValue(inst);

        var registerLeft = convertToRegister(left);
        var registerRight = convertToRegister(right);

        Register registerTarget;
        if (target instanceof Offset) {
            var newReg = Register.allocateTempRegister();
            registerTarget = newReg;
        } else if (target instanceof Register t) {
            registerTarget = t;
        } else {
            throw new RuntimeException(); // impossible
        }

        String instName = switch (inst.getCond()) {
            case EQ -> "seq";
            case NE -> "sne";
            case SGE -> "sge";
            case SGT -> "sgt";
            case SLE -> "sle";
            case SLT -> "slt";
        };

        asmTarget.addText(new TextInst(instName, registerTarget, registerLeft, registerRight));

        if (target instanceof Offset) {
            asmTarget.addText(new TextInst("sw", registerTarget, target));
        }
    }

    private void translateBrInst(BrInst inst) {
        var nextBlock = inst.getBasicBlock().getNextBasicBlock();
        if (inst.getCond() != null) {
            var cond = valueManager.getTargetValue(inst.getCond());
            var registerCond = convertToRegister(cond);
            var falseBranch = inst.getFalseBranch();
            var falseBranchName = buildBlockLabelName(falseBranch);
            var trueBranch = inst.getTrueBranch();
            var trueBranchName = buildBlockLabelName(trueBranch);

            if (nextBlock != falseBranch) {
                asmTarget.addText(new TextInst("beqz", registerCond, new Label(falseBranchName)));
            }
            if (nextBlock != trueBranch) {
                asmTarget.addText(new TextInst("bnez", registerCond, new Label(trueBranchName)));
            }
        } else {
            var destBranch = inst.getDest();
            var destBranchName = buildBlockLabelName(destBranch);

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
            var inputTargetValue = valueManager.getTargetValue(inputVal);

            var tmpRegister = Register.allocateTempRegister();
            asmTarget.addText(new TextInst("move", tmpRegister, Register.REGS.get("a0")));

            if (inputTargetValue instanceof Immediate) {
                asmTarget.addText(new TextInst("li", Register.REGS.get("a0"), inputTargetValue));
            } else if (inputTargetValue instanceof Offset) {
                asmTarget.addText(new TextInst("lw", Register.REGS.get("a0"), inputTargetValue));
            } else if (inputTargetValue instanceof Register) {
                asmTarget.addText(new TextInst("move", Register.REGS.get("a0"), inputTargetValue));
            } else {
                throw new RuntimeException(); // impossible
            }

            asmTarget.addText(new TextInst("syscall"));

            asmTarget.addText(new TextInst("move", Register.REGS.get("a0"), tmpRegister));

        } else if (func == Function.BUILD_IN_GETINT) {
            asmTarget.addText(new TextInst("li", Register.REGS.get("v0"), new Immediate(5)));
            asmTarget.addText(new TextInst("syscall"));

            var target = valueManager.getTargetValue(inst);

            if (target instanceof Offset) {
                asmTarget.addText(new TextInst("sw", Register.REGS.get("v0"), target));
            } else if (target instanceof Register) {
                asmTarget.addText(new TextInst("move", target, Register.REGS.get("v0")));
            } else {
                throw new RuntimeException(); // impossible
            }

        } else { // common func
            translateCommonFuncCall(inst);
        }
    }

    private void translateCommonFuncCall(CallInst inst) {
        var registerToReserve = Stream.of(
                "t0", "t1", "t2", "t3", "t4",
                "s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7",
                "a0", "a1", "a2", "a3", "ra"
        ).map(Register.REGS::get).toList();
        int registerByteSize = 4 * registerToReserve.size();

        var func = inst.getFunc();
        int paramByteSize = func.calcParamSpace();
        var sp = Register.REGS.get("sp");

        reserveRegistersInFuncCall(registerToReserve);

        if (paramByteSize > 0) {
            int base = -paramByteSize-registerByteSize;

            for (int paramCount = 0; paramCount < inst.getParams().size(); paramCount++, base += 4) {
                var param = inst.getParams().get(paramCount);
                var targetParam = valueManager.getTargetValue(param);
                if (paramCount < 4) {
                    var argReg = Register.REGS.get("a" + paramCount);

                    if (targetParam instanceof Immediate) {
                        asmTarget.addText(new TextInst("li", argReg, targetParam));
                    } else if (targetParam instanceof Offset) {
                        asmTarget.addText(new TextInst("lw", argReg, targetParam));
                    } else if (targetParam instanceof Register) {
                        asmTarget.addText(new TextInst("move", argReg, targetParam));
                    } else {
                        throw new RuntimeException(); //impossible
                    }
                } else {
                    var registerParam = convertToRegister(targetParam);
                    asmTarget.addText(new TextInst("sw", registerParam, new Offset(sp, base)));

                    Register.freeAllTempRegisters(); // TODO: maybe wrong
                }
            }
            asmTarget.addText(new TextInst("addiu", sp, sp, new Immediate(-paramByteSize)));
        }

        asmTarget.addText(new TextInst("addiu", sp, sp, new Immediate(-registerByteSize)));

        asmTarget.addText(new TextInst("jal", new Label(func.getName().substring(1))));

        if (paramByteSize > 0) {
            asmTarget.addText(new TextInst("addiu", sp, sp, new Immediate(paramByteSize)));
        }

        recoverRegistersInFuncCall(registerToReserve);

        asmTarget.addText(new TextInst("addiu", sp, sp, new Immediate(registerByteSize)));

        if (func.getRetType().getType() != IRTypeEnum.VOID) {
            var target = valueManager.getTargetValue(inst);

            if (target instanceof Offset) {
                asmTarget.addText(new TextInst("sw", Register.REGS.get("v0"), target));
            } else if (target instanceof Register) {
                asmTarget.addText(new TextInst("move", target, Register.REGS.get("v0")));
            } else {
                throw new RuntimeException(); // impossible
            }
        }
    }

    private void reserveRegistersInFuncCall(List<Register> registersToReserve) {
        var sp = Register.REGS.get("sp");
        int offset = -registersToReserve.size() * 4;
        for (var register : registersToReserve) {
            asmTarget.addText(new TextInst("sw", register, new Offset(sp, offset)));
            offset += 4;
        }
    }

    private void recoverRegistersInFuncCall(List<Register> registersToRecover) {
        var sp = Register.REGS.get("sp");
        int offset = 0;
        for (var register : registersToRecover) {
            asmTarget.addText(new TextInst("lw", register, new Offset(sp, offset)));
            offset += 4;
        }
    }

    private void translateGetElementPtrInst(GetElementPtrInst inst) {
        var base = inst.getElementBase();
        var offsets = inst.getOffsets();
        var dims = base.getType().getArrayDims();

        Register registerBase = Register.allocateTempRegister();

        if (base instanceof LoadInst || base instanceof GetElementPtrInst) { // if is pointer
            var baseVal = valueManager.getTargetValue(base);
            if (baseVal instanceof Offset) {
                asmTarget.addText(new TextInst("lw", registerBase, valueManager.getTargetValue(base)));
            } else if (baseVal instanceof Register) {
                asmTarget.addText(new TextInst("move", registerBase, valueManager.getTargetValue(base)));
            } else {
                throw new RuntimeException(); // impossible
            }

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

            var offsetVal = valueManager.getTargetValue(offset);

            if (offsetVal instanceof Immediate) {
                asmTarget.addText(new TextInst("li", registerTemp, offsetVal));
            } else if (offsetVal instanceof Offset) {
                asmTarget.addText(new TextInst("lw", registerTemp, offsetVal));
            } else if (offsetVal instanceof Register) {
                asmTarget.addText(new TextInst("move", registerTemp, offsetVal));
            } else {
                throw new RuntimeException(); // impossible
            }

            asmTarget.addText(new TextInst("mul", registerTemp, registerTemp, new Immediate(memSize)));
            asmTarget.addText(new TextInst("addu", registerBase, registerBase, registerTemp));
            currDim++;
        }

        var target = valueManager.getTargetValue(inst);

        if (target instanceof Register) {
            asmTarget.addText(new TextInst("move", target, registerBase));
        } else if (target instanceof Offset) {
            asmTarget.addText(new TextInst("sw", registerBase, target));
        } else {
            throw new RuntimeException(); // impossible
        }
    }

    private void translateZExtInst(ZExtInst inst) {
        var value = valueManager.getTargetValue(inst.getValue());
        var target = valueManager.getTargetValue(inst);

        var registerValue = convertToRegister(value);

        if (target instanceof Offset) {
            asmTarget.addText(new TextInst("sw", registerValue, target));
        } else if (target instanceof Register) {
            asmTarget.addText(new TextInst("move", target, registerValue));
        } else {
            throw new RuntimeException(); // impossible
        }
    }

    private void translateAllocaInst(AllocaInst inst) {
        var targetValue = valueManager.getTargetValue(inst);
        asmTarget.addText(new TextComment(inst.getName() + ": " + targetValue));
    }

    private static String buildBlockLabelName(BasicBlock block) {
        return block.getFunction().getName().substring(1) + "." + block.getName().substring(1);
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
