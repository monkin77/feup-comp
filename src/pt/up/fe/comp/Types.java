package pt.up.fe.comp;

import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.specs.util.SpecsEnums;

public enum Types {
    INT("int", false),
    BOOLEAN("boolean", false),
    STRING("string", false),
    INT_ARRAY("int", true),
    VOID("void", false),
    CUSTOM("custom", false),
    NONE("none", false),
    INVALID("invalid", false);

    private final String code;
    private final boolean isArray;

    Types(String code, boolean isArray) {
        this.code = code;
        this.isArray = isArray;
    }

    @Override
    public String toString() {
        return code;
    }

    public boolean getIsArray() {
        return this.isArray;
    }

    public static Types fromName(String name) {
        return SpecsEnums.fromName(Types.class, name);
    }

    public static Types getType(String astType) {
        switch (astType) {
            case "IntArray":
                return Types.INT_ARRAY;
            case "_Int":
                return Types.INT;
            case "_Bool":
                return Types.BOOLEAN;
            default:
                return Types.CUSTOM;
        }
    }
}
