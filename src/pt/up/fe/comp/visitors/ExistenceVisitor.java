package pt.up.fe.comp.visitors;

import pt.up.fe.comp.EntityTypes;
import pt.up.fe.comp.MySymbol;
import pt.up.fe.comp.MySymbolTable;
import pt.up.fe.comp.Types;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import static pt.up.fe.comp.visitors.Utils.existsInScope;

public class ExistenceVisitor extends AJmmVisitor<Object, Integer> {
    private final MySymbolTable symbolTable;
    private final Stack<MySymbol> scopeStack;

    public ExistenceVisitor(MySymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        this.scopeStack = new Stack<>();

        MySymbol globalScope = new MySymbol(new Type(Types.NONE.toString(), false), "global", EntityTypes.GLOBAL);
        this.createScope(globalScope);

        addVisit("ImportDecl", this::importDeclVisit);
        addVisit("ClassDecl", this::classDeclVisit);
        addVisit("MainDecl", this::mainDeclVisit);
        addVisit("VarDecl", this::varDeclVisit);
        addVisit("PublicMethod", this::publicMethodVisit);
        addVisit("Argument", this::argumentVisit);
        addVisit("AssignmentExpr", this::assignExprVisit);
        addVisit("WhileSt", this::whileStVisit);
        addVisit("IntegerLiteral", this::integerLiteralVisit);
        addVisit("BooleanLiteral", this::booleanLiteralVisit);

        addVisit("DotExpression", this::dotExpressionVisit);
        addVisit("_Identifier", this::identifierVisit);
        addVisit("DotMethod", this::dotMethodVisit);
        addVisit("IntArray", this::intArrayVisit);

        setDefaultVisit(this::defaultVisit);
    }

    private void createScope(MySymbol symbol) {
        this.scopeStack.push(symbol);
        this.symbolTable.openScope(symbol);
    }

    private Integer importDeclVisit(JmmNode node, Object dummy) {
        if (node.getNumChildren() >= 1) {
            return 0;
        }

        throw new RuntimeException("Illegal number of children in node " + "." + node.getKind());
    }

    private Integer classDeclVisit(JmmNode node, Object dummy) {
        MySymbol classSymbol = new MySymbol(new Type(Types.NONE.toString(), false), node.get("name"), EntityTypes.CLASS);

        // Add new scope
        this.createScope(classSymbol);

        Integer visitResult = 0;
        for (int i = 0; i < node.getNumChildren(); ++i) {
            JmmNode childNode = node.getJmmChild(i);
            visitResult = visit(childNode);
            // System.out.println("Visited class Decl child: " + i + " with result " + visitResult);
        }

        this.scopeStack.pop();

        return visitResult;
    }

    private Integer mainDeclVisit(JmmNode node, Object dummy) {
        MySymbol mainSymbol = new MySymbol(new Type(Types.VOID.toString(), false), "main", EntityTypes.METHOD);

        // Add new scope
        this.createScope(mainSymbol);

        Integer visitResult = 0;
        for (int i = 0; i < node.getNumChildren(); ++i) {
            JmmNode childNode = node.getJmmChild(i);
            visitResult = visit(childNode);
            // System.out.println("Visited main Decl child: " + i + " with result " + visitResult);
        }

        this.scopeStack.pop();

        return visitResult;
    }

    private Integer varDeclVisit(JmmNode node, Object dummy) {
        return 0;
    }

    private Integer publicMethodVisit(JmmNode node, Object dummy) {
        if (node.getNumChildren() <= 0)
            throw new RuntimeException("Illegal number of children in node " + node.getKind() + ".");

        // Get the return type of the method
        Types varType = Types.getType(node.getJmmChild(0).getKind());
        boolean isArray = varType.getIsArray();

        MySymbol methodSymbol = new MySymbol(new Type(varType.toString(), isArray), node.get("name"), EntityTypes.METHOD);

        // Insert next scope pointer in previous scope
        this.symbolTable.put(this.scopeStack.peek(), methodSymbol);

        // Add new scope
        this.createScope(methodSymbol);

        Integer visitResult = 0;
        for (int i = 1; i < node.getNumChildren(); ++i) {
            JmmNode childNode = node.getJmmChild(i);
            visitResult = visit(childNode);
            // System.out.println("Visited method Decl child: " + i + " with result " + visitResult);
        }

        // Pop current scope
        this.scopeStack.pop();

        return visitResult;
    }

    private Integer whileStVisit(JmmNode node, Object dummy) {
        // Children: Expr and While Block
        if (node.getNumChildren() < 2) {
            throw new RuntimeException("Illegal number of children in node " + node.getKind() + ".");
        }

        //TODO Should we be checking anything here? I think we can't declare variables
        // inside while loops; We can iterate the children and visit them in order to
        // accomplish this
        return 0;
    }

    private Integer argumentVisit(JmmNode node, Object dummy) {
        if (node.getNumChildren() == 1) {
            Types varType = Types.getType(node.getJmmChild(0).getKind());
            boolean isArray = varType.getIsArray();

            String argName = node.get("arg");
            MySymbol argSymbol = new MySymbol(new Type(varType.toString(), isArray), argName, EntityTypes.ARG);
            this.symbolTable.put(this.scopeStack.peek(), argSymbol);
            return 0;
        }

        throw new RuntimeException("Illegal number of children in node " + node.getKind() + ".");
    }

