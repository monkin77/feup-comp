package pt.up.fe.comp.ollir;

public class VisitResult {
    public final String preparationCode;
    public final String code;
    public final String returnType;

    public VisitResult(String preparationCode, String code) {
        this.preparationCode = preparationCode;
        this.code = code;
        returnType = null;
    }

    public VisitResult(String preparationCode, String code, String returnType) {
        this.preparationCode = preparationCode;
        this.code = code;
        this.returnType = returnType;
    }
}
