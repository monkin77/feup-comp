package pt.up.fe.comp.visitors;

import pt.up.fe.comp.*;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Stack;

import static pt.up.fe.comp.visitors.Utils.existsInScope;

public class ExistenceVisitor extends AJmmVisitor<Object, Integer> {
    private final MySymbolTable symbolTable;
    private final Stack<MySymbol> scopeStack;
    private final List<Report> reports;

    public ExistenceVisitor(MySymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        this.scopeStack = new Stack<>();
        this.reports = new ArrayList<>();

        MySymbol globalScope = new MySymbol(new Type(Types.NONE.toString(), false), "global", EntityTypes.GLOBAL);
        this.createScope(globalScope);

        addVisit("ClassDecl", this::classDeclVisit);
        addVisit("MainDecl", this::mainDeclVisit);
        addVisit("PublicMethod", this::publicMethodVisit);
        addVisit("DotExpression", this::dotExpressionVisit);
        addVisit("_Identifier", this::identifierVisit);
        addVisit("VarDecl", this::varDeclVisit);
        addVisit("NewObjExpr", this::newObjExprVisit);

        setDefaultVisit(this::defaultVisit);
    }

    private void createScope(MySymbol symbol) {
        this.scopeStack.push(symbol);
        this.symbolTable.openScope(symbol);
    }

