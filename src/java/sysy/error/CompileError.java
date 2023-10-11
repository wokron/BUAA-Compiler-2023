package sysy.error;

public class CompileError {
    private final CompileErrorType type;
    private final int lineNum;

    public CompileError(CompileErrorType type, int lineNum) {
        assert lineNum > 0;
        this.type = type;
        this.lineNum = lineNum;
    }

    public CompileErrorType getType() {
        return type;
    }

    public int getLineNum() {
        return lineNum;
    }

    @Override
    public String toString() {
        return lineNum + " " + type.toString();
    }
}
