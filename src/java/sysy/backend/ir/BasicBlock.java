package sysy.backend.ir;

import sysy.backend.ir.inst.*;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class BasicBlock extends Value {
    private final List<Instruction> instructions = new ArrayList<>();
    private final Function function;
    private int loopNum = 0;

    public BasicBlock(Function belongFunc) {
        super(new BasicIRType(IRTypeEnum.LABEL));
        this.function = belongFunc;
    }

    public int getLoopNum() {
        return loopNum;
    }

    public void setLoopNum(int loopNum) {
        this.loopNum = loopNum;
    }

    public Function getFunction() {
        return function;
    }

    public BasicBlock getNextBasicBlock() {
        var blocks = function.getBasicBlocks();
        var idx = blocks.indexOf(this);
        if (idx == -1 || idx == blocks.size()-1) {
            return null;
        } else {
            return blocks.get(idx+1);
        }
    }

    private Value insertInstruction(Instruction inst) {
        instructions.add(inst);
        inst.setBasicBlock(this);
        return inst;
    }

    private Integer tryGetImmediateValue(Value value) {
        if (value instanceof ImmediateValue ivalue) {
            return ivalue.getValue();
        } else {
            return null;
        }
    }

    public Value createAddInst(Value left, Value right) {
        Integer ileft = tryGetImmediateValue(left), iright = tryGetImmediateValue(right);
        if (ileft != null && iright != null) {
            return new ImmediateValue(ileft + iright);
        }
        if (ileft != null && ileft == 0) { // 0 + a = a
            return right;
        }
        if (iright != null && iright == 0) { // a + 0 = a
            return left;
        }

        return insertInstruction(new BinaryInst(BinaryInstOp.ADD, left, right));
    }

    public Value createSubInst(Value left, Value right) {
        Integer ileft = tryGetImmediateValue(left), iright = tryGetImmediateValue(right);
        if (ileft != null && iright != null) {
            return new ImmediateValue(ileft - iright);
        }
        if (iright != null && iright == 0) { // a - 0 = a
            return left;
        }
        if (left == right) { // a - a = 0
            return new ImmediateValue(0);
        }

        return insertInstruction(new BinaryInst(BinaryInstOp.SUB, left, right));
    }

    public Value createMulInst(Value left, Value right) {
        Integer ileft = tryGetImmediateValue(left), iright = tryGetImmediateValue(right);
        if (ileft != null && iright != null) {
            return new ImmediateValue(ileft * iright);
        }
        if (ileft != null && ileft == 0) { // 0 * a = 0
            return new ImmediateValue(0);
        }
        if (iright != null && iright == 0) { // a * 0 = 0
            return new ImmediateValue(0);
        }
        if (ileft != null && ileft == 1) { // 1 * a = a
            return right;
        }
        if (iright != null && iright == 1) { // a * 1 = a
            return left;
        }

        return insertInstruction(new BinaryInst(BinaryInstOp.MUL, left, right));
    }

    public Value createSDivInst(Value left, Value right) {
        Integer ileft = tryGetImmediateValue(left), iright = tryGetImmediateValue(right);
        if (ileft != null && iright != null) {
            return new ImmediateValue(ileft / iright);
        }
        if (iright != null && iright == 1) { // a / 1 = a
            return left;
        }
        if (ileft != null && ileft == 0) { // 0 / a = 0
            return new ImmediateValue(0);
        }
        if (left == right) { // a / a = 1
            return new ImmediateValue(1);
        }

        return insertInstruction(new BinaryInst(BinaryInstOp.SDIV, left, right));
    }

    public Value createSRemInst(Value left, Value right) {
        Integer ileft = tryGetImmediateValue(left), iright = tryGetImmediateValue(right);
        if (ileft != null && iright != null) {
            return new ImmediateValue(ileft % iright);
        }
        return insertInstruction(new BinaryInst(BinaryInstOp.SREM, left, right));
    }

    public Value createReturnInst(Value value) {
        var inst = value == null ? new ReturnInst() : new ReturnInst(value);
        return insertInstruction(inst);
    }

    public Value createLoadInst(Value ptr) {
        return insertInstruction(new LoadInst(ptr));
    }

    public Value createStoreInst(Value value, Value ptr) {
        return insertInstruction(new StoreInst(value, ptr));
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
        allocaInst.setBasicBlock(this);
        return allocaInst;
    }

    public Value createICmpInst(ICmpInstCond cond, Value left, Value right) {
        return insertInstruction(new ICmpInst(cond, left, right));
    }

    public Value createBrInstWithCond(Value cond, BasicBlock ifTrue, BasicBlock ifFalse) {
        return insertInstruction(new BrInst(cond, ifTrue, ifFalse));
    }

    public Value createBrInstWithoutCond(BasicBlock dest) {
        return insertInstruction(new BrInst(dest));
    }

    public Value createGetElementPtrInst(Value elementBase, List<Value> offsets) {
        return insertInstruction(new GetElementPtrInst(elementBase, offsets));
    }

    public Value createZExtInst(IRType dstType, Value value) {
        return insertInstruction(new ZExtInst(dstType, value));
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
