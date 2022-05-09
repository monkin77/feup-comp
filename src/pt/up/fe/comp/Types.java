package pt.up.fe.comp;

public enum Types {
    INT("int", false),
    BOOLEAN("boolean", false),
    STRING_ARRAY("string", true),
    INT_ARRAY("int", true),
    VOID("void", false),
    CUSTOM("custom", false),
    NONE("none", false),
    UNKNOWN("unknown", true);

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
