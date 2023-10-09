package sysy.symtable.symbol;

import java.util.ArrayList;
import java.util.List;

public class ArraySymbol extends Symbol {
    public boolean isConst = false;
    public List<Integer> dims = new ArrayList<>();
    public List<Integer> values = new ArrayList<>();
}
