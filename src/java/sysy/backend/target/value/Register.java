package sysy.backend.target.value;

public class Register extends TargetValue {
    private final String name;

    public Register(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "$" + name;
    }
}
