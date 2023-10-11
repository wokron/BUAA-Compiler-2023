package sysy.error;

public enum CompileErrorType {
    ILLEGAL_SYMBOL("a"),
    NAME_REDEFINE("b"),
    UNDEFINED_NAME("c"),
    NUM_OF_PARAM_NOT_MATCH("d"),
    TYPE_OF_PARAM_NOT_MATCH("e"),
    RETURN_NOT_MATCH("f"),
    RETURN_IS_MISSING("g"),
    TRY_TO_CHANGE_VAL_OF_CONST("h"),
    SEMICN_IS_MISSING("i"),
    RPARENT_IS_MISSING("j"),
    RBRACK_IS_MISSING("k"),
    NUM_OF_PARAM_IN_PRINTF_NOT_MATCH("l"),
    BREAK_OR_CONTINUE_NOT_IN_LOOP("m");

    private final String value;

    CompileErrorType(String s) {
        value = s;
    }

    @Override
    public String toString() {
        return value;
    }
}
