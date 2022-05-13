package pt.up.fe.comp.ollir;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;

import java.util.List;

public class MethodsBuilder extends AbstractBuilder {
    public MethodsBuilder(JmmSemanticsResult semanticsResult) {
        super(semanticsResult);
    }

    @Override
    public String compile() {
        SymbolTable symbolTable = semanticsResult.getSymbolTable();

        for (String methodName : symbolTable.getMethods()) {
            builder.append(OllirConstants.TAB);
            builder.append(".method public ").append(methodName).append("(");
            
            List<Symbol> paremeters = symbolTable.getParameters(methodName);
            for (Symbol param : paremeters) {
                builder.append(param.getName());
                String type = OllirUtils.convertType(param.getType());
                builder.append(".").append(type).append(")");
            }

            Type returnType = symbolTable.getReturnType(methodName);
            builder.append(".").append(OllirUtils.convertType(returnType))
                .append(" {\n");

            builder.append("\n").append(OllirConstants.TAB).append("}");
        }

        return builder.toString();
    }
}
