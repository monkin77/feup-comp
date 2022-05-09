package pt.up.fe.comp.visitors;

import pt.up.fe.comp.*;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

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
        addVisit("DotMethod", this::dotMethodVisit);
        addVisit("IntArray", this::intArrayVisit);

        addVisit("AddExpr", this::mathExprVisit);
        addVisit("SubExpr", this::mathExprVisit);
        addVisit("MultExpr", this::mathExprVisit);
        addVisit("DivExpr", this::mathExprVisit);
        addVisit("LessExpr", this::mathExprVisit);

        addVisit("AndExpr", this::boolExprVisit);
        addVisit("NotExpr", this::boolExprVisit);

        addVisit("ArrayExpr", this::arrayExprVisit);

        addVisit("IfElse", this::conditionalVisit);
        addVisit("WhileSt", this::conditionalVisit);

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



    private Integer assignExprVisit(JmmNode node, Object dummy) {
        // IF .length -> type is int
        // IF method -> use SymbolTable method to check method return type

        Integer visitResult;
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

            if (!this.isVariable(firstChild)) {
                this.reports.add(Report.newError(Stage.SEMANTIC, Integer.parseInt(firstChild.get("line")), Integer.parseInt(firstChild.get("col")),
                        "Type error. Attempting to assign a value to a '" + firstChild.getKind() + "' .",
                        null));
                return -1;
            } else if (!(this.isSameType(idType, assignType))) {
                String typeName = idType.getName();
                String assignTypeName = assignType.getName();

                this.reports.add(Report.newError(Stage.SEMANTIC, Integer.parseInt(firstChild.get("line")), Integer.parseInt(firstChild.get("col")),
                        "Type error. Attempting to assign value of type " + assignTypeName + " to a variable of type " + typeName + ".",
                        null));
                return -1;
            }

            return 0;
        }

        throw new RuntimeException("Illegal number of children in node " + "." + node.getKind());
    }

    private Integer mathExprVisit(JmmNode node, Object dummy) {
        Integer visitResult;
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

            // check if it's an accepted type
            String leftTypeName = Utils.printTypeName(leftType);
            String rightTypeName = Utils.printTypeName(rightType); // Using printTypeName to differ the case of int from int[]
            if (!(this.isArithmeticType(leftTypeName) && this.isArithmeticType(rightTypeName))) {
                this.reports.add(Report.newError(Stage.SEMANTIC, Integer.parseInt(firstChild.get("line")), Integer.parseInt(firstChild.get("col")),
                        "Type error. Attempting to do a " + node.getKind() + " with types: '" + Utils.printTypeName(leftType) + "' and '" + Utils.printTypeName(rightType) + "'.",
                        null));
                return -1;
            }

            return 0;
        }
        throw new RuntimeException("Illegal number of children in node " + "." + node.getKind());
    }

    private Integer boolExprVisit(JmmNode node, Object dummy) {
        Integer visitResult;
        for (int i = 0; i < node.getNumChildren(); ++i) {
            JmmNode childNode = node.getJmmChild(i);
            visitResult = visit(childNode);
            if (visitResult == -1) return -1;
        }

        if (node.getNumChildren() == 1) {
            // Not Expression
            JmmNode firstChild = node.getJmmChild(0);
            Type leftType = Utils.calculateNodeType(firstChild, this.scopeStack, this.symbolTable);
            // check if it's an accepted type
            String leftTypeName = leftType.getName();
            if (!this.isBoolType(leftTypeName)) {
                this.reports.add(Report.newError(Stage.SEMANTIC, Integer.parseInt(firstChild.get("line")), Integer.parseInt(firstChild.get("col")),
                        "Type error. Attempting to do a " + node.getKind() + " with type: '" + Utils.printTypeName(leftType) + "'.",
                        null));
                return -1;
            }

            return 0;
        } else if (node.getNumChildren() == 2) {
            JmmNode firstChild = node.getJmmChild(0);
            Type leftType = Utils.calculateNodeType(firstChild, this.scopeStack, this.symbolTable);

            JmmNode secondChild = node.getJmmChild(1);
            Type rightType = Utils.calculateNodeType(secondChild, this.scopeStack, this.symbolTable);

            // check if it's an accepted type
            String leftTypeName = leftType.getName();
            String rightTypeName = rightType.getName();
            if (!(this.isBoolType(leftTypeName) && this.isBoolType(rightTypeName))) {
                this.reports.add(Report.newError(Stage.SEMANTIC, Integer.parseInt(firstChild.get("line")), Integer.parseInt(firstChild.get("col")),
                        "Type error. Attempting to do a " + node.getKind() + " with types: '" + Utils.printTypeName(leftType) + "' and '" + Utils.printTypeName(rightType) + "'.",
                        null));
                return -1;
            }

            return 0;
        }
        throw new RuntimeException("Illegal number of children in node " + "." + node.getKind());
    }

    private Integer arrayExprVisit(JmmNode node, Object dummy) {
        Integer visitResult;
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

            // check if it's an accepted type
            String leftTypeName = Utils.printTypeName(leftType);
            String rightTypeName = Utils.printTypeName(rightType); // Using printTypeName to differ the case of int from int[]

            if (!leftType.isArray()) {
                this.reports.add(Report.newError(Stage.SEMANTIC, Integer.parseInt(firstChild.get("line")), Integer.parseInt(firstChild.get("col")),
                        "Type error. Attempting to access an array element in a variable of type '" + leftTypeName + "'.",
                        null));
                return -1;
            } else if (!this.isArithmeticType(rightTypeName)) {
                this.reports.add(Report.newError(Stage.SEMANTIC, Integer.parseInt(secondChild.get("line")), Integer.parseInt(secondChild.get("col")),
                        "Type error. Attempting to index an array element with a variable of type '" + rightTypeName + "'.",
                        null));
                return -1;
            }

            return 0;
        }
        throw new RuntimeException("Illegal number of children in node " + "." + node.getKind());
    }

    private Integer conditionalVisit(JmmNode node, Object dummy) {
        Integer visitResult;
        for (int i = 0; i < node.getNumChildren(); ++i) {
            JmmNode childNode = node.getJmmChild(i);
            visitResult = visit(childNode);
            if (visitResult == -1) return -1;
        }

        // can be a "while statement" or an "if statement"
        if (node.getNumChildren() == 2 || node.getNumChildren() == 3) {
            JmmNode conditionChild = node.getJmmChild(0);
            Type leftType = Utils.calculateNodeType(conditionChild, this.scopeStack, this.symbolTable);

            // check if it's an accepted type
            String leftTypeName = Utils.printTypeName(leftType);

           if (!this.isBoolType(leftTypeName)) {
                this.reports.add(Report.newError(Stage.SEMANTIC, Integer.parseInt(conditionChild.get("line")), Integer.parseInt(conditionChild.get("col")),
                        "Type error. Condition is not boolean. Type: '" + leftTypeName + "'.",
                        null));
                return -1;
            }

            return 0;
        }
        throw new RuntimeException("Illegal number of children in node " + "." + node.getKind());
    }

    private Integer dotMethodVisit(JmmNode node, Object dummy) {
        String methodName = node.get("method");
        List<MySymbol> parameters = this.symbolTable.getMethodArguments(methodName);

        boolean isImport = parameters == null;
        if (!isImport && node.getNumChildren() != parameters.size()) {
            this.reports.add(Report.newError(Stage.SEMANTIC, Integer.parseInt(node.get("line")), Integer.parseInt(node.get("col")),
                    "Type error. Expected " + parameters.size() + " arguments but found " + node.getNumChildren() + " in method '" + methodName + "'.",
                    null));
            return -1;
        }

        for (int i = 0; i < node.getNumChildren(); ++i) {
            JmmNode argNode = node.getJmmChild(i);
            Type argType = Utils.calculateNodeType(argNode, this.scopeStack, this.symbolTable);

            // if it has parameters check them
            if (!isImport && !argType.equals(parameters.get(i).getType())) {
                this.reports.add(Report.newError(Stage.SEMANTIC, Integer.parseInt(argNode.get("line")), Integer.parseInt(argNode.get("col")),
                        "Type error. Expected argument of type '" + Utils.printTypeName(parameters.get(i).getType()) + "' but found argument of type '" + Utils.printTypeName(argType) + "' in method '" + methodName + "'.",
                        null));
                return -1;
            }
            visit(node.getJmmChild(i));
        }

        return 0;
    }

    private Integer intArrayVisit(JmmNode node, Object dummy) {
        if (node.getNumChildren() == 0) {
            return 2;   // Return 2 if IntArray; Return 1 if Int, and so on... ?
        }

        throw new RuntimeException("Illegal number of children in node " + node.getKind() + ".");
    }

    private Integer defaultVisit(JmmNode node, Object dummy) {
        if (node.getNumChildren() < 0) {
            throw new RuntimeException("Illegal number of children in node " + node.getKind() + ".");
        }

        Integer visitResult = 0;
        for (int i = 0; i < node.getNumChildren(); ++i) {
            JmmNode childNode = node.getJmmChild(i);
            visitResult = visit(childNode);
        }

        return visitResult;
    }

    private boolean isArithmeticType(String type){
        return type.equals(Types.UNKNOWN.toString()) || type.equals(Types.INT.toString());
    }

    private boolean isBoolType(String type){
        return type.equals(Types.UNKNOWN.toString()) || type.equals(Types.BOOLEAN.toString());
    }

    private boolean isSameType(Type type1, Type type2){
        return type1.equals(type2) || type1.getName().equals(Types.UNKNOWN.toString()) || type2.getName().equals(Types.UNKNOWN.toString());
    }

    private boolean isVariable(JmmNode node) {
        return node.getKind().equals(AstTypes.IDENTIFIER.toString()) || node.getKind().equals(AstTypes.ARRAY_EXPR.toString());
    }

    public List<Report> getReports() {
        return reports;
    }

}
