package sysy.backend.target;

import sysy.backend.ir.*;
import sysy.backend.ir.Module;
import sysy.backend.ir.inst.*;
import sysy.backend.target.inst.TextInst;
import sysy.backend.target.inst.TextLabel;
import sysy.backend.target.value.*;

import java.util.*;

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
        if (initVals.isEmpty()) {
            if (irGlobalValue.getType() instanceof ArrayIRType arrayType) {
                initVals = new ArrayList<>(Collections.nCopies(arrayType.getTotalSize(), 0));
            } else {
                initVals = new ArrayList<>();
                initVals.add(0);
            }
        }
        var newDataEntry = new Data(irGlobalValue.getName().substring(1), "word", Arrays.asList(initVals.toArray()));
        asmTarget.addData(newDataEntry);
        valueManager.putGlobal(irGlobalValue, newDataEntry.getLabel());
    }

    private void translateFunction(Function irFunction) {
        asmTarget.addText(new TextLabel(irFunction.getName().substring(1)));
        var totalMemorySize = valueManager.putLocals(irFunction);
        memorySizeForLocal = totalMemorySize - irFunction.calcParamSpace();

        if (memorySizeForLocal > 0) {
            var sp = Register.REGS.get("sp");
            asmTarget.addText(new TextInst("subu", sp, sp, new Immediate(memorySizeForLocal)));
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
        }
        Register.freeAllTempRegisters();
    }

    private void translateBinaryInst(BinaryInst inst) {
        var left = valueManager.getTargetValue(inst.getLeft());
        var right = valueManager.getTargetValue(inst.getRight());
        var target = valueManager.getTargetValue(inst);

        var registerLeft = convertToRegister(left);
        var registerRight = convertToRegister(right);

        TextInst targetInst = null;
        Register registerTarget = null;
        if (isAddress(target)) {
            var newReg = Register.allocateTempRegister();
            targetInst = new TextInst("sw", newReg, target);
            registerTarget = newReg;
        } else if (target instanceof Register regTarget) {
            registerTarget = regTarget;
        }

        switch (inst.getOp()) {
            case ADD:
            case SUB:
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

        if (targetInst != null) {
            asmTarget.addText(targetInst);
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
        var ptr = valueManager.getTargetValue(inst.getPtr());
        var registerPtr = convertToRegister(ptr);
        var target = valueManager.getTargetValue(inst);

        TextInst targetInst = null;
        if (isAddress(target)) {
            targetInst = new TextInst("sw", registerPtr, target);
        }

        asmTarget.addText(targetInst);
    }

    private void translateStoreInst(StoreInst inst) {
        if (inst.getValue() instanceof FunctionArgument) {
            return;
        }

        var ptr = valueManager.getTargetValue(inst.getPtr());
        var value = valueManager.getTargetValue(inst.getValue());

        var registerValue = convertToRegister(value);

        asmTarget.addText(new TextInst("sw", registerValue, ptr));
    }

    private void translateICmpInst(ICmpInst inst) {
        var left = valueManager.getTargetValue(inst.getLeft());
        var right = valueManager.getTargetValue(inst.getRight());
        var target = valueManager.getTargetValue(inst);

        var registerLeft = convertToRegister(left);
        var registerRight = convertToRegister(right);

        TextInst targetInst = null;
        Register registerTarget = null;
        if (isAddress(target)) {
            var newReg = Register.allocateTempRegister();
            targetInst = new TextInst("sw", newReg, target);
            registerTarget = newReg;
        } else if (target instanceof Register regTarget) {
            registerTarget = regTarget;
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

        if (targetInst != null) {
            asmTarget.addText(targetInst);
        }
    }

    private void translateBrInst(BrInst inst) {
        if (inst.getCond() != null) {
            var cond = valueManager.getTargetValue(inst.getCond());
            var registerCond = convertToRegister(cond);
            var falseBranch = inst.getFalseBranch();
            var falseBranchName = buildBlockLabelName(falseBranch);
            var trueBranch = inst.getTrueBranch();
            var trueBranchName = buildBlockLabelName(trueBranch);

            asmTarget.addText(new TextInst("beqz", registerCond, new Label(falseBranchName)));
            asmTarget.addText(new TextInst("bnez", registerCond, new Label(trueBranchName)));
        } else {
            var destBranch = inst.getDest();
            var destBranchName = buildBlockLabelName(destBranch);
            asmTarget.addText(new TextInst("j", new Label(destBranchName)));
        }
    }

    private void translateCallInst(CallInst inst) {
        var func = inst.getFunc();
        if (func == Function.BUILD_IN_PUTINT || func == Function.BUILD_IN_PUTCH) {
            var inputVal = inst.getParams().get(0);
            var inputTargetValue = valueManager.getTargetValue(inputVal);
            var registerInputVal = convertToRegister(inputTargetValue);
            asmTarget.addText(new TextInst("li", Register.REGS.get("v0"), new Immediate(func == Function.BUILD_IN_PUTINT ? 1 : 11)));
            asmTarget.addText(new TextInst("move", Register.REGS.get("a0"), registerInputVal));
            asmTarget.addText(new TextInst("syscall"));
        } else if (func == Function.BUILD_IN_GETINT) {
            var target = valueManager.getTargetValue(inst);
            TextInst targetInst = null;
            Register registerTarget = null;
            if (isAddress(target)) {
                var newReg = Register.allocateTempRegister();
                targetInst = new TextInst("sw", newReg, target);
                registerTarget = newReg;
            } else if (target instanceof Register regTarget) {
                registerTarget = regTarget;
            }

            asmTarget.addText(new TextInst("li", Register.REGS.get("v0"), new Immediate(5)));
            asmTarget.addText(new TextInst("syscall"));
            asmTarget.addText(new TextInst("move", registerTarget, Register.REGS.get("v0")));
            asmTarget.addText(targetInst);
        } else { // common func
            int paramByteSize = func.calcParamSpace();
            var sp = Register.REGS.get("sp");
            asmTarget.addText(new TextInst("sw", Register.REGS.get("ra"), new Offset(sp, -4)));

            if (paramByteSize > 0) {
                int base = -paramByteSize-4;
                for (var param : inst.getParams()) {
                    var registerParam = convertToRegister(valueManager.getTargetValue(param));
                    asmTarget.addText(new TextInst("sw", registerParam, new Offset(sp, base)));

                    Register.freeAllTempRegisters(); // TODO: maybe wrong
                    base += 4;
                }
                asmTarget.addText(new TextInst("subu", sp, sp, new Immediate(paramByteSize)));
            }

            asmTarget.addText(new TextInst("subu", sp, sp, new Immediate(4)));

            asmTarget.addText(new TextInst("jal", new Label(func.getName().substring(1))));

            if (paramByteSize > 0) {
                asmTarget.addText(new TextInst("addiu", sp, sp, new Immediate(paramByteSize)));
            }

            asmTarget.addText(new TextInst("lw", Register.REGS.get("ra"), new Offset(sp, 0)));
            asmTarget.addText(new TextInst("addiu", sp, sp, new Immediate(4)));

            if (func.getRetType().getType() != IRTypeEnum.VOID) {
                var target = valueManager.getTargetValue(inst);

                if (isAddress(target)) {
                    asmTarget.addText(new TextInst("sw", Register.REGS.get("v0"), target));
                } else {
                    asmTarget.addText(new TextInst("move", target, Register.REGS.get("v0")));
                }
            }
        }
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
