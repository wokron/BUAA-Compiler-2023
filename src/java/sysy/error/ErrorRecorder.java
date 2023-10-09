package sysy.error;

import java.util.ArrayList;
import java.util.List;

public class ErrorRecorder {
    private final List<CompileError> errorList = new ArrayList<>();

    public void addError(CompileError error) {
        errorList.add(error);
    }

    public void addError(CompileErrorType type, int lineNum) {
        errorList.add(new CompileError(type, lineNum));
    }

    public List<CompileError> getErrors() {
        return errorList;
    }
}
