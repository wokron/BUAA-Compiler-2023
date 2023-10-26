package sysy.backend.ir;

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

    public GlobalValue createGlobalValue(IRType type, List<Integer> initVals, List<Integer> dims) {
        var newGlobalValue = new GlobalValue(type, initVals, dims);
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
        }
    }

    public static void main(String[] args) {
        var module = new Module();
        var argTypes = new ArrayList<IRType>();
        argTypes.add(IRType.getInt().ptr(1));
        argTypes.add(IRType.getInt());
        var func1 = module.createFunction(IRType.getInt(), argTypes);
        func1.setName("func1");

        var initVal = new ArrayList<Integer>();
        initVal.add(1);
        var global = module.createGlobalValue(IRType.getInt(), initVal, new ArrayList<>());
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

        b1.createReturnInst(IRType.getInt(), i3);

        var b2 = func1.createBasicBlock();
        b2.setName("b2");

        b2.createReturnInst(IRType.getVoid(), null);

        module.dump(System.out);
    }
}
