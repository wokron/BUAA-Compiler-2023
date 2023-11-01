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

    public Value createSRemInst(Value left, Value right) {
        return insertInstruction(new BinaryInst(BinaryInstOp.SREM, left, right, IRType.getInt()));
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

    public Value createCallInst(Function func, List<Value> params) {
        return insertInstruction(new CallInst(func, params));
    }

    public Value createAllocaInst(IRType type) {
        return insertInstruction(new AllocaInst(type));
    }

    public Value createAllocaInstAndInsertToFront(IRType type) {
        var allocaInst = new AllocaInst(type);
        int insertPos;
        for (insertPos = 0; insertPos < instructions.size() && instructions.get(insertPos) instanceof AllocaInst; insertPos++);
        instructions.add(insertPos, allocaInst);
        return allocaInst;
    }

    public Value createICmpInst(ICmpInstCond cond, Value left, Value right) {
        return insertInstruction(new ICmpInst(cond, left, right, IRType.getInt()));
    }

    public Value createBrInstWithCond(Value cond, BasicBlock ifTrue, BasicBlock ifFalse) {
        return insertInstruction(new BrInst(cond, ifTrue, ifFalse));
    }

    public Value createBrInstWithoutCond(BasicBlock dest) {
        return insertInstruction(new BrInst(dest));
    }

    public Value createGetElementPtrInst(IRType type, Value elementBase, List<Value> offsets) {
        return insertInstruction(new GetElementPtrInst(type, elementBase, offsets));
    }

    public Value createZExtInst(IRType dstType, IRType srcType, Value value) {
        return insertInstruction(new ZExtInst(dstType, srcType, value));
    }

    public List<Instruction> getInstructions() {
        return instructions;
    }

    @Override
    public String getName() {
        return "%b" + super.getName();
    }

    public void dump(PrintStream out) {
        out.printf("b%s:\n", super.getName());

        for (var inst : instructions) {
            inst.dump(out);
        }

        out.print("\n");
    }
}
