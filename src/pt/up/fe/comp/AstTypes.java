package pt.up.fe.comp;

import pt.up.fe.specs.util.SpecsEnums;

public enum AstTypes {
    INT("_Int"),
    BOOLEAN("_Bool"),
    IDENTIFIER("_Identifier"),
    INT_ARRAY("IntArray"),
    CUSTOM("CustomType"),
    DOT_EXPR("DotExpression"),
    ARRAY_EXPR("ArrayExpr"),
    THIS("_This"),
    MAIN_DECL("MainDecl"),
    UNDEFINED("Undefined");

    private final String code;

    AstTypes(String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return code;
    }


    public static Types fromName(String name) {
        return SpecsEnums.fromName(Types.class, name);
    }
}
