package sysy.backend.ir;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class BasicBlock extends Value {
    private final List<Instruction> instructions = new ArrayList<>();

    private Value insertInstruction(Instruction inst) {
        instructions.add(inst);
        return inst;
    }

    public List<Instruction> getInstructions() {
        return instructions;
    }

    public void dump(PrintStream out) {
        out.printf("%s:\n", getName());

        for (var inst : instructions) {
            inst.dump(out);
        }

        out.print("\n");
    }
}
