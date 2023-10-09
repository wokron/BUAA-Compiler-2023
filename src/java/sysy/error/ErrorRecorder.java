package sysy.error;

import java.util.ArrayList;
import java.util.List;

public class ErrorRecorder {
    private final List<CompileError> errorList = new ArrayList<>();

    public void AddError(CompileError error) {
        errorList.add(error);
    }

    public List<CompileError> getErrors() {
        return errorList;
    }
}
