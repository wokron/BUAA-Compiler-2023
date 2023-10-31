package sysy.backend.ir;


public class NameAllocator {
    private int count = 0;
    private static final NameAllocator instance = new NameAllocator();
    public static NameAllocator getInstance() {
        return instance;
    }

    public void reset() {
        count = 0;
    }

    public String alloc() {
        String name = Integer.toString(count);
        count++;
        return name;
    }
}
