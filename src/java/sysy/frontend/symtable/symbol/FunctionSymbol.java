package sysy.frontend.symtable.symbol;

import java.util.ArrayList;
import java.util.List;

public class FunctionSymbol extends Symbol {
    public Type retType = new Type();
    public final List<Type> paramTypeList = new ArrayList<>();
}
