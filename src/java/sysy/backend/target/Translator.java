package sysy.backend.target;

import sysy.backend.ir.*;
import sysy.backend.ir.Module;
import sysy.backend.target.inst.TextLabel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

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
    }
}
