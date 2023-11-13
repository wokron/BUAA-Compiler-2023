package sysy.backend.target.value;

import java.util.*;

public class Register extends TargetValue {
    private final String name;

    public Register(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "$" + name;
    }

    public final static Map<String, Register> REGS = new HashMap<>();
    private final static Stack<Register> TEMP_REGS = new Stack<>();
    private final static Stack<Register> TEMP_REGS_ON_USE = new Stack<>();

    static {
        var registersName = List.of(
                "v0", "v1", "a0", "a1", "a2", "a3",
                "t0", "t1", "t2", "t3", "t4", "t5", "t6", "t7",
                "s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7",
                "sp", "fp","ra"
        );

        var tempRegistersName = List.of("t5", "t6", "t7"); // 3 temp register is enough to translate inst from ir to asm

        for (var name : registersName) {
            var reg = new Register(name);
            REGS.put(name, reg);
            if (tempRegistersName.contains(name)) {
                TEMP_REGS.add(reg);
            }
        }
    }

    public static Register allocateTempRegister() {
        if (TEMP_REGS.isEmpty()) {
            return null;
        }
        var allocReg = TEMP_REGS.pop();
        TEMP_REGS_ON_USE.push(allocReg);
        return allocReg;
    }

    public static void freeAllTempRegisters() {
        while (!TEMP_REGS_ON_USE.isEmpty()) {
            TEMP_REGS.push(TEMP_REGS_ON_USE.pop());
        }
    }
}
