package pt.up.fe.comp;

import pt.up.fe.specs.util.SpecsEnums;

public enum Types {
    GLOBAL("global"),
    CLASS("class"),
    METHOD("method"),
    IMPORT("import"),
    INT("int"),
    BOOLEAN("boolean"),
    STRING("string");

    private final String code;

    Types(String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return code;
    }

    static Types fromName(String name) {
        return SpecsEnums.fromName(Types.class, name);
    }
}
