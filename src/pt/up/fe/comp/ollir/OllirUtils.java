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
            case "int" -> stringBuilder.append("i32");
            case "boolean" -> stringBuilder.append("bool");
            case "string" -> stringBuilder.append("String");
            case "void" -> stringBuilder.append("V");
            default -> stringBuilder.append(javaType); // Custom class
        }

        return stringBuilder.toString();
    }

    public static boolean isNotTerminalNode(JmmNode node) {
        String kind = node.getKind();
        return !(kind.equals("IntegerLiteral") || kind.equals("ArrayExpr")
                 || kind.equals("_Identifier") || kind.equals("BooleanLiteral") || kind.equals("_This") || kind.equals("NewObjExpr") || kind.equals("NewArrayExpr") || kind.equals("BooleanCondition") || kind.equals("DotMethod"));
    }

    public static Symbol getSymbol(String symbol, String currentMethod, SymbolTable symbolTable
    ) {
        // TODO: Return type of field?
        // TODO: $?
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

        for (Symbol s : symbolTable.getFields()) {
            if (s.getName().equals(symbol)) {
                return s;
            }
        }

        return null;
    }

    public static boolean isClassField(String name, String currentMethod, SymbolTable symbolTable) {
        return symbolTable.getFields().stream().anyMatch(x -> x.getName().equals(name)) &&
               symbolTable.getLocalVariables(currentMethod).stream().noneMatch(x -> x.getName().equals(name)) &&
               symbolTable.getParameters(currentMethod).stream().noneMatch(x -> x.getName().equals(name));
    }
}
