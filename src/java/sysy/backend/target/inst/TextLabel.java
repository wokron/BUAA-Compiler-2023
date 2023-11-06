package sysy.backend.target.inst;

public class TextLabel extends TextEntry {
    private final String labelName;
    public TextLabel(String labelName) {
        this.labelName = labelName;
    }

    @Override
    public String toString() {
        return labelName + ":";
    }
}
