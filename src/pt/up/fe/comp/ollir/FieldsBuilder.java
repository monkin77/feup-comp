package pt.up.fe.comp.ollir;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;

public class FieldsBuilder extends AbstractBuilder {
    public FieldsBuilder(SymbolTable symbolTable) {
        super(symbolTable);
    }

    @Override
    public String compile() {
        for (Symbol field : symbolTable.getFields()) {
            builder.append(OllirConstants.TAB);
            builder.append(".field private ").append(field.getName());
            builder.append(".").append(OllirUtils.convertType(field.getType()));
            builder.append(";\n");
        }

        return builder.toString();
    }
}
