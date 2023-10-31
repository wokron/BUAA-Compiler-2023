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
        this.func = func;
        this.params.addAll(params);
    }

    @Override
    public void dump(PrintStream out) {
        out.print("  ");
        if (func.getRetType().getType() != IRTypeEnum.VOID) {
            out.printf("%s = ", getName());
        }
        out.printf("call %s %s(", func.getRetType().toString(), func.getName());

        assert params.size() == func.getArguments().size();
        for (int i = 0; i < params.size(); i++) {
            if (i != 0) {
                out.print(", ");
            }
            var currArg = func.getArguments().get(i);
            var currParam = params.get(i);
            out.printf("%s %s", currArg.getType().toString(), currParam.getName());
        }

        out.print(")\n");
    }
}
