package sysy.backend.ir;

import sysy.backend.ir.inst.BinaryInst;
import sysy.backend.ir.inst.BinaryInstOp;
import sysy.backend.ir.inst.Instruction;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class BasicBlock extends Value {
    private final List<Instruction> instructions = new ArrayList<>();

    private Value insertInstruction(Instruction inst) {
        instructions.add(inst);
        return inst;
    }

    public Value createAddInst(Value left, Value right) {
        return insertInstruction(new BinaryInst(BinaryInstOp.ADD, left, right, IRType.getInt()));
    }

    public Value createSubInst(Value left, Value right) {
        return insertInstruction(new BinaryInst(BinaryInstOp.SUB, left, right, IRType.getInt()));
    }

    public Value createMulInst(Value left, Value right) {
        return insertInstruction(new BinaryInst(BinaryInstOp.MUL, left, right, IRType.getInt()));
    }

    public Value createSDivInst(Value left, Value right) {
        return insertInstruction(new BinaryInst(BinaryInstOp.SDIV, left, right, IRType.getInt()));
    }

    public Value createAndInst(Value left, Value right) {
        return insertInstruction(new BinaryInst(BinaryInstOp.AND, left, right, IRType.getInt()));
    }

    public Value createOrInst(Value left, Value right) {
        return insertInstruction(new BinaryInst(BinaryInstOp.OR, left, right, IRType.getInt()));
    }

    public List<Instruction> getInstructions() {
        return instructions;
    }

    @Override
    public String getName() {
        return "%" + super.getName();
    }

    public void dump(PrintStream out) {
        out.printf("%s:\n", super.getName());

        for (var inst : instructions) {
            inst.dump(out);
        }

        out.print("\n");
    }
}
