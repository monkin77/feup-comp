package pt.up.fe.comp.visitors;

import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp.symbolTable.EntityTypes;
import pt.up.fe.comp.symbolTable.MySymbol;
import pt.up.fe.comp.symbolTable.MySymbolTable;
import pt.up.fe.comp.symbolTable.Types;

import java.util.*;

public class VisitorEval extends AJmmVisitor<Object, Integer> {
    private final MySymbolTable symbolTable;
    private final Deque<MySymbol> scopeStack;
    private final List<Report> reports;

    public VisitorEval(MySymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        this.scopeStack = new ArrayDeque<>();
        this.reports = new ArrayList<>();

        MySymbol globalScope = new MySymbol(new Type(Types.NONE.toString(), false), "global", EntityTypes.GLOBAL);
        this.createScope(globalScope, null);

        addVisit("ClassDecl", this::classDeclVisit);
        addVisit("MainDecl", this::mainDeclVisit);
        addVisit("ImportDecl", this::importDeclVisit);
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

    private boolean createScope(MySymbol symbol, JmmNode node) {
        boolean opStatus = this.symbolTable.openScope(symbol);

        if (!opStatus) {
            this.reports.add(Report.newError(Stage.SEMANTIC, Integer.parseInt(node.get("line")), Integer.parseInt(node.get("col")),
                    "Error attempting to create a scope that is already defined: " + symbol.getName(),
                    null));
        } else
            this.scopeStack.push(symbol);

        return opStatus;
    }

    private boolean putSymbol(MySymbol symbol, JmmNode node) {
        // Insert next scope pointer in previous scope
        boolean opStatus = this.symbolTable.put(this.scopeStack.peek(), symbol);

        if (!opStatus) {
            this.reports.add(Report.newError(Stage.SEMANTIC, Integer.parseInt(node.get("line")), Integer.parseInt(node.get("col")),
                    "Error attempting to put a symbol that is already defined: " + symbol.getName() + " in the scope: " + this.scopeStack.peek().getName(),
                    null));
        }
        return !opStatus;
    }

    private Integer classDeclVisit(JmmNode node, Object dummy) {
        MySymbol classSymbol = new MySymbol(new Type(Types.UNKNOWN.toString(), false), node.get("name"), EntityTypes.CLASS);

        // Insert next scope pointer in previous scope
        if (this.putSymbol(classSymbol, node)) return -1;

        // Add new scope
        if (!this.createScope(classSymbol, node)) return -1;

        try {
            Optional<String> extendedClass = node.getOptional("extends");
            if (extendedClass.isPresent()) {
                MySymbol extendedSymbol = new MySymbol(new Type(Types.UNKNOWN.toString(), false), extendedClass.get(), EntityTypes.EXTENDS);
                if (this.putSymbol(extendedSymbol, node)) return -1;
            }
        } catch(Error ignored){
        }

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

        // Insert next scope pointer in previous scope
        if (this.putSymbol(mainSymbol, node)) return -1;

        // Add new scope
        if (!this.createScope(mainSymbol, node)) return -1;

        String argName = node.get("mainArgs");
        MySymbol argSymbol = new MySymbol(new Type(Types.STRING_ARRAY.toString(), true), argName, EntityTypes.ARG);
       if (this.putSymbol(argSymbol, node)) return -1;

        Integer visitResult = 0;
        for (int i = 0; i < node.getNumChildren(); ++i) {
            JmmNode childNode = node.getJmmChild(i);
            visitResult = visit(childNode);
        }

        this.scopeStack.pop();

        return visitResult;
    }

    private Integer importDeclVisit(JmmNode node, Object dummy) {
        if (node.getNumChildren() >= 1) {
            JmmNode childNode = node.getJmmChild(0);
            StringBuilder importName = new StringBuilder(childNode.get("id"));
            for (int i = 1; i < node.getNumChildren(); ++i) {
                childNode = node.getJmmChild(i);
                importName.append(".").append(childNode.get("id"));
            }

            MySymbol importSymbol = new MySymbol(new Type(Types.NONE.toString(), false), importName.toString(), EntityTypes.IMPORT);
            if (this.putSymbol(importSymbol, childNode)) return -1;

            return 0;
        }

        throw new RuntimeException("Illegal number of children in node " + "." + node.getKind());
    }

    private Integer varDeclVisit(JmmNode node, Object dummy) {
        if (node.getNumChildren() == 1) {
            String varName = node.get("name");

            Type returnNodeType = Utils.getNodeType(node.getJmmChild(0));

            MySymbol varSymbol = new MySymbol(returnNodeType, varName, EntityTypes.VARIABLE);

            if (this.putSymbol(varSymbol, node)) return -1;

            return 0;
        }

        throw new RuntimeException("Illegal number of children in node " + node.getKind() + ".");
    }

    private Integer publicMethodVisit(JmmNode node, Object dummy) {
        if (node.getNumChildren() <= 0)
            throw new RuntimeException("Illegal number of children in node " + node.getKind() + ".");

        Type returnNodeType = Utils.getNodeType(node.getJmmChild(0));

        MySymbol methodSymbol = new MySymbol(returnNodeType, node.get("name"), EntityTypes.METHOD);

        // Insert next scope pointer in previous scope
        if (this.putSymbol(methodSymbol, node)) return -1;

        // Add new scope
        if (!this.createScope(methodSymbol, node)) return -1;

        // Adds an entry of the list of parameters of the function
        if (!this.symbolTable.createParamScope(methodSymbol)) return -1;

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

        //TODO Should we be checking anything here? I think we can't declare variables
        // inside while loops; We can iterate the children and visit them in order to
        // accomplish this
        return 0;
    }

    /**
     * Visit argument, puts it in symbol table and in function arguments list
     */
    private Integer argumentVisit(JmmNode node, Object dummy) {
        if (node.getNumChildren() == 1) {
            Type returnNodeType = Utils.getNodeType(node.getJmmChild(0));
            String argName = node.get("arg");

            MySymbol argSymbol = new MySymbol(returnNodeType, argName, EntityTypes.ARG);
            if (this.putSymbol(argSymbol, node)) return -1;

            // Adds argument to List of arguments of the function
            if (!this.symbolTable.putArgument(this.scopeStack.peek(), argSymbol)) return -1;

            return 0;
        }

        throw new RuntimeException("Illegal number of children in node " + node.getKind() + ".");
    }

    private Integer assignExprVisit(JmmNode node, Object dummy) {
        // TODO: Confirm we don't need to store anything
        if (node.getNumChildren() == 2) {
            return 0;
        }

        throw new RuntimeException("Illegal number of children in node " + "." + node.getKind());
    }

    private Integer dotExpressionVisit(JmmNode node, Object dummy) {
        if (node.getNumChildren() == 2) {
            return 0;
        }

        throw new RuntimeException("Illegal number of children in node " + "." + node.getKind());
    }

    private Integer identifierVisit(JmmNode node, Object dummy) {
        if (node.getNumChildren() == 0) {
            return 0;
        }

        throw new RuntimeException("Illegal number of children in node " + node.getKind() + ".");
    }

    private Integer dotMethodVisit(JmmNode node, Object dummy) {
        return 0;
    }

    private Integer intArrayVisit(JmmNode node, Object dummy) {
        if (node.getNumChildren() == 0) {
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

    public List<Report> getReports() {
        return reports;
    }
}
