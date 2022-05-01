package pt.up.fe.comp.visitors;

import pt.up.fe.comp.AstTypes;
import pt.up.fe.comp.MySymbol;
import pt.up.fe.comp.MySymbolTable;
import pt.up.fe.comp.Types;
import pt.up.fe.comp.jmm.analysis.table.Type;
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

    public static Type getNodeType(JmmNode node, Stack<MySymbol> scopeStack, MySymbolTable symbolTable){
        String kind = node.getKind();

        if(isMathExpression(kind)) return new Type(Types.INT.toString(), Types.INT.getIsArray());
        if(isBooleanExpression(kind)) return new Type(Types.BOOLEAN.toString(), Types.BOOLEAN.getIsArray());

        switch (kind){
            case "DotExpression":
                return getReturnValueMethod(node, scopeStack, symbolTable);
            case "ArrayExpr":
                return new Type(Types.INT.toString(), Types.INT.getIsArray());
            case "NewObjExpr":
                return new Type(node.getChildren().get(0).get("object"), Types.CUSTOM.getIsArray()); // return the class name
            case "NewArrayExpr":
                return new Type(Types.INT_ARRAY.toString(), Types.INT_ARRAY.getIsArray());
            case "_This":
                return new Type(symbolTable.getClassName(), false);
            default:
                // Identifier
                MySymbol identifier = existsInScope(node.get("id"), scopeStack, symbolTable);
                return identifier.getType();
        }
    }

    private static String getVariableType(JmmNode node, String parentMethodName) {
        return "";
    }

    private static String getParentMethodName(JmmNode node) {
        return "";
    }

    private static Type getReturnValueMethod(JmmNode node, Stack<MySymbol> scopeStack, MySymbolTable symbolTable) {
        JmmNode leftNode = node.getJmmChild(0);
        JmmNode rightNode = node.getJmmChild(1);

        // Recursive step that finds the type of the left node
        Type leftNodeType = Utils.getNodeType(leftNode, scopeStack, symbolTable);

        String className = symbolTable.getClassName();

        if (rightNode.getKind().equals("DotLength")) return new Type(Types.INT.toString(), Types.INT.getIsArray());

        String methodName = rightNode.get("method");
        boolean containsMethodName = symbolTable.getMethods().contains(methodName);

        System.out.println("------------- " + node.toString() + " leftNodeType: " + leftNodeType.toString());
        if (containsMethodName && (leftNodeType.getName().equals(className) || node.getKind().equals(AstTypes.THIS.toString()))) {
            System.out.println("XXXXXXXXX " + symbolTable.getReturnType(methodName));
            return symbolTable.getReturnType(methodName);
        }

        return new Type(Types.UNKNOWN.toString(), Types.UNKNOWN.getIsArray());
    }

    private static boolean isBooleanExpression(String kind) {
        return kind.equals("LessExpr") || kind.equals("AndExpr") || kind.equals("NotExpr");
    }

    private static boolean isMathExpression(String kind) {
        return kind.equals("MultExpr") || kind.equals("AddExpr") || kind.equals("SubExpr") || kind.equals("DivExpr");
    }

    public static boolean hasImport(String checkImport, MySymbolTable symbolTable){
        for(String importName : symbolTable.getImports()) {
            String[] splitImport = importName.split("\\.");
            if (splitImport[splitImport.length - 1].equals(checkImport)) return true;
        }
        return false;
    }

    public static Boolean isCustomType(String typeName) {
        return !typeName.equals(Types.INT.toString()) && !typeName.equals(Types.STRING_ARRAY.toString()) && !typeName.equals(Types.BOOLEAN.toString());
    }

    /**
     * Gets the type of a node. If custom, will return with the custom class name
     * @param node
     * @return
     */
    public static Type getNodeType(JmmNode node) {
        Types varType = Types.getType(node.getKind());
        boolean isArray = varType.getIsArray();

        String typeName = varType.toString();

        // Check if it is a custom type and update the type name
        if (typeName.equals(Types.CUSTOM.toString()))
            typeName = node.get("name");

        return new Type(typeName, isArray);
    }
}
