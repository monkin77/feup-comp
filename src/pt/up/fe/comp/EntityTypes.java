package pt.up.fe.comp;

import pt.up.fe.specs.util.SpecsEnums;

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

    public static EntityTypes fromName(String name) {
        return SpecsEnums.fromName(EntityTypes.class, name);
    }

}
