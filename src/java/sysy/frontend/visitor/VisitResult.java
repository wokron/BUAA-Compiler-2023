package sysy.frontend.visitor;

import sysy.backend.ir.Value;
import sysy.frontend.symtable.symbol.Type;

import java.util.ArrayList;
import java.util.List;

public class VisitResult {
    public Type expType = new Type();
    public Integer constVal;
    public List<Integer> constInitVals = new ArrayList<>();
    public List<Type> paramTypes = new ArrayList<>();

    public Value irValue;
    public List<Value> irValues = new ArrayList<>();
}
