package sysy.symtable.symbol;

import java.util.ArrayList;
import java.util.List;

public class VarSymbol extends Symbol {
    public Type varType = new Type();
    public boolean isConst = false;
    public final List<Integer> values = new ArrayList<>();
    public int constLVal = -1;

    public boolean isArray() {
        return !varType.dims.isEmpty();
    }
}
