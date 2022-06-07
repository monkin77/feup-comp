package pt.up.fe.comp.symbolTable;

public enum AstTypes {
    INT("_Int"),
    IDENTIFIER("_Identifier"),
    ARRAY_EXPR("ArrayExpr"),
    THIS("_This"),
    DOT_EXPR("DotExpression"),
    DOT_METHOD("DotMethod");

    private final String code;

    AstTypes(String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return code;
    }
}
