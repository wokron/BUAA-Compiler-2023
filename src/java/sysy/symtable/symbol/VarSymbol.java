package sysy.symtable.symbol;

import java.util.ArrayList;
import java.util.List;

public class VarSymbol extends Symbol {
    public boolean isConst = false;
    public final List<Integer> values = new ArrayList<>();
    public final List<Integer> dims = new ArrayList<>();
    public int constLVal = -1;

    public boolean isArray() {
        return !dims.isEmpty();
    }
}
