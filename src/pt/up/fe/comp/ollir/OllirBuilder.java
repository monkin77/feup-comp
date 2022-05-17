package pt.up.fe.comp.ollir;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.analysis.table.Symbol;

public class OllirBuilder {
    protected final JmmSemanticsResult semanticsResult;
    protected final StringBuilder builder;

    public OllirBuilder(JmmSemanticsResult semanticsResult) {
        this.semanticsResult = semanticsResult;
        this.builder = new StringBuilder();
    }

    public String compile() {
        compileImports();
        builder.append(semanticsResult.getSymbolTable().getClassName());
        if (semanticsResult.getSymbolTable().getSuper() != null)
            builder.append(" extends ").append(semanticsResult.getSymbolTable().getSuper());

        builder.append(" {\n");
        compileFields();
        builder.append("\n");

        compileConstructor();
        builder.append("\n");
        VisitResult result = new OllirVisitor(semanticsResult.getSymbolTable()).visit(semanticsResult.getRootNode());
        builder.append(result.preparationCode);
        builder.append(result.code);

        builder.append("\n}");

        return builder.toString();
    }

    private void compileImports() {
        for (String importDecl : semanticsResult.getSymbolTable().getImports()) {
            builder.append("import ").append(importDecl).append(";\n");
        }
    }

    private void compileConstructor() {
        String className = semanticsResult.getSymbolTable().getClassName();
        builder.append(OllirConstants.TAB);
        builder.append(".construct ").append(className).append("().V {\n");
        builder.append(OllirConstants.TAB).append(OllirConstants.TAB);
        builder.append("invokespecial(this, \"<init>\").V;\n");
        builder.append(OllirConstants.TAB).append("}\n");
    }

    public void compileFields() {
        for (Symbol field : semanticsResult.getSymbolTable().getFields()) {
            builder.append(OllirConstants.TAB);
            builder.append(".field private ").append(field.getName());
            builder.append(".").append(OllirUtils.convertType(field.getType()));
            builder.append(";\n");
        }
    }
}
