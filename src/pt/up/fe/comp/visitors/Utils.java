package pt.up.fe.comp.visitors;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.symbolTable.*;

import java.util.*;

public class Utils {
    public static final ArrayList<EntityTypes> identifierTypes = new ArrayList<>(Arrays.asList(EntityTypes.VARIABLE, EntityTypes.ARG, EntityTypes.IMPORT));

    /**
     * Iterates all the existing scopes to look for the symbol given as argument
     * @param entityTypes List of entityTypes to be accepted
     * @return symbol if found, null otherwise
     */
    public static MySymbol existsInScope(String name, List<EntityTypes> entityTypes, Deque<MySymbol> scopeStack, MySymbolTable symbolTable) {
        Iterator<MySymbol> scopeIter = scopeStack.iterator();

        String fullName = name;
        if (entityTypes.contains(EntityTypes.IMPORT)) fullName = Utils.getFullImportName(fullName, symbolTable);


        MySymbol symbol;
        while(scopeIter.hasNext()) {
            MySymbol nextScope = scopeIter.next();
            symbol = symbolTable.get(nextScope, fullName, entityTypes);
            if (symbol != null) return symbol;
        }

        return null;
    }

    /**
     * Calculates the type of a Node, iterating the tree recursively.
     */
    public static Type calculateNodeType(JmmNode node, Deque<MySymbol> scopeStack, MySymbolTable symbolTable) {
        String kind = node.getKind();

        if(isMathExpression(kind)) return new Type(Types.INT.toString(), Types.INT.getIsArray());
        if(isBooleanExpression(kind)) return new Type(Types.BOOLEAN.toString(), Types.BOOLEAN.getIsArray());

        switch (kind){
            case "ReturnExpr": case "BooleanCondition":
                return calculateNodeType(node.getJmmChild(0), scopeStack, symbolTable);
            case "DotExpression":
                return getReturnValueMethod(node, scopeStack, symbolTable);
            case "ArrayExpr":
            case "IntegerLiteral":
            case "MultExpr":
            case "DivExpr":
            case "AddExpr":
            case "SubExpr":
                return new Type(Types.INT.toString(), Types.INT.getIsArray());
            case "BooleanLiteral":
            case "LessExpr":
            case "AndExpr":
            case "NotExpr":
                return new Type(Types.BOOLEAN.toString(), Types.BOOLEAN.getIsArray());
            case "NewObjExpr":
                return new Type(node.get("object"), Types.CUSTOM.getIsArray()); // return the class name
            case "NewArrayExpr":
                return new Type(Types.INT_ARRAY.toString(), Types.INT_ARRAY.getIsArray());
            case "_This":
                return new Type(symbolTable.getClassName(), false);

            default:
                // Identifier
                String nodeName = node.get("id");
                MySymbol identifier = existsInScope(nodeName, identifierTypes, scopeStack, symbolTable);

                if (identifier == null) {
                    throw new RuntimeException("Unknown reference to symbol " + nodeName + ".");
                }
                return identifier.getType();
        }
    }

    private static Type getReturnValueMethod(JmmNode node, Deque<MySymbol> scopeStack, MySymbolTable symbolTable) {
        JmmNode leftNode = node.getJmmChild(0);
        JmmNode rightNode = node.getJmmChild(1);

        // Recursive step that finds the type of the left node
        Type leftNodeType = Utils.calculateNodeType(leftNode, scopeStack, symbolTable);

        String className = symbolTable.getClassName();

        if (rightNode.getKind().equals("DotLength")) return new Type(Types.INT.toString(), Types.INT.getIsArray());

        String methodName = rightNode.get("method");
        int result = isValidMethodCall(methodName, leftNodeType.getName(), leftNode.getKind(), className, symbolTable);
        return switch (result) {
            case 0 -> symbolTable.getReturnType(methodName);
            case 1 -> new Type(Types.UNKNOWN.toString(), Types.UNKNOWN.getIsArray());
            default -> throw new RuntimeException("Invalid method call to method: " + methodName + " to element of type " + Utils.printTypeName(leftNodeType) + ".");
        };
    }

    private static boolean isBooleanExpression(String kind) {
        return kind.equals("LessExpr") || kind.equals("AndExpr") || kind.equals("NotExpr");
    }

    private static boolean isMathExpression(String kind) {
        return kind.equals("MultExpr") || kind.equals("AddExpr") || kind.equals("SubExpr") || kind.equals("DivExpr");
    }

    public static boolean hasImport(String checkImport, SymbolTable symbolTable){
        for(String importName : symbolTable.getImports()) {
            String[] splitImport = importName.split("\\.");
            if (splitImport[splitImport.length - 1].equals(checkImport)) return true;
        }
        return false;
    }

    /**
     * @return Full import name if found. Otherwise, return the provided name
     */
    public static String getFullImportName(String checkImport, MySymbolTable symbolTable){
        for(String importName : symbolTable.getImports()) {
            String[] splitImport = importName.split("\\.");
            if (splitImport[splitImport.length - 1].equals(checkImport)) return importName;
        }

        return checkImport;
    }

    public static Boolean isCustomType(String typeName) {
        return !typeName.equals(Types.INT.toString()) && !typeName.equals(Types.STRING_ARRAY.toString()) &&
                !typeName.equals(Types.BOOLEAN.toString()) && !typeName.equals(Types.INT_ARRAY.toString());
    }

    /**
     * Gets the type of a node. If custom, will return with the custom class name
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

    /**
     *
     * @return 0 -> Method is valid; 1 -> Assuming Method exists; 2 -> Method is invalid
     */
    public static int isValidMethodCall(String methodName, String leftNodeType, String nodeKind, String className, MySymbolTable symbolTable) {
        boolean containsMethodName = symbolTable.getMethods().contains(methodName);
        boolean leftNodeIsClass = (leftNodeType.equals(className) || nodeKind.equals(AstTypes.THIS.toString()));

        if (isCustomType(leftNodeType)) {
            // Check if it is an object of the Class and if so check if the method exists
            if (leftNodeIsClass) {
                if (containsMethodName) return 0;   // Method is valid
                if (symbolTable.hasInheritance()) return 1;  // Assume it exists
                else {
                    // TODO: ADD ANALYSIS REPORT
                    return 2;
                }
            }

            // Assume it exists since it's an import
            return 1;
        }

        return 2;
    }

    /**
     * Prints a type taking into account if it is an array or not
     */
    public static String printTypeName(Type type) {
        return type.getName() + (type.isArray() && !type.getName().equals(Types.UNKNOWN.toString()) ? "[]" : "");
    }
}
