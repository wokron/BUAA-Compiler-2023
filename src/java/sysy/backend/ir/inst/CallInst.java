package sysy.backend.ir.inst;

import sysy.backend.ir.Function;
import sysy.backend.ir.IRTypeEnum;
import sysy.backend.ir.Value;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class CallInst extends Instruction {
    private final Function func;
    private final List<Value> params = new ArrayList<>();

    public CallInst(Function func, List<Value> params) {
        super(func.getRetType().clone(), params.toArray(new Value[0])); // TODO: func is used as well
        this.func = func;
        this.params.addAll(params);
    }

    public Function getFunc() {
        return func;
    }

    public List<Value> getParams() {
        return params;
    }

    @Override
    public void dump(PrintStream out) {
        out.print("  ");
        if (func.getRetType().getType() != IRTypeEnum.VOID) {
            out.printf("%s = ", getName());
        }
        out.printf("call %s(", func);

        assert params.size() == func.getArguments().size();
        for (int i = 0; i < params.size(); i++) {
            if (i != 0) {
                out.print(", ");
            }
            var currParam = params.get(i);
            out.printf("%s", currParam.toString());
        }

        out.print(")\n");
    }

    @Override
    public void replaceOperand(int pos, Value newOperand) {
        super.replaceOperand(pos, newOperand);
        params.set(pos, newOperand);
    }
}
