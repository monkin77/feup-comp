package pt.up.fe.comp.symbolTable;

public enum AstTypes {
    INT("_Int"),
    IDENTIFIER("_Identifier"),
    ARRAY_EXPR("ArrayExpr"),
    THIS("_This");

    private final String code;

    AstTypes(String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return code;
    }
}
