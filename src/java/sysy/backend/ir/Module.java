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

        int i = 0;
        for (var arg : func1.getArguments()) {
            arg.setName("" + i);
            i++;
        }

        var b1 = func1.createBasicBlock();
        b1.setName("b1");
        var b2 = func1.createBasicBlock();
        b2.setName("b2");


        var initVal = new ArrayList<Integer>();
        initVal.add(1);
        var global = module.createGlobalValue(IRType.getInt(), initVal, new ArrayList<>());
        global.setName("g1");

        module.dump(System.out);
    }
}
