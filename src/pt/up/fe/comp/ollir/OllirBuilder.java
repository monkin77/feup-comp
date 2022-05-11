package pt.up.fe.comp.ollir;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;

public class OllirBuilder extends AbstractBuilder {
    public OllirBuilder(SymbolTable symbolTable) {
        super(symbolTable);
    }

    @Override
    public String compile() {
        compileImports();
        builder.append(symbolTable.getClassName());
        if (symbolTable.getSuper() != null)
            builder.append("extends ").append(symbolTable.getSuper());

        builder.append("{\n");
        builder.append(new FieldsBuilder(symbolTable).compile());
        builder.append("\n");
        builder.append(new MethodsBuilder(symbolTable).compile());
        builder.append("\n}");

        return builder.toString();
    }

    private void compileImports() {
        for (String importDecl : symbolTable.getImports()) {
            builder.append("import ").append(importDecl).append(";\n");
        }
    }
}
