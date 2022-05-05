package pt.up.fe.comp.visitors;

import pt.up.fe.comp.EntityTypes;
import pt.up.fe.comp.MySymbol;
import pt.up.fe.comp.MySymbolTable;
import pt.up.fe.comp.Types;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.*;

public class TypeCheckingVisitor extends AJmmVisitor<Object, Integer> {
    private final MySymbolTable symbolTable;
    private final Stack<MySymbol> scopeStack;
    private final List<Report> reports;

    public TypeCheckingVisitor(MySymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        this.scopeStack = new Stack<>();
        this.reports = new ArrayList<>();

        MySymbol globalScope = new MySymbol(new Type(Types.NONE.toString(), false), "global", EntityTypes.GLOBAL);
        this.createScope(globalScope);

        addVisit("ClassDecl", this::classDeclVisit);
        addVisit("MainDecl", this::mainDeclVisit);
        addVisit("PublicMethod", this::publicMethodVisit);
        addVisit("AssignmentExpr", this::assignExprVisit);
        //TODO visit of AndExpr... ArrayExpr
        addVisit("WhileSt", this::whileStVisit);
        //TODO Add if else visitor
        addVisit("DotMethod", this::dotMethodVisit);
        addVisit("IntArray", this::intArrayVisit);

        addVisit("AddExpr", this::mathExprVisit);
        addVisit("MultExpr", this::mathExprVisit);
        addVisit("SubExpr", this::mathExprVisit);

        setDefaultVisit(this::defaultVisit);
    }

    private void createScope(MySymbol symbol) {
        this.scopeStack.push(symbol);
        this.symbolTable.openScope(symbol);
    }

    private Integer classDeclVisit(JmmNode node, Object dummy) {
        MySymbol classSymbol = new MySymbol(new Type(Types.NONE.toString(), false), node.get("name"), EntityTypes.CLASS);

        // Add new scope
        this.createScope(classSymbol);

        Integer visitResult = 0;
        for (int i = 0; i < node.getNumChildren(); ++i) {
            JmmNode childNode = node.getJmmChild(i);
            visitResult = visit(childNode);
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
        }

        this.scopeStack.pop();

        return visitResult;
    }

    private Integer publicMethodVisit(JmmNode node, Object dummy) {
        if (node.getNumChildren() <= 0)
            throw new RuntimeException("Illegal number of children in node " + node.getKind() + ".");

        // Get the return type of the method
        Types varType = Types.getType(node.getJmmChild(0).getKind());
        boolean isArray = varType.getIsArray();

        MySymbol methodSymbol = new MySymbol(new Type(varType.toString(), isArray), node.get("name"), EntityTypes.METHOD);

        // Add new scope
        this.createScope(methodSymbol);

        Integer visitResult = 0;
        for (int i = 1; i < node.getNumChildren(); ++i) {
            JmmNode childNode = node.getJmmChild(i);
            visitResult = visit(childNode);
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

        //TODO get first child, check its type and visit it
        return 0;
    }


    private Integer assignExprVisit(JmmNode node, Object dummy) {
        // TODO: Check right side type by looking at its second child if it is a DotExpression.
        // IF .length -> type is int
        // IF method -> use SymbolTable method to check method return type

        Integer visitResult = 0;
        for (int i = 0; i < node.getNumChildren(); ++i) {
            JmmNode childNode = node.getJmmChild(i);
            visitResult = visit(childNode);
            if (visitResult == -1) return -1;
        }

        if (node.getNumChildren() == 2) {
            JmmNode firstChild = node.getJmmChild(0);
            Type idType = Utils.calculateNodeType(firstChild, this.scopeStack, this.symbolTable);

            JmmNode secondChild = node.getJmmChild(1);

            Type assignType = Utils.calculateNodeType(secondChild, this.scopeStack, this.symbolTable);

            if (!idType.equals(assignType)) {
                String typeName = idType.getName();
                String assignTypeName = assignType.getName();
                // TODO should we accept if both sides are imports?
                if (Utils.hasImport(typeName, this.symbolTable) && Utils.hasImport(assignTypeName, this.symbolTable)) {
                    return 0;
                }

                this.reports.add(Report.newError(Stage.SEMANTIC, Integer.valueOf(firstChild.get("line")), Integer.valueOf(firstChild.get("col")),
                        "Type error. Attempting to assign value of type " + assignTypeName + " to a variable of type " + typeName + ".",
                        null));
                return -1;
            }

            return 0;
        }

        throw new RuntimeException("Illegal number of children in node " + "." + node.getKind());
    }

    private Integer mathExprVisit(JmmNode node, Object dummy) {

        Integer visitResult = 0;
        for (int i = 0; i < node.getNumChildren(); ++i) {
            JmmNode childNode = node.getJmmChild(i);
            visitResult = visit(childNode);
            if (visitResult == -1) return -1;
        }

        if (node.getNumChildren() == 2) {
            JmmNode firstChild = node.getJmmChild(0);
            Type leftType = Utils.calculateNodeType(firstChild, this.scopeStack, this.symbolTable);

            JmmNode secondChild = node.getJmmChild(1);
            Type rightType = Utils.calculateNodeType(secondChild, this.scopeStack, this.symbolTable);
            Type intType = new Type(Types.INT.toString(), Types.INT.getIsArray());
            // check if it's INT
            if (!(leftType.equals(intType) && rightType.equals(intType))) {

                String typeName = leftType.getName();
                String rightTypeName = rightType.getName();
                // TODO should we accept if both sides are imports? Like we can add 2 classes by overloading the operator?
                if (Utils.hasImport(typeName, this.symbolTable) && Utils.hasImport(rightTypeName, this.symbolTable)) {
                    return 0;
                }

                // TODO what should we be outputing? When we have Int and Int_Array we show "type int and type int."
                this.reports.add(Report.newError(Stage.SEMANTIC, Integer.valueOf(firstChild.get("line")), Integer.valueOf(firstChild.get("col")),
                        "Type error. Attempting to add value of type " + leftType.toString() + " with value of type " + rightType.toString() + ".",
                        null));
                return -1;
            }

            return 0;
        }

        throw new RuntimeException("Illegal number of children in node " + "." + node.getKind());
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

    private Integer defaultVisit(JmmNode node, Object dummy) {
        if (node.getNumChildren() < 0) {
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

    public List<Report> getReports() {
        return reports;
    }

}
