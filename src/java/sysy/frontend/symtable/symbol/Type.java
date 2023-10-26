package sysy.frontend.symtable.symbol;

import java.util.ArrayList;
import java.util.List;

public class Type {
    public String type;
    public final List<Integer> dims = new ArrayList<>();

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Type o) {
            return type.equals(o.type) && dims.equals(o.dims);
        } else {
            return false;
        }
    }
}
