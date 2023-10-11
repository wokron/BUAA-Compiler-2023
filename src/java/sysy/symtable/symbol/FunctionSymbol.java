package sysy.symtable.symbol;

import java.util.ArrayList;
import java.util.List;

public class FunctionSymbol extends Symbol {
    public String retType;
    public final List<ParamType> paramTypeList = new ArrayList<>();
}
