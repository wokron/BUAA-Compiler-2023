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
        valueManager.putLocals(irFunction);

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
        } else if (inst instanceof CallInst) {

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
        asmTarget.addText(new TextInst("jr", Register.REGS.get("ra")));
    }

    private void translateLoadInst(LoadInst inst) {
        var ptr = valueManager.getTargetValue(inst.getPtr());
        var registerPtr = convertToRegister(ptr);
        var target = valueManager.getTargetValue(inst);

        TextInst targetInst = null;
        if (isAddress(target)) {
            var newReg = Register.allocateTempRegister();
            targetInst = new TextInst("sw", newReg, target);
        }

        asmTarget.addText(targetInst);
    }

    private void translateStoreInst(StoreInst inst) {
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
            var falseBranch = inst.getFalseBranch();
            var falseBranchName = buildBlockLabelName(falseBranch);
            var trueBranch = inst.getTrueBranch();
            var trueBranchName = buildBlockLabelName(trueBranch);

            asmTarget.addText(new TextInst("beqz", cond, new Label(falseBranchName)));
            asmTarget.addText(new TextInst("bnez", cond, new Label(trueBranchName)));
        } else {
            var destBranch = inst.getDest();
            var destBranchName = buildBlockLabelName(destBranch);
            asmTarget.addText(new TextInst("j", new Label(destBranchName)));
        }
    }

    private static String buildBlockLabelName(BasicBlock block) {
        return block.getFunction().getName().substring(1) + "_" + block.getName().substring(1);
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
