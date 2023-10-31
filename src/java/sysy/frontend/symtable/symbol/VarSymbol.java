package sysy.frontend.symtable.symbol;

import java.util.ArrayList;
import java.util.List;

public class VarSymbol extends Symbol {
    public Type varType = new Type();
    public boolean isConst = false;
    public final List<Integer> values = new ArrayList<>();

    public boolean isArray() {
        return !varType.dims.isEmpty();
    }
}
