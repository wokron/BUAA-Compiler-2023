package sysy.backend.optim;

import sysy.backend.ir.*;
import sysy.backend.ir.Module;
import sysy.backend.ir.inst.BinaryInst;

public class ConstFoldPass {
    private final Module irModule;
    private boolean improve = false;

    public ConstFoldPass(Module irModule) {
        this.irModule = irModule;
    }

    public boolean isImprove() {
        return improve;
    }

    public Module pass() {
        for (var func : irModule.getFunctions()) {
            passFunc(func);
        }

        return irModule;
    }

    private void passFunc(Function func) {
        for (var block : func.getBasicBlocks()) {
            passBlock(block);
        }
    }

    private void passBlock(BasicBlock block) {
        var insts = block.getInstructions();
        for (int i = 0; i < insts.size(); i++) {
            var inst = insts.get(i);
            if (inst instanceof BinaryInst binaryInst) {
                Value valueToReplace = getValueToReplace(binaryInst);
                if (valueToReplace != null) {
                    inst.replaceAllUseWith(valueToReplace, false);
                    improve = true;
                    i--;
                }
            }
        }
    }

    private static Value getValueToReplace(BinaryInst binaryInst) {
        var left = binaryInst.getLeft();
        var right = binaryInst.getRight();
        Integer ileft = tryGetImmediateValue(left), iright = tryGetImmediateValue(right);
        Value valueToReplace = null;
        switch (binaryInst.getOp()) {
            case ADD:
                if (ileft != null && iright != null) {
                    valueToReplace = new ImmediateValue(ileft + iright);
                }
                if (ileft != null && ileft == 0) { // 0 + a = a
                    valueToReplace = right;
                }
                if (iright != null && iright == 0) { // a + 0 = a
                    valueToReplace = left;
                }
                break;
            case SUB:
                if (ileft != null && iright != null) {
                    valueToReplace = new ImmediateValue(ileft - iright);
                }
                if (iright != null && iright == 0) { // a - 0 = a
                    valueToReplace = left;
                }
                if (left == right) { // a - a = 0
                    valueToReplace = new ImmediateValue(0);
                }
                break;
            case MUL:
                if (ileft != null && iright != null) {
                    valueToReplace = new ImmediateValue(ileft * iright);
                }
                if (ileft != null && ileft == 0) { // 0 * a = 0
                    valueToReplace = new ImmediateValue(0);
                }
                if (iright != null && iright == 0) { // a * 0 = 0
                    valueToReplace = new ImmediateValue(0);
                }
                if (ileft != null && ileft == 1) { // 1 * a = a
                    valueToReplace = right;
                }
                if (iright != null && iright == 1) { // a * 1 = a
                    valueToReplace = left;
                }
                break;
            case SDIV:
                if (ileft != null && iright != null) {
                    valueToReplace = new ImmediateValue(ileft / iright);
                }
                if (iright != null && iright == 1) { // a / 1 = a
                    valueToReplace = left;
                }
                if (ileft != null && ileft == 0) { // 0 / a = 0
                    valueToReplace = new ImmediateValue(0);
                }
                if (left == right) { // a / a = 1
                    valueToReplace = new ImmediateValue(1);
                }
                break;
            case SREM:
                if (ileft != null && iright != null) {
                    valueToReplace = new ImmediateValue(ileft % iright);
                }
                break;
        }

        return valueToReplace;
    }

    private static Integer tryGetImmediateValue(Value value) {
        if (value instanceof ImmediateValue ivalue) {
            return ivalue.getValue();
        } else {
            return null;
        }
    }
}
