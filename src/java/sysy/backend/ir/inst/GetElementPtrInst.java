package sysy.backend.ir.inst;

import sysy.backend.ir.ArrayIRType;
import sysy.backend.ir.BasicIRType;
import sysy.backend.ir.IRType;
import sysy.backend.ir.Value;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class GetElementPtrInst extends Instruction {
    private final IRType dataType;
    private final Value elementBase;
    private final List<Value> offsets = new ArrayList<>();

    public GetElementPtrInst(IRType dataType, Value elementBase, List<Value> offsets) {
        super(getGEPInstType(dataType.clone().ptr(dataType.getPtrNum()+1), offsets.size()));
        this.dataType = dataType;
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
                return new ArrayIRType(arrayIRType.getElmType(), rtDims);
            }
        } else {
            assert false;
            return null; // impossible
        }
    }

    public List<Value> getOffsets() {
        return offsets;
    }

    public IRType getDataType() {
        return dataType;
    }

    @Override
    public void dump(PrintStream out) {
        out.printf("  %s = getelementptr %s, %s* %s",
                getName(),
                dataType,
                dataType,
                elementBase.getName());

        for (Value offset : offsets) {
            out.printf(", i32 %s", offset.getName());
        }
        out.print("\n");
    }
}
