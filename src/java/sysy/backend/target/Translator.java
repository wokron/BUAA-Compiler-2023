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
        asmTarget.addText(new TextLabel(irBlock.getFunction().getName().substring(1) + "_" + irBlock.getName().substring(1)));

        for (var inst : irBlock.getInstructions()) {
            translateInstruction(inst);
        }
    }

    private void translateInstruction(Instruction inst) {
        if (inst instanceof BinaryInst i) {
            translateBinaryInst(i);
        } else if (inst instanceof BrInst) {

        } else if (inst instanceof CallInst) {

        } else if (inst instanceof ICmpInst) {

        } else if (inst instanceof LoadInst) {

        } else if (inst instanceof StoreInst) {

        } else if (inst instanceof ReturnInst) {

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
