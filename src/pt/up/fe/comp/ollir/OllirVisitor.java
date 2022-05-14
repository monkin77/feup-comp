package pt.up.fe.comp.ollir;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.visitors.Utils;

import java.util.List;

import static pt.up.fe.comp.ollir.OllirUtils.getSymbol;

public class OllirVisitor extends AJmmVisitor<ArgumentPool, String> {
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
        addVisit("ClosedStatement", this::closedStVisit);
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
        addVisit("_This", this::thisVisit);
//        addVisit("ArrayExpr", this::arrayExprVisit);
//        addVisit("IfElse", this::conditionalVisit);
//        addVisit("WhileSt", this::whileVisit);

        setDefaultVisit(this::defaultVisit);
    }

    private String closedStVisit(JmmNode node, ArgumentPool argumentPool) {

        JmmNode childNode = node.getJmmChild(0);
        builder.append(visit(childNode));
        builder.append(";\n");

        return "";
    }

    private String dotExpressionVisit(JmmNode node, ArgumentPool argumentPool) {
        String type = argumentPool.getType() == null ? "V" : argumentPool.getType();
        JmmNode lhs = node.getJmmChild(0);
        JmmNode rhs = node.getJmmChild(1);

        String lhsId = visit(lhs);
        String rhsId = visit(rhs, new ArgumentPool(lhsId));
        // TODO dot methods returning void except assignment
        // TODO dot length
        // TODO invokespecial
        // TODO ifelse, arrayexpr, whileSt
        // TODO remover esparguete
        // TODO remover os builders

        String invokeExpr = rhsId + ")" + "." + type;

        if (argumentPool.getIsNotTerminal()) {
            String tempVariable = newTemp();
            builder.append(tempVariable).append(".").append(type)
                    .append(" :=.").append(type).append(" ").append(invokeExpr)
                    .append(";\n");
            return tempVariable + '.' + type;
        }

        return invokeExpr;
    }

    private String dotMethodVisit(JmmNode node, ArgumentPool argumentPool) {

        String id = argumentPool.getId();
        Symbol symbol = getSymbol(id, currentMethod, symbolTable);

        StringBuilder sb = new StringBuilder();
        String method = "\"" + node.get("method") + "\"";
        if (symbol == null) {
            if (Utils.hasImport(id, symbolTable)) {
                sb.append("invokestatic(").append(id);
            } else if (id.equals("this")) {
                sb.append("invokevirtual(this");
            } else {
                throw new RuntimeException("Invalid symbol method invocation");
            }
        } else {
            sb.append("invokevirtual(").append(symbol.getName()).append(".").append(OllirUtils.convertType(symbol.getType()));
        }

        sb.append(", ").append(method);
        for (int i = 0; i < node.getNumChildren(); ++i) {
            JmmNode childNode = node.getJmmChild(i);
            sb.append(", ");
            sb.append(visit(childNode));
        }
        // return will be appended in DotExpression

        return sb.toString();
    }

    private String assignExprVisit(JmmNode jmmNode, ArgumentPool argumentPool) {
        JmmNode lhs = jmmNode.getJmmChild(0);
        Symbol symbol = getSymbol(lhs.get("id"), currentMethod, symbolTable);
        String assignType = OllirUtils.convertType(symbol.getType());

        JmmNode rhs = jmmNode.getJmmChild(1);
        final String id = visit(lhs);

        final String value = visit(rhs, new ArgumentPool(assignType, OllirUtils.isNotTerminalNode(lhs)));
        // TODO: Types
        return id + " :=." + assignType + " " + value;
    }

    private String integerLiteralVisit(JmmNode jmmNode, ArgumentPool argumentPool) {
        return jmmNode.get("value") + ".i32";
    }

    private String booleanLiteralVisit(JmmNode jmmNode, ArgumentPool argumentPool) {
        return jmmNode.get("value") + ".bool";
    }

    private String identifierVisit(JmmNode jmmNode, ArgumentPool argumentPool) {
        // TODO: Type
        Symbol nodeSymbol = getSymbol(jmmNode.get("id"), currentMethod, symbolTable);
        return jmmNode.get("id") + (nodeSymbol != null ? "." + OllirUtils.convertType(nodeSymbol.getType()) : "");
    }

    private String addExprVisit(JmmNode jmmNode, ArgumentPool argumentPool) {
        return binOpVisit(jmmNode, argumentPool.getIsNotTerminal(), "+", "i32", "i32");
    }

    private String subExprVisit(JmmNode jmmNode, ArgumentPool argumentPool) {
        return binOpVisit(jmmNode, argumentPool.getIsNotTerminal(), "-", "i32", "i32");
    }

    private String mulExprVisit(JmmNode jmmNode, ArgumentPool argumentPool) {
        return binOpVisit(jmmNode, argumentPool.getIsNotTerminal(), "*", "i32", "i32");
    }

    private String divExprVisit(JmmNode jmmNode, ArgumentPool argumentPool) {
        return binOpVisit(jmmNode, argumentPool.getIsNotTerminal(), "/", "i32", "i32");
    }

    private String lessExprVisit(JmmNode jmmNode, ArgumentPool argumentPool) {
        return binOpVisit(jmmNode, argumentPool.getIsNotTerminal(), "<", "bool", "i32");
    }

    private String andExprVisit(JmmNode jmmNode, ArgumentPool argumentPool) {
        return binOpVisit(jmmNode, argumentPool.getIsNotTerminal(), "&&", "bool", "bool");
    }

    private String notExprVisit(JmmNode jmmNode, ArgumentPool argumentPool) {
        JmmNode node = jmmNode.getJmmChild(0);
        Symbol symbol = getSymbol(node.get("id"), currentMethod, symbolTable);
        String assignType = OllirUtils.convertType(symbol.getType());

        String rhs = visit(node, new ArgumentPool(assignType, OllirUtils.isNotTerminalNode(node)));

        String calculation = '!' + "." + "bool" + " " + rhs + ";" + "\n";

        if (argumentPool.getIsNotTerminal() != null && argumentPool.getIsNotTerminal()) {
            String tempVariable = newTemp();
            builder.append(tempVariable).append(".").append("bool").append(" :=.").append("bool").append(" ").append(calculation);
            return tempVariable + '.' + "bool";
        }

        return calculation;
    }

    private String publicMethodVisit(JmmNode jmmNode, ArgumentPool o) {
        return methodDeclaration(jmmNode, jmmNode.get("name"), false);
    }

    private String mainDeclVisit(JmmNode jmmNode, ArgumentPool argumentPool) {
        return methodDeclaration(jmmNode, "main", true);
    }

    private String startVisit(JmmNode jmmNode, ArgumentPool argumentPool) {
        defaultVisit(jmmNode, argumentPool);
        return builder.toString();
    }

    private String thisVisit(JmmNode jmmNode, ArgumentPool argumentPool) {
        if (jmmNode.getNumChildren() > 0) {
            throw new RuntimeException("Illegal number of children in node " + jmmNode.getKind() + ".");
        }

        return "this";
    }

    private String defaultVisit(JmmNode node, ArgumentPool argumentPool) {
        if (node.getNumChildren() < 0) {
            throw new RuntimeException("Illegal number of children in node " + node.getKind() + ".");
        }

        for (int i = 0; i < node.getNumChildren(); ++i) {
            JmmNode childNode = node.getJmmChild(i);
            visit(childNode);
        }

        return "";
    }

    private String methodDeclaration(JmmNode jmmNode, String methodName, Boolean isStatic) {
        currentMethod = methodName;
        builder.append(OllirConstants.TAB);
        builder.append(".method public ");
        if (isStatic) builder.append("static ");
        builder.append(methodName).append("(");

        List<Symbol> parameters = symbolTable.getParameters(methodName);
        for (Symbol param : parameters) {
            builder.append(param.getName());
            String type = OllirUtils.convertType(param.getType());
            builder.append(".").append(type);
            if (param != parameters.get(parameters.size() - 1)) builder.append(", ");
        }
        builder.append(")");

        Type returnType = symbolTable.getReturnType(methodName);
        builder.append(".").append(OllirUtils.convertType(returnType))
                .append(" {\n");

        defaultVisit(jmmNode, null);

        builder.append("\n").append(OllirConstants.TAB).append("}").append("\n");
        return "";
    }

    private String binOpVisit(JmmNode jmmNode, Boolean isNotTerminal, String operation, String returnType, String argumentType) {
        JmmNode lhsNode = jmmNode.getJmmChild(0);
        JmmNode rhsNode = jmmNode.getJmmChild(1);

        String lhs = visit(lhsNode, new ArgumentPool(returnType, OllirUtils.isNotTerminalNode(lhsNode)));
        String rhs = visit(rhsNode, new ArgumentPool(returnType, OllirUtils.isNotTerminalNode(rhsNode)));

        String calculation = lhs + " " + operation + "." + argumentType + " " + rhs;

        if (isNotTerminal != null && isNotTerminal) {
            String tempVariable = newTemp();
            builder.append(tempVariable).append(".").append(returnType).append(" :=.").append(returnType).append(" ").append(calculation).append(";\n");

            return tempVariable + '.' + returnType;
        }

        return calculation;
    }

    private String newTemp() {
        return "temp" + tempCounter++;
    }

    private String ignore(JmmNode jmmNode, ArgumentPool argumentPool) {
        return "";
    }
}