    private Integer classDeclVisit(JmmNode node, Object dummy) {
        MySymbol classSymbol = new MySymbol(new Type(Types.NONE.toString(), false), node.get("name"), EntityTypes.CLASS);

        // Check if extended class was imported
        Optional<String> extendedClass = node.getOptional("extends");
        if (!extendedClass.isEmpty()) {
            if (!Utils.hasImport(extendedClass.get(), this.symbolTable)) {
                this.reports.add(Report.newError(Stage.SEMANTIC, Integer.valueOf(node.get("line")), Integer.valueOf(node.get("col")),
                        "Extended class was not imported.",
                        null));
                return -1;
            }
        }

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

    private Integer publicMethodVisit(JmmNode node, Object dummy) {
        if (node.getNumChildren() <= 0)
            throw new RuntimeException("Illegal number of children in node " + node.getKind() + ".");

        Type returnNodeType = Utils.getNodeType(node.getJmmChild(0));

        MySymbol methodSymbol = new MySymbol(returnNodeType, node.get("name"), EntityTypes.METHOD);

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

    private Integer dotExpressionVisit(JmmNode node, Object dummy) {
        if (node.getNumChildren() == 2) {
            JmmNode firstChild = node.getJmmChild(0);
            JmmNode secondChild = node.getJmmChild(1);

            // Check length property
            if (secondChild.getKind().equals("DotLength")) {
                if (!this.validateLength(firstChild)) {
                    this.reports.add(Report.newError(Stage.SEMANTIC, Integer.valueOf(firstChild.get("line")), Integer.valueOf(firstChild.get("col")),
                            "Built-in \"length\" is only valid over arrays.",
                            null));
                    return -1;
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
                    if (!this.checkObjectMethod(firstName, calledMethod, firstChild)) {
                        return -1;
                    }
                }

                MySymbol firstSymbol = existsInScope(firstName, Utils.identifierTypes, this.scopeStack, this.symbolTable);
                if (firstSymbol == null) {
                    this.reports.add(Report.newError(Stage.SEMANTIC, Integer.valueOf(firstChild.get("line")), Integer.valueOf(firstChild.get("col")),
                            "Invalid reference to " + firstName + ". Identifier does not exist!",
                            null));
                    return -1;
                }
                return 0;
            }

            // Check class methods
            if (firstChild.getKind().equals("_This") && secondChild.getKind().equals("DotMethod")) {
                if (this.symbolTable.hasInheritance()) return 0; // Assume the method exists
                if (!this.hasThisDotMethod(secondChild)) return -1;
            }

            if (!this.validateDotExpression(firstChild, secondChild)) return -1;

            for (int i = 0; i < node.getNumChildren(); ++i) {
                visit(node.getJmmChild(i));
            }

            return 0;
        }

        throw new RuntimeException("Illegal number of children in node " + "." + node.getKind());
    }

    private Integer identifierVisit(JmmNode node, Object dummy) {
        if (node.getNumChildren() == 0) {
            String firstName = node.get("id");
            MySymbol firstSymbol = existsInScope(firstName, Utils.identifierTypes, this.scopeStack, this.symbolTable);
            if (firstSymbol == null) {
                this.reports.add(Report.newError(Stage.SEMANTIC, Integer.valueOf(node.get("line")), Integer.valueOf(node.get("col")),
                        "Invalid reference to " + firstName + ". Identifier does not exist!",
                        null));
                return -1;
            }
            return 0;
        }

        throw new RuntimeException("Illegal number of children in node " + node.getKind() + ".");
    }

    private Integer varDeclVisit(JmmNode node, Object dummy) {
        if (node.getNumChildren() == 1) {
            Type returnNodeType = Utils.getNodeType(node.getJmmChild(0));
            String typeStr = returnNodeType.getName();

            // If not a custom type
            if (!Utils.isCustomType(typeStr)) return 0;

            // If type is an import or the class
            if (Utils.hasImport(typeStr, this.symbolTable) || this.symbolTable.getClassName().equals(typeStr)) return 0;

            // if type is the extended class
            if (this.symbolTable.hasInheritance() && this.symbolTable.getSuper().equals(typeStr)) return 0;

            this.reports.add(Report.newError(Stage.SEMANTIC, Integer.valueOf(node.get("line")), Integer.valueOf(node.get("col")),
                    "Invalid attempt to create a variable of non-existing type " + typeStr + ".",
                    null));
            return -1;
        }

        throw new RuntimeException("Illegal number of children in node " + node.getKind() + ".");
    }

    private Integer newObjExprVisit(JmmNode node, Object dummy) {
        if (node.getNumChildren() == 0) {
            String objectName = node.get("object");

            // If not a custom type
            if (!Utils.isCustomType(objectName)) {
                this.reports.add(Report.newError(Stage.SEMANTIC, Integer.valueOf(node.get("line")), Integer.valueOf(node.get("col")),
                        "Invalid attempt to create a dynamic variable of primitive type " + objectName + ".",
                        null));
                return -1;
            }

            // If type is an import or the class
            if (Utils.hasImport(objectName, this.symbolTable) || this.symbolTable.getClassName().equals(objectName)) return 0;

            // if type is the extended class
            if (this.symbolTable.hasInheritance() && this.symbolTable.getSuper().equals(objectName)) return 0;


            this.reports.add(Report.newError(Stage.SEMANTIC, Integer.valueOf(node.get("line")), Integer.valueOf(node.get("col")),
                    "Invalid attempt to create a dynamic variable of type " + objectName + ".",
                    null));
            return -1;
        }

        throw new RuntimeException("Illegal number of children in node " + node.getKind() + ".");
    }

    public boolean validateLength(JmmNode left) {
        Type type = Utils.calculateNodeType(left, this.scopeStack, this.symbolTable);  // send symbol table
        return type.isArray();
    }

    /**
     * Validates a nested Dot Expression by checking the type recursively and verifying the method exists in the class
     * @param dotExpr
     * @param dotMethod
     * @return true if valid, false otherwise
     */
    public boolean validateDotExpression(JmmNode dotExpr, JmmNode dotMethod) {
        Type type = Utils.calculateNodeType(dotExpr, this.scopeStack, this.symbolTable);

        String methodName = dotMethod.get("method");
        int result = Utils.isValidMethodCall(methodName, type.getName(), dotExpr.getKind(), this.symbolTable.getClassName(), symbolTable);

        if (result >= 2) {
            this.reports.add(Report.newError(Stage.SEMANTIC, Integer.valueOf(dotExpr.get("line")), Integer.valueOf(dotExpr.get("col")),
                    "Invalid method call " + methodName + " to element of type " + type.getName() + (type.isArray() ? "[]" : "") + ".",
                    null));
            return false;
        }
        return true;
    }

    /**
     * Verifies if dot method exists
     * @param node
     */
    public boolean hasThisDotMethod(JmmNode node){
        String identifier = node.get("method");
        if (!this.symbolTable.getMethods().contains(identifier)) {
            this.reports.add(Report.newError(Stage.SEMANTIC, Integer.valueOf(node.get("line")), Integer.valueOf(node.get("col")),
                    "Method \"" + identifier + "\" is undefined",
                    null));
            return false;
        }

        return true;
    }

    public boolean checkObjectMethod(String nodeName, String calledMethod, JmmNode node) {
        // TODO: CHECK IF THIS EXISTSINSCOPE IS CORRECT. DO WE NEED TO COMPARE MORE THAN JUST THE VARIABLE NAME??
        MySymbol foundSymbol = existsInScope(nodeName, Utils.identifierTypes, this.scopeStack, this.symbolTable);
        if (foundSymbol == null) {
            this.reports.add(Report.newError(Stage.SEMANTIC, Integer.valueOf(node.get("line")), Integer.valueOf(node.get("col")),
                    "Unknown reference to symbol " + nodeName + " when attempting to call " + nodeName + "." + calledMethod + "().",
                    null));

            return false;
        }

        String foundType = foundSymbol.getType().getName();
        // If the variable type is custom
        if (Utils.isCustomType(foundType)) {
            // Check if it is an object of the Class and if so check if the method exists
            if (foundType.equals(this.symbolTable.getClassName())) {
                if (this.symbolTable.hasInheritance()) return true;  // Assume it exists
                else if(!this.symbolTable.getMethods().contains(calledMethod)) {
                    this.reports.add(Report.newError(Stage.SEMANTIC, Integer.valueOf(node.get("line")), Integer.valueOf(node.get("col")),
                            "Unknown reference to method " + calledMethod + " when attempting to call " + nodeName + "." + calledMethod + "().",
                            null));
                    return false;
                }
            }

            return true;
        }

        this.reports.add(Report.newError(Stage.SEMANTIC, Integer.valueOf(node.get("line")), Integer.valueOf(node.get("col")),
                "Invalid attempt to call method " + calledMethod + " in primitive type " + foundType + ".",
                null));
        return false;
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
