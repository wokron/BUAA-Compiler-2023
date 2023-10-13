package sysy.visitor;

import sysy.symtable.symbol.Type;

import java.util.ArrayList;
import java.util.List;

public class VisitResult {
    public Type expType = new Type();
    public Integer constVal;
    public List<Integer> constInitVals = new ArrayList<>();
    public List<Type> paramTypes = new ArrayList<>();
}
