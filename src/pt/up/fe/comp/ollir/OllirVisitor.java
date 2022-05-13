package pt.up.fe.comp.ollir;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.List;

public class OllirVisitor extends AJmmVisitor<Boolean, String> {
    private final StringBuilder builder;
    private final SymbolTable symbolTable;
    private int tempCounter;

    public OllirVisitor(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        builder = new StringBuilder();
        tempCounter = 0;

        addVisit("Start", this::startVisit);
        addVisit("MainDecl", this::mainDeclVisit);
        addVisit("PublicMethod", this::publicMethodVisit);
        addVisit("AssignmentExpr", this::assignExprVisit);
        /*addVisit("DotMethod", this::dotMethodVisit);
        addVisit("IntArray", this::intArrayVisit);*/

        addVisit("IntegerLiteral", this::integerLiteralVisit);
        addVisit("BooleanLiteral", this::booleanLiteralVisit);
        addVisit("_Identifier", this::identifierVisit);

        addVisit("AddExpr", this::addExprVisit);
        addVisit("SubExpr", this::subExprVisit);
        addVisit("MultExpr", this::mulExprVisit);
        addVisit("DivExpr", this::divExprVisit);
        addVisit("LessExpr", this::lessExprVisit);

        addVisit("AndExpr", this::andExprVisit);
        addVisit("NotExpr", this::notExprVisit);

//        addVisit("ArrayExpr", this::arrayExprVisit);

//        addVisit("IfElse", this::conditionalVisit);
//        addVisit("WhileSt", this::whileVisit);

        setDefaultVisit(this::defaultVisit);
    }

    private String assignExprVisit(JmmNode jmmNode, Object dummy) {
        return "";
    }

    private String integerLiteralVisit(JmmNode jmmNode, Object dummy) {
        return jmmNode.get("value") + ".i32";
    }

    private String booleanLiteralVisit(JmmNode jmmNode, Object dummy) {
        return jmmNode.get("value") + ".bool";
    }

    private String identifierVisit(JmmNode jmmNode, Object dummy) {
        // TODO: Type
        return jmmNode.get("id");
    }

    private String addExprVisit(JmmNode jmmNode, Boolean isNotTerminal) {
        return binOpVisit(jmmNode, isNotTerminal, "+", "i32", "i32");
    }

    private String subExprVisit(JmmNode jmmNode, Boolean isNotTerminal) {
        return binOpVisit(jmmNode, isNotTerminal, "-", "i32", "i32");
    }

    private String mulExprVisit(JmmNode jmmNode, Boolean isNotTerminal) {
        return binOpVisit(jmmNode, isNotTerminal, "*", "i32", "i32");
    }

    private String divExprVisit(JmmNode jmmNode, Boolean isNotTerminal) {
        return binOpVisit(jmmNode, isNotTerminal, "/", "i32", "i32");
    }

    private String lessExprVisit(JmmNode jmmNode, Boolean isNotTerminal) {
        return binOpVisit(jmmNode, isNotTerminal, "<", "bool", "i32");
    }

    private String andExprVisit(JmmNode jmmNode, Boolean isNotTerminal) {
        return binOpVisit(jmmNode, isNotTerminal, "&&", "bool", "bool");
    }

    private String notExprVisit(JmmNode jmmNode, Boolean isNotTerminal) {
        JmmNode node = jmmNode.getJmmChild(0);

        String rhs = visit(node, !OllirUtils.isTerminalNode(node));

        String calculation = '!' + "." + "bool" + " " + rhs + "\n";

        if (isNotTerminal != null && isNotTerminal) {
            String tempVariable = newTemp();
            builder.append(tempVariable).append(".").append("bool").append(" :=.").append("bool").append(" ").append(calculation);
            return tempVariable + '.' + "bool";
        }

        return calculation;
    }

    private String publicMethodVisit(JmmNode jmmNode, Object o) {
        return methodDeclaration(jmmNode, jmmNode.get("name"), false);
    }

    private String mainDeclVisit(JmmNode jmmNode, Object dummy) {
        return methodDeclaration(jmmNode, "main", true);
    }

    private String startVisit(JmmNode jmmNode, Object dummy) {
        defaultVisit(jmmNode, dummy);
        return builder.toString();
    }

    private String defaultVisit(JmmNode node, Object dummy) {
        if (node.getNumChildren() < 0) {
            throw new RuntimeException("Illegal number of children in node " + node.getKind() + ".");
        }

        for (int i = 0; i < node.getNumChildren(); ++i) {
            JmmNode childNode = node.getJmmChild(i);
            visit(childNode);
        }

        return "";
    }

    private String methodDeclaration(JmmNode jmmNode, String methodName, boolean isStatic) {
        builder.append(OllirConstants.TAB);
        builder.append(".method public ");
        if (isStatic) builder.append("static ");
        builder.append(methodName).append("(");

        List<Symbol> paremeters = symbolTable.getParameters(methodName);
        for (Symbol param : paremeters) {
            builder.append(param.getName());
            String type = OllirUtils.convertType(param.getType());
            builder.append(".").append(type).append(")");
        }

        Type returnType = symbolTable.getReturnType(methodName);
        builder.append(".").append(OllirUtils.convertType(returnType))
                .append(" {\n");

        defaultVisit(jmmNode, null);

        builder.append("\n").append(OllirConstants.TAB).append("}");
        return "";
    }

    private String binOpVisit(JmmNode jmmNode, Boolean isNotTerminal, String operation, String returnType, String argumentType) {
        JmmNode lhsNode = jmmNode.getJmmChild(0);
        JmmNode rhsNode = jmmNode.getJmmChild(1);

        String lhs = visit(lhsNode, !OllirUtils.isTerminalNode(lhsNode));
        String rhs = visit(rhsNode, !OllirUtils.isTerminalNode(rhsNode));

        String calculation = lhs + " " + operation + "." + argumentType + " " + rhs + "\n";

        if (isNotTerminal != null && isNotTerminal) {
            String tempVariable = newTemp();
            builder.append(tempVariable).append(".").append(returnType).append(" :=.").append(returnType).append(" ").append(calculation);

            return tempVariable + '.' + returnType;
        }

        return calculation;
    }

    private String newTemp() {
        return "temp" + tempCounter++;
    }
}
