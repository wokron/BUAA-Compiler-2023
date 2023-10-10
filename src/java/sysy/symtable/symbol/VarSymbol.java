package sysy.symtable.symbol;

import java.util.ArrayList;
import java.util.List;

public class VarSymbol extends Symbol {
    public boolean isConst = false;
    public List<Integer> values = new ArrayList<>();
    public List<Integer> dims = new ArrayList<>();

    public boolean isArray() {
        return !dims.isEmpty();
    }
}
