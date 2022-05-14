package pt.up.fe.comp.ollir;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;

public class OllirUtils {
    public static String convertType(Type symbolType) {
        StringBuilder stringBuilder = new StringBuilder();

        if (symbolType.isArray()) {
            stringBuilder.append("array.");
        }

        String javaType = symbolType.getName();
        switch (javaType) {
            case "int":
                stringBuilder.append("i32");
                break;
            case "boolean":
                stringBuilder.append("bool");
                break;
            case "string":
                stringBuilder.append("String");
                break;
            case "void":
                stringBuilder.append("V");
                break;
            default:
                stringBuilder.append(javaType); // Custom class
        }

        return stringBuilder.toString();
    }

    public static boolean isNotTerminalNode(JmmNode node) {
        String kind = node.getKind();
        return !(kind.equals("IntegerLiteral") || kind.equals("ArrayExpr")
                || kind.equals("_Identifier") || kind.equals("BooleanLiteral") || kind.equals("_This"));
    }

    public static Symbol getSymbol(String symbol, String currentMethod, SymbolTable symbolTable) {
        for (Symbol s : symbolTable.getLocalVariables(currentMethod)) {
            if (s.getName().equals(symbol)) {
                return s;
            }
        }
        for (Symbol s : symbolTable.getParameters(currentMethod)) {
            if (s.getName().equals(symbol)) {
                return s;
            }
        }

        // TODO missing class fields
        return null;
    }
}
