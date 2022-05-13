package pt.up.fe.comp.ollir;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.List;

public class OllirVisitor extends AJmmVisitor<Object, String> {
    private final StringBuilder builder;
    private final SymbolTable symbolTable;
    private int tempCounter;
    private String currentMethod;

    public OllirVisitor(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        builder = new StringBuilder();
        tempCounter = 0;

        addVisit("Start", this::startVisit);
        addVisit("MainDecl", this::mainDeclVisit);
        addVisit("PublicMethod", this::publicMethodVisit);
        addVisit("AssignmentExpr", this::assignExprVisit);
        addVisit("DotExpression", this::dotExpressionVisit);
        addVisit("DotMethod", this::dotMethodVisit);

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
        addVisit("ImportRegion", this::ignore);

//        addVisit("ArrayExpr", this::arrayExprVisit);
//        addVisit("IfElse", this::conditionalVisit);
//        addVisit("WhileSt", this::whileVisit);

        setDefaultVisit(this::defaultVisit);
    }

    private String dotExpressionVisit(JmmNode node, Object dummy) {
        String type = dummy == null ? "V" : (String) dummy;
        JmmNode lhs = node.getJmmChild(0);
        JmmNode rhs = node.getJmmChild(1);

        String lhsId = visit(lhs);
        String rhsId = visit(rhs, lhsId);
        builder.append(rhsId).append(".").append(type);
        return null;
    }

    private String dotMethodVisit(JmmNode node, Object dummy) {
        /*
        invokestatic(io, "println", t2.String, t3.i32);
		invokevirtual(c1.myClass, "put", 2.i32);
		invokespecial(c1.myClass,"<init>");
        */
//        String lhsId = visit(lhs);


        return null;
    }

    private String assignExprVisit(JmmNode jmmNode, Object dummy) {
        JmmNode lhs = jmmNode.getJmmChild(0);
        Symbol symbol = getSymbol(lhs);
        String assignType = OllirUtils.convertType(symbol.getType());

        JmmNode rhs = jmmNode.getJmmChild(1);
        final String id = visit(lhs);
        final String value = visit(rhs, assignType);
        // TODO: Types
        builder.append(id).append(" :=.").append(assignType).append(" ").append(value).append(";\n");
        return "";
    }

    private Symbol getSymbol(JmmNode node) {
        for (Symbol s : symbolTable.getLocalVariables(currentMethod)) {
            if (s.getName().equals(node.get("id"))) {
                return s;
            }
        }
        for (Symbol s : symbolTable.getParameters(currentMethod)) {
            if (s.getName().equals(node.get("id"))) {
                return s;
            }
        }
        return null;
    }

    private String integerLiteralVisit(JmmNode jmmNode, Object dummy) {
        return jmmNode.get("value") + ".i32";
    }

    private String booleanLiteralVisit(JmmNode jmmNode, Object dummy) {
        return jmmNode.get("value") + ".bool";
    }

    private String identifierVisit(JmmNode jmmNode, Object dummy) {
        // TODO: Type
        return jmmNode.get("id") + "." + OllirUtils.convertType(getSymbol(jmmNode).getType());
    }

    private String addExprVisit(JmmNode jmmNode, Object isNotTerminal) {
        return binOpVisit(jmmNode, isNotTerminal, "+", "i32", "i32");
    }

    private String subExprVisit(JmmNode jmmNode, Object isNotTerminal) {
        return binOpVisit(jmmNode, isNotTerminal, "-", "i32", "i32");
    }

    private String mulExprVisit(JmmNode jmmNode, Object isNotTerminal) {
        return binOpVisit(jmmNode, isNotTerminal, "*", "i32", "i32");
    }

    private String divExprVisit(JmmNode jmmNode, Object isNotTerminal) {
        return binOpVisit(jmmNode, isNotTerminal, "/", "i32", "i32");
    }

    private String lessExprVisit(JmmNode jmmNode, Object isNotTerminal) {
        return binOpVisit(jmmNode, isNotTerminal, "<", "bool", "i32");
    }

    private String andExprVisit(JmmNode jmmNode, Object isNotTerminal) {
        return binOpVisit(jmmNode, isNotTerminal, "&&", "bool", "bool");
    }

    private String notExprVisit(JmmNode jmmNode, Object isNotTerminal) {
        JmmNode node = jmmNode.getJmmChild(0);

        String rhs = visit(node, !OllirUtils.isTerminalNode(node));

        String calculation = '!' + "." + "bool" + " " + rhs + ";" + "\n";

        if (isNotTerminal != null && (Boolean) isNotTerminal) {
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

    private String methodDeclaration(JmmNode jmmNode, String methodName, Object isStatic) {
        currentMethod = methodName;
        builder.append(OllirConstants.TAB);
        builder.append(".method public ");
        if ((Boolean) isStatic) builder.append("static ");
        builder.append(methodName).append("(");

        List<Symbol> paremeters = symbolTable.getParameters(methodName);
        for (Symbol param : paremeters) {
            builder.append(param.getName());
            String type = OllirUtils.convertType(param.getType());
            builder.append(".").append(type);
            if (param != paremeters.get(paremeters.size() - 1)) builder.append(", ");
        }
        builder.append(")");

        Type returnType = symbolTable.getReturnType(methodName);
        builder.append(".").append(OllirUtils.convertType(returnType))
                .append(" {\n");

        defaultVisit(jmmNode, null);

        builder.append("\n").append(OllirConstants.TAB).append("}").append("\n");
        return "";
    }

    private String binOpVisit(JmmNode jmmNode, Object isNotTerminal, String operation, String returnType, String argumentType) {
        JmmNode lhsNode = jmmNode.getJmmChild(0);
        JmmNode rhsNode = jmmNode.getJmmChild(1);

        String lhs = visit(lhsNode, !OllirUtils.isTerminalNode(lhsNode));
        String rhs = visit(rhsNode, !OllirUtils.isTerminalNode(rhsNode));

        String calculation = lhs + " " + operation + "." + argumentType + " " + rhs;

        if (isNotTerminal != null && (Boolean) isNotTerminal) {
            String tempVariable = newTemp();
            builder.append(tempVariable).append(".").append(returnType).append(" :=.").append(returnType).append(" ").append(calculation).append(";\n");

            return tempVariable + '.' + returnType;
        }

        return calculation;
    }

    private String newTemp() {
        return "temp" + tempCounter++;
    }

    private String ignore(JmmNode jmmNode, Object dummy) {
        return "";
    }
}
