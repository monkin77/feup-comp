package pt.up.fe.comp.ollir;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.analysis.table.Symbol;

public class FieldsBuilder extends AbstractBuilder {
    public FieldsBuilder(JmmSemanticsResult semanticsResult) {
        super(semanticsResult);
    }

    @Override
    public String compile() {
        for (Symbol field : semanticsResult.getSymbolTable().getFields()) {
            builder.append(OllirConstants.TAB);
            builder.append(".field private ").append(field.getName());
            builder.append(".").append(OllirUtils.convertType(field.getType()));
            builder.append(";\n");
        }

        return builder.toString();
    }
}
