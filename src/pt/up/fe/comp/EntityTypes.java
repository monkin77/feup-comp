package pt.up.fe.comp;

public enum EntityTypes {
    GLOBAL("global"),
    CLASS("class"),
    METHOD("method"),
    IMPORT("import"),
    VARIABLE("variable"),
    ARG("argument"),
    EXTENDS("extends");

    private final String code;

    EntityTypes(String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return code;
    }
}
