package sysy.backend.target.value;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    static {
        var registersName = List.of("v0", "v1", "a0", "a1", "a2", "a3", "t0", "t1", "t2", "t3", "t4", "t5", "t6", "t7", "s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7", "sp", "fp","ra");
        for (var name : registersName) {
            REGS.put(name, new Register(name));
        }
    }
}
