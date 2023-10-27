package sysy.frontend.symtable.symbol;

import sysy.backend.ir.Value;
import sysy.frontend.symtable.SymbolTable;

public abstract class Symbol {
    public String ident;
    public SymbolTable table;
    public Value targetValue;
}
