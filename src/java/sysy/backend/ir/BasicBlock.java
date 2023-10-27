package sysy.backend.ir;

import sysy.backend.ir.inst.*;

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

    public Value createReturnInst(IRType type, Value value) {
        return insertInstruction(new ReturnInst(type, value));
    }

    public Value createLoadInst(IRType type, Value ptr) {
        return insertInstruction(new LoadInst(type, ptr));
    }

    public Value createStoreInst(IRType type, Value value, Value ptr) {
        return insertInstruction(new StoreInst(type, value, ptr));
    }

    public Value createAllocaInst(IRType type) {
        return insertInstruction(new AllocaInst(type));
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
