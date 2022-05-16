package pt.up.fe.comp.symbolTable;

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
        return switch (astType) {
            case "IntArray" -> Types.INT_ARRAY;
            case "_Int" -> Types.INT;
            case "_Bool" -> Types.BOOLEAN;
            default -> Types.CUSTOM;
        };
    }
}
