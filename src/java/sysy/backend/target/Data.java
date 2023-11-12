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

    public Label getLabel() {
        return label;
    }

    @Override
    public String toString() {
        String sb = label + ": ." + type + " " +
                String.join(", ", values.stream().map(Object::toString).toList());
        return sb;
    }
}
