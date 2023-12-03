package sysy.backend.target;

import sysy.backend.target.inst.TextInst;
import sysy.backend.target.value.Immediate;
import sysy.backend.target.value.Offset;
import sysy.backend.target.value.Register;

import java.util.*;

public class TempRegisterPool {
    private final Target target;
    private final List<Register> registers = new ArrayList<>();
    private final Stack<Register> registersUnused = new Stack<>();
    private final Map<Offset, Register> addrRegisterMap = new HashMap<>();
    private final Map<Register, Offset> registerAddrMap = new HashMap<>();
    private final Queue<Register> timeQueue = new ArrayDeque<>();

    public TempRegisterPool(Target target, List<Register> registers) {
        this.target = target;
        this.registers.addAll(registers);
        this.registersUnused.addAll(registers);
    }

    public Register allocTempRegister(Offset addr, boolean firstTime) {
        if (hasBeenAllocated(addr)) {
            var reg = getRegister(addr);
            timeQueue.remove(reg);
            timeQueue.add(reg);
            return getRegister(addr);
        }

        if (registersUnused.isEmpty()) {
            Register regToKill = timeQueue.poll();
            var regToKillAddr = registerAddrMap.get(regToKill);
            addrRegisterMap.remove(regToKillAddr);
            registerAddrMap.remove(regToKill);
            target.addText(new TextInst("sw", regToKill, regToKillAddr));
            registersUnused.add(regToKill);
        }

        Register regToAlloc = registersUnused.pop();
        timeQueue.add(regToAlloc);

        if (!firstTime) {
            target.addText(new TextInst("lw", regToAlloc, addr));
        }

        addrRegisterMap.put(addr, regToAlloc);
        registerAddrMap.put(regToAlloc, addr);

        return regToAlloc;
    }

    public void writeBackToMemoryForAll() {
        while (!timeQueue.isEmpty()) {
            var reg = timeQueue.poll();
            var addr = registerAddrMap.get(reg);
            target.addText(new TextInst("sw", reg, addr));
        }
    }

    public void reset() {
        registersUnused.clear();
        timeQueue.clear();
        addrRegisterMap.clear();
        registerAddrMap.clear();
        registersUnused.addAll(registers);
    }

    public boolean hasBeenAllocated(Offset addr) {
        return addrRegisterMap.containsKey(addr);
    }

    public Register getRegister(Offset addr) {
        return addrRegisterMap.get(addr);
    }

    public static void main(String[] args) {
        var target = new Target();
        var pool = new TempRegisterPool(target, List.of(new Register("t0"), new Register("t1"), new Register("t2")));

        var sp = new Register("sp");
        var offset1 = new Offset(sp, 0); // %1
        var offset2 = new Offset(sp, 4); // %2
        var offset3 = new Offset(sp, 8); // %3
        var offset4 = new Offset(sp, 12); // %4
        var offset5 = new Offset(sp, 16); // %4

        // %1 = load
        var r1 = pool.allocTempRegister(offset1, true);
        target.addText(new TextInst("load", r1));

        // %2 = %1 + 2
        var r2 = pool.allocTempRegister(offset2, true);
        target.addText(new TextInst("add", r2, r1, new Immediate(2)));

        // %3 = %1 + %2
        var r3 = pool.allocTempRegister(offset3, true);
        target.addText(new TextInst("add", r3, r1, r2));

        // %4 = %3 + %3
        var r4 = pool.allocTempRegister(offset4, true);
        target.addText(new TextInst("add", r4, r3, r3));

        // %5 = %1 + %4
        var r5 = pool.allocTempRegister(offset5, true);
        r1 = pool.allocTempRegister(offset1, false);
        r4 = pool.allocTempRegister(offset4, false);
        target.addText(new TextInst("add", r5, r1, r4));

        // call
        pool.writeBackToMemoryForAll();
        target.addText(new TextInst("jal"));

        target.dump(System.out, false);
    }
}
