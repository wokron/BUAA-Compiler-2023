package sysy.backend.target.inst;

import sysy.backend.target.value.TargetValue;

import java.util.ArrayList;
import java.util.List;

public class TextInst extends TextEntry {
    private final String instName;
    private final List<TargetValue> values = new ArrayList<>();

    public TextInst(String instName, TargetValue... values) {
        this.instName = instName;
        this.values.addAll(List.of(values));
    }

    @Override
    public String toString() {
        String sb = String.format("%-6s ", instName) +
                String.join(", ", values.stream().map(TargetValue::toString).toList());
        return sb;
    }
}
