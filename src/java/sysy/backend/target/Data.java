package sysy.backend.target;

import sysy.backend.target.value.Label;

import java.util.ArrayList;
import java.util.List;

public class Data {
    private final Label label;
    private final String type;
    private final List<Object> values = new ArrayList<>();
    public Data(String labelName, String type, List<Object> values) {
        this.label = new Label(labelName);
        this.type = type;
        this.values.addAll(values);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(label).append(": .").append(type).append(" ");
        for (var val : values) {
            sb.append(val).append(", ");
        }
        return sb.toString();
    }
}
