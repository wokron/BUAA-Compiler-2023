package sysy.backend.ir.inst;

import sysy.backend.ir.ArrayIRType;
import sysy.backend.ir.BasicIRType;
import sysy.backend.ir.IRType;
import sysy.backend.ir.Value;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class GetElementPtrInst extends Instruction {
    private final Value elementBase;
    private final List<Value> offsets = new ArrayList<>();

    public GetElementPtrInst(Value elementBase, List<Value> offsets) {
        super(getGEPInstType(elementBase.getType(), offsets.size()));
        this.elementBase = elementBase;
        this.offsets.addAll(offsets);
    }

    private static IRType getGEPInstType(IRType elementBaseType, int offsetCount) {
        if (elementBaseType instanceof ArrayIRType arrayIRType) {
            var oriArrayDims = arrayIRType.getArrayDims();
            var rtDims = oriArrayDims.subList(offsetCount-1, oriArrayDims.size());
            if (rtDims.isEmpty()) {
                return new BasicIRType(arrayIRType.getElmType().getType()).ptr(1);
            } else {
                return new ArrayIRType(arrayIRType.getElmType(), rtDims).ptr(1);
            }
        } else {
            assert offsetCount == 1;
            return elementBaseType.clone();
        }
    }

    public List<Value> getOffsets() {
        return offsets;
    }

    public IRType getDataType() {
        var dataType = elementBase.getType().clone().ptr(elementBase.getType().getPtrNum()-1);
        return dataType;
    }

    @Override
    public void dump(PrintStream out) {
        var dataType = elementBase.getType().clone().ptr(elementBase.getType().getPtrNum()-1);
        out.printf("  %s = getelementptr %s, %s",
                getName(),
                dataType,
                elementBase.toString());

        for (Value offset : offsets) {
            out.printf(", %s", offset.toString());
        }
        out.print("\n");
    }
}
