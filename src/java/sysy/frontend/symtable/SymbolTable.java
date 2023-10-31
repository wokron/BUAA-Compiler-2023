package sysy.frontend.symtable;

import sysy.frontend.symtable.symbol.Symbol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SymbolTable {
    private SymbolTable preTable = null;
    private final List<SymbolTable> nextTables = new ArrayList<>();
    private final Map<String, Symbol> symbolMap = new HashMap<>();

    public boolean contains(String name) {
        return symbolMap.containsKey(name);
    }

    public void insertSymbol(Symbol symbol) {
        symbolMap.put(symbol.ident, symbol);
        symbol.table = this;
    }

    public Symbol getSymbol(String ident) {
        if (symbolMap.containsKey(ident)) {
            return symbolMap.get(ident);
        }

        if (preTable != null) {
            return preTable.getSymbol(ident);
        } else {
            return null;
        }
    }

    public SymbolTable createSubTable() {
        SymbolTable subTable = new SymbolTable();
        nextTables.add(subTable);
        subTable.preTable = this;
        return subTable;
    }

    public SymbolTable getPreTable() {
        return preTable;
    }

    public List<SymbolTable> getNextTables() {
        return nextTables;
    }
}
