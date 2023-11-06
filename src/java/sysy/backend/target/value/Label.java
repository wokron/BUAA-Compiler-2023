package sysy.backend.target.value;

public class Label extends TargetValue {
    private final String name;

    public Label(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
