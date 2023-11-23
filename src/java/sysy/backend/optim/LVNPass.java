package sysy.backend.optim;

import sysy.backend.ir.*;
import sysy.backend.ir.Module;
import sysy.backend.ir.inst.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LVNPass {
    private final Module irModule;
    private final Map<Integer, Instruction> hashTable = new HashMap<>();

    public LVNPass(Module irModule) {
        this.irModule = irModule;
    }

    public Module pass() {
        for (var func : irModule.getFunctions()) {
            passFunc(func);
        }

        return irModule;
    }

    private void passFunc(Function func) {
        for (var block : func.getBasicBlocks()) {
            passBlock(block);
        }
    }

    private void passBlock(BasicBlock block) {
        hashTable.clear();
        var hashHelper = new HashHelper();

        var insts = block.getInstructions();
        for (int i = 0; i < insts.size(); i++) {
            var inst = insts.get(i);

            if (inst instanceof BrInst || inst instanceof ReturnInst) {
                continue;
            }
            if (inst instanceof StoreInst) {
                hashHelper.incStoreTime();
                continue;
            }

            int hash = hashHelper.hash(inst);
            if (hashTable.containsKey(hash)) {
                inst.replaceAllUseWith(hashTable.get(hash), false);
                i--;
            } else {
                hashTable.put(hash, inst);
            }
        }
    }
}

class HashHelper {
    private final Map<String, Integer> descToHash = new HashMap<>();
    private final Map<Value, Integer> valToHash = new HashMap<>();
    private int nextHashValue = 0;

    private int storeTime = 0;

    public void reset() {
        descToHash.clear();
        valToHash.clear();
        storeTime = 0;
        nextHashValue = 0;
    }

    public void incStoreTime() {
        storeTime++;
    }

    public String createValueDesc(Value value) {
        if (value instanceof GlobalValue gvalue) {
            return "G " + gvalue.getName();
        }

        if (value instanceof ImmediateValue ivalue) {
            return "Imm " + ivalue.getValue();
        }

        // else value instanceof inst
        if (value instanceof AllocaInst) {
            return "Alloca" + value.hashCode();
        }

        if (value instanceof CallInst) {
            return "Call" + value.hashCode();
        }

        if (value instanceof LoadInst loadInst) {
            return "Load "  + hash(loadInst.getPtr()) + " " + storeTime; // load inst will be different after store inst even if ptr is the same
        }

        var hashList = ((User)value).getOperands().stream().map(this::hash).map(Object::toString).toList();
        var operandsStr = String.join(", ", hashList);
        if (value instanceof BinaryInst binaryInst) {
            return binaryInst.getOp().name() + " " + operandsStr;
        }
        if (value instanceof GetElementPtrInst) {
            return "gep " + operandsStr;
        }
        if (value instanceof ICmpInst iCmpInst) {
            return iCmpInst.getCond().name() + " " + operandsStr;
        }
        if (value instanceof ZExtInst) {
            return "zext " + operandsStr;
        }

        return "";
    }

    public int hash(Value value) {
        if (valToHash.containsKey(value)) {
            return valToHash.get(value);
        }
        String desc = createValueDesc(value);
        if (descToHash.containsKey(desc)) {
            int hash = descToHash.get(desc);
            valToHash.put(value, hash);
            return hash;
        }

        int nextHash = nextHashValue++;
        valToHash.put(value, nextHash);
        descToHash.put(desc, nextHash);

        return nextHash;
    }

    public static void main(String[] args) {
        var helper = new HashHelper();

        var imm1 = new ImmediateValue(1);
        var hash = helper.hash(imm1);
        System.out.println(1 + ": " + hash);

        var imm2 = new ImmediateValue(1);
        hash = helper.hash(imm2);
        System.out.println(1 + ": " + hash);

        var imm3 = new ImmediateValue(3);
        hash = helper.hash(imm3);
        System.out.println(3 + ": " + hash);

        helper.reset();

        var allocA = new AllocaInst(IRType.getInt());
        var allocB = new AllocaInst(IRType.getInt());
        var loadA = new LoadInst(allocA);
        var loadB = new LoadInst(allocB);
        var loadB2 = new LoadInst(allocB);
        var c = new BinaryInst(BinaryInstOp.ADD, loadA, loadB);
        var d = new BinaryInst(BinaryInstOp.SUB, c, loadB2);
        var e = new BinaryInst(BinaryInstOp.ADD, loadA, loadB);
        var f = new BinaryInst(BinaryInstOp.SUB, e, loadB2);

        List<Instruction> insts = new ArrayList<>(List.of(allocA, allocB, loadA, loadB, loadB2, c, d, e, f));

        for (int i = 0; i < insts.size(); i++) {
            var inst = insts.get(i);
            System.out.printf("hash(insts[%d]) = %d\n", i, helper.hash(inst));
        }

        helper.reset();

        var getint1 = new CallInst(Function.BUILD_IN_GETINT, List.of());
        var getint2 = new CallInst(Function.BUILD_IN_GETINT, List.of());
        System.out.printf("getint = %d\n", helper.hash(getint1));
        System.out.printf("getint = %d\n", helper.hash(getint2));

        helper.reset();

        var x = new AllocaInst(IRType.getInt());
        var loadx1 = new LoadInst(x);
        System.out.printf("loadx1 = %d\n", helper.hash(loadx1));
        var loadx2 = new LoadInst(x);
        System.out.printf("loadx2 = %d\n", helper.hash(loadx2));
        var store1 = new StoreInst(loadx2, x); helper.incStoreTime();
        var loadx3 = new LoadInst(x);
        System.out.printf("loadx3 = %d\n", helper.hash(loadx3));
    }
}
