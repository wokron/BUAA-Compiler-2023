package sysy.backend.ir;

import sysy.backend.ir.inst.BrInst;
import sysy.backend.ir.inst.ICmpInstCond;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class Module {
    List<GlobalValue> globalValues = new ArrayList<>();
    List<Function> functions = new ArrayList<>();

    public List<GlobalValue> getGlobalValues() {
        return globalValues;
    }

    public List<Function> getFunctions() {
        return functions;
    }

    public GlobalValue createGlobalValue(IRType type, List<Integer> initVals) {
        var newGlobalValue = new GlobalValue(type, initVals);
        globalValues.add(newGlobalValue);
        return newGlobalValue;
    }

    public Function createFunction(IRType retType, List<IRType> argTypes) {
        var newFunction = new Function(retType, argTypes);
        functions.add(newFunction);
        return newFunction;
    }

    public void dump(PrintStream out) {
        for (var globalVal : globalValues) {
            globalVal.dump(out);
        }

        for (var func : functions) {
            func.dump(out);
            NameAllocator.getInstance().reset();
        }
    }

    public static void main(String[] args) {
        testValue();
//        testBasic();
//        testIfElse();
//        testShortCircuit();
//        testType();
        testGetElementPtr();
    }

    private static void testValue() {

    }

    private static void testType() {
        System.out.println(IRType.getInt().dims(List.of(6)));

        System.out.println(IRType.getInt().dims(List.of(3, 3)));

        List<Integer> dims = new ArrayList<>();
        dims.add(null);
        System.out.println(IRType.getInt().dims(dims));

        dims = new ArrayList<>();
        dims.add(null);
        dims.add(3);
        System.out.println(IRType.getInt().dims(dims));

        var arr1 = IRType.getInt().dims(List.of(6));
        System.out.println(arr1.initValsToString(List.of(1, 2, 3, 4, 5, 6)));

        var arr2 = IRType.getInt().dims(List.of(3, 3));
        System.out.println(arr2.initValsToString(List.of(0, 0, 0, 0, 5, 6, 0, 0, 0)));

        var justVar = IRType.getInt();
        System.out.println(justVar.initValsToString(List.of(0)));

        // ir type after refactor
        System.out.println(IRType.getInt().ptr(1).dims(List.of(1, 2)).ptr(1));

        System.out.println(IRType.getInt().dims(List.of(2, 2)).initValsToString(List.of(1, 2, 3, 4)));
        System.out.println(IRType.getInt().dims(List.of(2, 2)).initValsToString(List.of(0, 0, 3, 4)));
        System.out.println(IRType.getInt().dims(List.of(2, 2)).initValsToString(List.of()));

        System.out.println(IRType.getInt().initValsToString(List.of(10)));
        System.out.println(IRType.getInt().initValsToString(List.of(0)));
        System.out.println(IRType.getInt().initValsToString(List.of()));
    }

    private static void testBasic() {
        var module = new Module();
        var argTypes = new ArrayList<IRType>();
        argTypes.add(IRType.getInt().ptr(1));
        argTypes.add(IRType.getInt());
        var func1 = module.createFunction(IRType.getInt(), argTypes);
        func1.setName("func1");

        var initVal = new ArrayList<Integer>();
        initVal.add(1);
        var global = module.createGlobalValue(IRType.getInt(), initVal);
        global.setName("g1");

        int i = 0;
        for (var arg : func1.getArguments()) {
            arg.setName("" + i);
            i++;
        }

        var b1 = func1.createBasicBlock();
        b1.setName("b1");

        var i1 = b1.createAddInst(new ImmediateValue(1), new ImmediateValue(10));
        i1.setName("2");
        var i2 = b1.createMulInst(i1, new ImmediateValue(20));
        i2.setName("3");

        var i3 = b1.createMulInst(i2, global);
        i3.setName("4");

        var i4 = b1.createAllocaInst(IRType.getInt());

        b1.createReturnInst(i3);

        var b2 = func1.createBasicBlock();
        b2.setName("b2");

        b2.createStoreInst(new ImmediateValue(10), i4);

        var load = b2.createLoadInst(global);
        load.setName("5");

        var call = b2.createCallInst(Function.BUILD_IN_PUTSTR, List.of(load));

        b2.createReturnInst(null);

        // if (1 < 1) { putint(1); }
        var cond = b2.createICmpInst(ICmpInstCond.EQ, new ImmediateValue(1), new ImmediateValue(1));
        var trueBlock = func1.createBasicBlock();
        trueBlock.createCallInst(Function.BUILD_IN_PUTINT, List.of(new ImmediateValue(1)));
        var finishBlock = func1.createBasicBlock();
        b2.createBrInstWithCond(cond, trueBlock, finishBlock);

        // if (i < 1) { putint(1); } else { putint(2); }
        var cond2 = finishBlock.createICmpInst(ICmpInstCond.EQ, new ImmediateValue(1), new ImmediateValue(1));
        var trueBlock2 = func1.createBasicBlock();
        trueBlock2.createCallInst(Function.BUILD_IN_PUTINT, List.of(new ImmediateValue(1)));
        var falseBlock2 = func1.createBasicBlock();
        falseBlock2.createCallInst(Function.BUILD_IN_PUTINT, List.of(new ImmediateValue(1)));
        finishBlock.createBrInstWithCond(cond, trueBlock2, falseBlock2);
        var finishBlock2 = func1.createBasicBlock();
        trueBlock2.createBrInstWithoutCond(finishBlock2);

        module.dump(System.out);
    }

    private static void testIfElse() {
        var module = new Module();
        var func1 = module.createFunction(IRType.getVoid(), List.of());

        var currBlock = func1.createBasicBlock();
//        if (1 < 1) {
//            putint(1);
//            if (1 > 1) {
//                putint(2);
//            } else {
//                putint(3);
//            }
//        }  else {
//            putint(4);
//        }

        var cond1 = currBlock.createICmpInst(ICmpInstCond.SLT, new ImmediateValue(1), new ImmediateValue(1)); // read cond
        var condBlock1 = currBlock;

        currBlock = func1.createBasicBlock(); // create new block and visit true stmt
        var trueBlock1 = currBlock;
        currBlock.createCallInst(Function.BUILD_IN_PUTINT, List.of(new ImmediateValue(1)));

        var cond2 = currBlock.createICmpInst(ICmpInstCond.SGT, new ImmediateValue(1), new ImmediateValue(1)); // read cond
        var condBlock2 = currBlock;

        currBlock = func1.createBasicBlock(); // create new block and visit true stmt
        var trueBlock2 = currBlock;
        currBlock.createCallInst(Function.BUILD_IN_PUTINT, List.of(new ImmediateValue(2)));
        var lastInTrue2 = currBlock;

        currBlock = func1.createBasicBlock(); // return, create new block and visit false stmt
        var falseBlock2 = currBlock;
        currBlock.createCallInst(Function.BUILD_IN_PUTINT, List.of(new ImmediateValue(3)));

        condBlock2.createBrInstWithCond(cond2, trueBlock2, falseBlock2); // return, add br inst to cond block
        currBlock = func1.createBasicBlock(); // block after if-else
        lastInTrue2.createBrInstWithoutCond(currBlock); // add br inst to true block
        var lastInTrue1 = currBlock;

        currBlock = func1.createBasicBlock(); // return, create new block and visit false stmt
        var falseBlock1 = currBlock;
        currBlock.createCallInst(Function.BUILD_IN_PUTINT, List.of(new ImmediateValue(4)));

        condBlock1.createBrInstWithCond(cond1, trueBlock1, falseBlock1); // return, add br inst to cond block
        currBlock = func1.createBasicBlock(); // block after if-else
        lastInTrue1.createBrInstWithoutCond(currBlock); // and br inst to true block

        System.out.print("\n\ntest if-else stmt:\n");
        module.dump(System.out);
    }

    private static void testShortCircuit() {
        var module = new Module();
        var func1 = module.createFunction(IRType.getVoid(), List.of());

        List<BasicBlock> toFalse;
        List<BasicBlock> toTrue = new ArrayList<>();

        var currBlock = func1.createBasicBlock();
//        if (1 == 1 || 2 <= 2 && 3 >= 3 || 4 <= 4 && 5 >= 5) {
//            putint(1);
//        } else {
//            putint(2);
//        }

        var cond1 = currBlock.createICmpInst(ICmpInstCond.EQ, new ImmediateValue(1), new ImmediateValue(1));
        var br1 = (BrInst)currBlock.createBrInstWithCond(cond1, null, null);
        var andList1 = List.of(currBlock);
        currBlock = func1.createBasicBlock();
        var lastAndList1 = andList1;
        toFalse = lastAndList1;
        toTrue.add(andList1.get(andList1.size()-1)); // add b1

        List<BasicBlock> andList2 = new ArrayList<>();
        var cond2 = currBlock.createICmpInst(ICmpInstCond.SLE, new ImmediateValue(2), new ImmediateValue(2));
        var br2 = (BrInst)currBlock.createBrInstWithCond(cond2, null, null);
        andList2.add(currBlock);
        currBlock = func1.createBasicBlock();

        var cond3 = currBlock.createICmpInst(ICmpInstCond.SGE, new ImmediateValue(3), new ImmediateValue(3));
        var br3 = (BrInst)currBlock.createBrInstWithCond(cond3, null, null);
        br2.setTrueBranch(currBlock);
        andList2.add(currBlock);
        currBlock = func1.createBasicBlock();

//        for (var lastAnd : lastAndList1) { // br1
//            lastAnd.setFalseBranch(andList2.get(0));
//        }
        br1.setFalseBranch(andList2.get(0));

        var lastAndList2 = andList2;
        toFalse = lastAndList2;
        toTrue.add(andList2.get(andList2.size()-1)); // add b3

        List<BasicBlock> andList3 = new ArrayList<>();
        var cond4 = currBlock.createICmpInst(ICmpInstCond.SLE, new ImmediateValue(4), new ImmediateValue(4));
        var br4 = (BrInst)currBlock.createBrInstWithCond(cond4, null, null);
        andList3.add(currBlock);
        currBlock = func1.createBasicBlock();

        var cond5 = currBlock.createICmpInst(ICmpInstCond.SGE, new ImmediateValue(5), new ImmediateValue(5));
        var br5 = (BrInst)currBlock.createBrInstWithCond(cond5, null, null);
        andList3.add(currBlock);
        br4.setTrueBranch(currBlock);
        currBlock = func1.createBasicBlock();

//        for (var lastAnd : lastAndList2)
        br2.setFalseBranch(andList3.get(0));
        br3.setFalseBranch(andList3.get(0));

        var lastAndList3 = andList3;
        toFalse = lastAndList3; // now is b4,b5
        toTrue.add(andList3.get(andList3.size()-1)); // add b5

        br1.setTrueBranch(currBlock);
        br3.setTrueBranch(currBlock);
        br5.setTrueBranch(currBlock);

        currBlock.createCallInst(Function.BUILD_IN_PUTINT, List.of(new ImmediateValue(1)));

        var lastBlockInTrue = currBlock;

        currBlock = func1.createBasicBlock();

        br4.setFalseBranch(currBlock);
        br5.setFalseBranch(currBlock);

        currBlock.createCallInst(Function.BUILD_IN_PUTINT, List.of(new ImmediateValue(2)));

        currBlock = func1.createBasicBlock();

        lastBlockInTrue.createBrInstWithoutCond(currBlock);

        System.out.print("\n\ntest short circuit:\n");
        module.dump(System.out);
    }

    private static void testGetElementPtr() {
        var module = new Module();
        var func1 = module.createFunction(IRType.getVoid(), List.of());

        var b1 = func1.createBasicBlock();

        var arr1 = b1.createAllocaInst(IRType.getInt().dims(List.of(2, 3)));
        var tmp1 = b1.createGetElementPtrInst(arr1, List.of(new ImmediateValue(0)));
        b1.createGetElementPtrInst(tmp1, List.of(new ImmediateValue(0)));

        module.dump(System.out);
    }
}
