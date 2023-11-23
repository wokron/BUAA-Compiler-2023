package sysy.backend.ir;

public class Use {
    private final User user;
    private Value value;
    private int pos;

    public Use(User user, Value value, int pos) {
        this.user = user;
        this.value = value;
        this.pos = pos;
    }

    public User getUser() {
        return user;
    }

    public Value getValue() {
        return value;
    }

    public int getPos() {
        return pos;
    }

    public void setValue(Value value) {
        this.value = value;
    }
}
