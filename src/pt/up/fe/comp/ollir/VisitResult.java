package pt.up.fe.comp.ollir;

public class VisitResult {
    public final String preparationCode;
    public final String code;
    public final String finalCode;
    public final String returnType;

    public VisitResult(String preparationCode, String code, String finalCode) {
        this.preparationCode = preparationCode;
        this.code = code;
        this.finalCode = finalCode;
        returnType = null;
    }

    public VisitResult(String preparationCode, String code, String finalCode, String returnType) {
        this.preparationCode = preparationCode;
        this.code = code;
        this.finalCode = finalCode;
        this.returnType = returnType;
    }
}
