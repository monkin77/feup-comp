package pt.up.fe.comp.visitors;

import pt.up.fe.comp.MySymbol;
import pt.up.fe.comp.MySymbolTable;
import pt.up.fe.comp.Types;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.Iterator;
import java.util.Stack;

public class Utils {
    /**
     * Iterates all the existing scopes to look for the symbol given as argument
     * @param name
     * @return symbol if found, null otherwise
     */
    public static MySymbol existsInScope(String name, Stack<MySymbol> scopeStack, MySymbolTable symbolTable) {
        Iterator<MySymbol> scopeIter = scopeStack.iterator();

        MySymbol symbol;
        while(scopeIter.hasNext()) {
            MySymbol nextScope = scopeIter.next();
            symbol = symbolTable.get(nextScope, name);
            if (symbol != null) return symbol;
        }

        return null;
    }

    public static String getNodeType(JmmNode node, Stack<MySymbol> scopeStack, MySymbolTable symbolTable){
        String kind = node.getKind();

        if(isMathExpression(kind)) return "_Int";
        if(isBooleanExpression(kind)) return "_Bool";

        switch (kind){
            case "DotExpression":
                return getReturnValueMethod(node);
            case "ArrayExpr":
                return "_Int";
            case "NewObjExpr":
                return node.getChildren().get(0).get("object"); // return the class name
            case "NewArrayExpr":
                return "IntArray";
            default:
                // Identifier
                MySymbol identifier = existsInScope(node.get("id"), scopeStack, symbolTable);
                return identifier.getType().toString();
        }

    }

    private static String getVariableType(JmmNode node, String parentMethodName) {
        return "";
    }

    private static String getParentMethodName(JmmNode node) {
        return "";
    }

    private static String getReturnValueMethod(JmmNode node) {
        return "";
    }

    private static boolean isBooleanExpression(String kind) {
        return kind.equals("LessExpr") || kind.equals("AndExpr") || kind.equals("NotExpr");
    }

    private static boolean isMathExpression(String kind) {
        return kind.equals("MultExpr") || kind.equals("AddExpr") || kind.equals("SubExpr") || kind.equals("DivExpr");
    }
}
