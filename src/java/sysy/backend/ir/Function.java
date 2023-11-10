package sysy.backend.ir;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class Function extends Value {
    private final IRType retType;
    private final List<FunctionArgument> arguments = new ArrayList<>();
    private final List<BasicBlock> basicBlocks = new ArrayList<>();

    public static Function BUILD_IN_GETINT = new Function(IRType.getInt(), List.of());
    public static Function BUILD_IN_PUTINT = new Function(IRType.getVoid(), List.of(IRType.getInt()));
    public static Function BUILD_IN_PUTCH = new Function(IRType.getVoid(), List.of(IRType.getInt()));
    public static Function BUILD_IN_PUTSTR = new Function(IRType.getVoid(), List.of(IRType.getChar().ptr(1)));

    static {
        BUILD_IN_GETINT.setName("getint");
        BUILD_IN_PUTINT.setName("putint");
        BUILD_IN_PUTCH.setName("putch");
        BUILD_IN_PUTSTR.setName("putstr");
    }

    public Function(IRType retType, List<IRType> argTypes) {
        super(retType);
        this.retType = retType;
        for (var argType : argTypes) {
            arguments.add(new FunctionArgument(argType));
        }
    }

    public BasicBlock getFirstBasicBlock() {
        if (basicBlocks.isEmpty()) {
            return null;
        } else {
            return basicBlocks.get(0);
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
        var newBlock = new BasicBlock(this);
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

    public int calcParamSpace() {
        return arguments.size() * 4;
    }
}
