package pt.up.fe.comp.visitors;

import pt.up.fe.comp.EntityTypes;
import pt.up.fe.comp.MySymbol;
import pt.up.fe.comp.MySymbolTable;
import pt.up.fe.comp.Types;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.Iterator;
import java.util.Stack;

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

    /**
     * Iterates all the existing scopes to look for the symbol given as argument
     * @param name
     * @return symbol if found, null otherwise
     */
    private MySymbol existsInScope(String name) {
        Iterator<MySymbol> scopeIter = this.scopeStack.iterator();

        MySymbol symbol;
        while(scopeIter.hasNext()) {
            symbol = this.symbolTable.get(scopeIter.next(), name);
            if (symbol != null) return symbol;
        }

        return null;
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
            String firstName = node.getJmmChild(0).get("id");
            MySymbol firstSymbol = this.existsInScope(firstName);
            if (firstSymbol == null) throw new RuntimeException("Invalid reference to " + firstName + ". Identifier does not exist!");

            JmmNode secondChild = node.getJmmChild(1);
            if (secondChild.getKind().equals("DotLength")) {
                // verify if first child is an array
                if (!firstSymbol.getType().isArray()) throw new RuntimeException("Invalid property length of " + firstName + ". Variable is not an array.");
            } else {
                visit(secondChild);
            }

            return 0;
        }

        throw new RuntimeException("Illegal number of children in node " + "." + node.getKind());
    }

    private Integer identifierVisit(JmmNode node, Object dummy) {
        if (node.getNumChildren() == 0) {
            String firstName = node.get("id");
            MySymbol firstSymbol = this.existsInScope(firstName);
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