    private Integer assignExprVisit(JmmNode node, Object dummy) {
        // TODO: Confirm we don't need to store anything
        if (node.getNumChildren() == 2) {
            // System.out.println("Assign Expr with " + node.getNumChildren() + " children");
            return 0;
        }

        throw new RuntimeException("Illegal number of children in node " + "." + node.getKind());
    }

    private Integer dotExpressionVisit(JmmNode node, Object dummy) {
        if (node.getNumChildren() == 2) {
            JmmNode firstChild = node.getJmmChild(0);
            JmmNode secondChild = node.getJmmChild(1);

            // Check length property
            if (secondChild.getKind().equals("DotLength")) {
                if (!this.validateLength(firstChild)) {
                    throw new RuntimeException("Built-in \"length\" is only valid over arrays.");
                }
                // verify if first child is an array
                return 0;
            }

            // Check imported method
            if (firstChild.getKind().equals("_Identifier")) {
                String firstName = firstChild.get("id");
                if (!Utils.hasImport(firstName, this.symbolTable)) {
                    // Check if Object
                    String calledMethod = secondChild.get("method");
                    if (!this.checkObjectMethod(firstName, calledMethod)) {
                        throw new RuntimeException("\"" + calledMethod + "\" is not a class method");
                    }
                }
                MySymbol firstSymbol = existsInScope(firstName, this.scopeStack, this.symbolTable);
                if (firstSymbol == null) throw new RuntimeException("Invalid reference to " + firstName + ". Identifier does not exist!");
                return 0;
            }

            // Check class methods
            if (firstChild.getKind().equals("_This") && secondChild.getKind().equals("DotMethod")) {
                if (this.hasInheritance()) return 0; // Assume the method exists
                this.hasThisDotMethod(secondChild);
            }

            return 0;
        }

        throw new RuntimeException("Illegal number of children in node " + "." + node.getKind());
    }

    private Integer identifierVisit(JmmNode node, Object dummy) {
        if (node.getNumChildren() == 0) {
            String firstName = node.get("id");
            MySymbol firstSymbol = existsInScope(firstName, this.scopeStack, this.symbolTable);
            if (firstSymbol == null) throw new RuntimeException("Invalid reference to " + firstName + ". Identifier does not exist!");
            return 0;
        }

        throw new RuntimeException("Illegal number of children in node " + node.getKind() + ".");
    }

    private Integer dotMethodVisit(JmmNode node, Object dummy) {
        for (int i = 0; i < node.getNumChildren(); ++i) {
            visit(node.getJmmChild(i));
        }

        return 0;
    }

    private Integer intArrayVisit(JmmNode node, Object dummy) {
        if (node.getNumChildren() == 0) {
            // System.out.println("Analysing the " + node.getKind());
            return 2;   // Return 2 if IntArray; Return 1 if Int, and so on... ?
        }

        throw new RuntimeException("Illegal number of children in node " + node.getKind() + ".");
    }

    private Integer integerLiteralVisit(JmmNode node, Object dummy) {
        if (node.getNumChildren() == 0) {
            return Integer.parseInt(node.get("value"));
        }

        throw new RuntimeException("Illegal number of children in node " + node.getKind() + ".");
    }

    private Integer booleanLiteralVisit(JmmNode node, Object dummy) {
        if (node.getNumChildren() == 0) {
            return 1;
        }

        throw new RuntimeException("Illegal number of children in node " + node.getKind() + ".");
    }

    public boolean validateLength(JmmNode left) {
        String type = Utils.getNodeType(left, this.scopeStack, this.symbolTable);  // send symbol table
        // TODO: CHANGE THIS LOGIC
        return type.equals(new Type(Types.INT_ARRAY.toString(), Types.INT_ARRAY.getIsArray()).toString());
    }

    private boolean hasInheritance() {
        return this.symbolTable.getSuper() != null;
    }

    /**
     * Verifies if dot method exists
     * @param node
     */
    public void hasThisDotMethod(JmmNode node){
        String identifier = node.get("method");
        if (!this.symbolTable.getMethods().contains(identifier)) {
            throw new RuntimeException("Method \"" + identifier + "\" is undefined");
            // analysis.addReport(node,"Function \"" + identifier + "\" is undefined");
        }
    }

    public Boolean checkObjectMethod(String nodeName, String calledMethod) {
        // TODO: CHECK IF THIS EXISTSINSCOPE IS CORRECT. DO WE NEED TO COMPARE MORE THAN JUST THE VARIABLE NAME??
        MySymbol foundSymbol = existsInScope(nodeName, this.scopeStack, this.symbolTable);
        String foundType = foundSymbol.getType().getName();
        // If the variable type is custom
        if (Utils.isCustomType(foundType)) {
            // Check if it is an object of the Class and if so check if the method exists
            if (foundType.equals(this.symbolTable.getClassName())) {
                if (hasInheritance()) return true;  // Assume it exists
                else if(!this.symbolTable.getMethods().contains(calledMethod)) {
                    // TODO: ADD ANALYSIS REPORT
                    return false;
                }
            }

            return true;
        }

        return false;
    }

    private Integer defaultVisit(JmmNode node, Object dummy) {
        if (node.getNumChildren() <= 0) {
            throw new RuntimeException("Illegal number of children in node " + node.getKind() + ".");
        }

        // System.out.println("Currently in node: " + node.getKind());
        Integer visitResult = 0;
        for (int i = 0; i < node.getNumChildren(); ++i) {
            JmmNode childNode = node.getJmmChild(i);
            visitResult = visit(childNode);
            //System.out.println("Intermediate result: " + visitResult);
        }

        return visitResult;
    }
}
