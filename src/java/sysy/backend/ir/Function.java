package sysy.backend.ir;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class Function extends Value {
    private final IRType retType;
    private final List<FunctionArgument> arguments = new ArrayList<>();
    private final List<BasicBlock> basicBlocks = new ArrayList<>();

    public Function(IRType retType, List<IRType> argTypes) {
        this.retType = retType;
        for (var argType : argTypes) {
            arguments.add(new FunctionArgument(argType));
        }
    }

    public IRType getRetType() {
        return retType;
    }

    public List<FunctionArgument> getArguments() {
        return arguments;
    }

    public List<BasicBlock> getBasicBlocks() {
        return basicBlocks;
    }

    public BasicBlock createBasicBlock() {
        var newBlock = new BasicBlock();
        basicBlocks.add(newBlock);
        return newBlock;
    }

    @Override
    public String getName() {
        return "@" + super.getName();
    }

    public void dump(PrintStream out) {
        out.printf("define dso_local %s %s(", retType.toString(), getName());

        boolean isFirst = true;
        for (var arg : arguments) {
            if (isFirst) {
                isFirst = false;
            } else {
                out.print(", ");
            }
            arg.dump(out);
        }

        out.print(") {\n");

        for (var block : basicBlocks) {
            block.dump(out);
        }

        out.print("}\n");
    }
}
