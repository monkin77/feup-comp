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
        addVisit("DotLength", this::dotLengthVisit);

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
        addVisit("ArrayExpr", this::arrayExprVisit);
//        addVisit("IfElse", this::conditionalVisit);
//        addVisit("WhileSt", this::whileVisit);

        setDefaultVisit(this::defaultVisit);
    }

    private String arrayExprVisit(JmmNode node, ArgumentPool argumentPool) {
        JmmNode lhs = node.getJmmChild(0);
        JmmNode rhs = node.getJmmChild(1);
        ArgumentPool leftArgumentPool = new ArgumentPool(null, OllirUtils.isNotTerminalNode(lhs));
        ArgumentPool rightArgumentPool = new ArgumentPool(null, true);
        // TODO: Identifier should probably not return the type or have a flag.
        final String lhsId = visit(lhs, leftArgumentPool).split("\\.")[0];
        final String rhsId = visit(rhs, rightArgumentPool);
        // TODO: Even though we only have int[], this feels weird.
        final String arrayType = "i32";
        return "%s[%s].%s".formatted(lhsId, rhsId, arrayType);
    }

    private String closedStVisit(JmmNode node, ArgumentPool argumentPool) {
        return defaultVisit(node, null) + ";\n";
    }

    private String dotExpressionVisit(JmmNode node, ArgumentPool argumentPool) {
        JmmNode lhs = node.getJmmChild(0);
        JmmNode rhs = node.getJmmChild(1);

        ArgumentPool leftArg = new ArgumentPool(null, OllirUtils.isNotTerminalNode(lhs));
        String lhsId = visit(lhs, leftArg);

        ArgumentPool rightArg = new ArgumentPool(lhsId);
        rightArg.setAssignmentType(argumentPool.getType());
        String rhsId = visit(rhs, rightArg);
        // TODO dot methods returning void except assignment
        // TODO dot length
        // TODO invokespecial
        // TODO ifelse, arrayexpr, whileSt
        // TODO remover esparguete
        // TODO remover os builders
        // TODO putfield, getfield

        String type = argumentPool.getType() == null ? rightArg.getReturnType() : argumentPool.getType();
        if (argumentPool.getIsNotTerminal()) return createTempVariable(type, rhsId);

        return rhsId;
    }

    private String dotMethodVisit(JmmNode node, ArgumentPool argumentPool) {
        String id = argumentPool.getId();
        Symbol symbol = getSymbol(id, currentMethod, symbolTable);

        StringBuilder sb = new StringBuilder();
        String method = "\"" + node.get("method") + "\"";

        if (symbol == null) {
            if (Utils.hasImport(id, symbolTable)) {
                sb.append("invokestatic(").append(id);
                argumentPool.setReturnType("V");
            } else {
                // Assume it's a symbol from our class
                sb.append("invokevirtual(").append(id);
                argumentPool.setReturnType(this.symbolTable.getReturnType(node.get("method")).getName());
            }
        } else {
            sb.append("invokevirtual(").append(symbol.getName()).append(".").append(OllirUtils.convertType(symbol.getType()));

            if (symbol.getType().getName().equals(this.symbolTable.getClassName())) {
                // variable of class type
                argumentPool.setReturnType(this.symbolTable.getReturnType(node.get("method")).getName());
            } else {
                argumentPool.setReturnType("V");
            }
        }

        sb.append(", ").append(method);
        for (int i = 0; i < node.getNumChildren(); ++i) {
            JmmNode childNode = node.getJmmChild(i);
            sb.append(", ");
            sb.append(visit(childNode));
        }
        // return will be appended in DotExpression

        String type = argumentPool.getAssignmentType() == null ? argumentPool.getReturnType() : argumentPool.getAssignmentType();
        sb.append(").").append(type);
        return sb.toString();
    }

    private String dotLengthVisit(JmmNode node, ArgumentPool argumentPool) {
        String id = argumentPool.getId();
        StringBuilder sb = new StringBuilder();

        sb.append("arraylength(").append(id).append(").i32");
        return sb.toString();
    }

    private String assignExprVisit(JmmNode jmmNode, ArgumentPool argumentPool) {
        JmmNode lhs = jmmNode.getJmmChild(0);
        JmmNode rhs = jmmNode.getJmmChild(1);

        String lhsId = visit(lhs, new ArgumentPool(null, OllirUtils.isNotTerminalNode(lhs)));
        // TODO: This should probably not be necessary, because visit should not return type information.
        String assignType = lhsId.split("\\.", 2)[1];

        final String value = visit(rhs, new ArgumentPool(assignType, OllirUtils.isNotTerminalNode(rhs)));
        // TODO: Types
        return lhsId + " :=." + assignType + " " + value;
    }

    private String integerLiteralVisit(JmmNode jmmNode, ArgumentPool argumentPool) {
        final String calculation = jmmNode.get("value") + ".i32";
        if (argumentPool != null && argumentPool.getIsNotTerminal()) return createTempVariable("i32", calculation);
        return calculation;
    }

    private String booleanLiteralVisit(JmmNode jmmNode, ArgumentPool argumentPool) {
        return jmmNode.get("value") + ".bool";
    }

    private String identifierVisit(JmmNode jmmNode, ArgumentPool argumentPool) {
        final Symbol nodeSymbol = getSymbol(jmmNode.get("id"), currentMethod, symbolTable);
        final String type = nodeSymbol != null ? OllirUtils.convertType(nodeSymbol.getType()) : "";
        final String annotatedId = jmmNode.get("id") + (type.isEmpty() ? "" : "." + type);
        // TODO: Really weird way to see if this is a class field. Maybe getSymbol should help here?
        if (symbolTable.getFields().stream().anyMatch(x -> x.getName().equals(jmmNode.get("id")))) {
            // TODO: Getfield should also work for object fields (not just this)
            final String objId = "this";
            final String calculation = ("getfield(%s, %s).%s").formatted(objId, annotatedId, type);
            return createTempVariable(type, calculation);
        }
        // TODO: Type
        return annotatedId;
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

        if (argumentPool.getIsNotTerminal()) return createTempVariable("bool", calculation);

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

        StringBuilder builder = new StringBuilder();
        for (JmmNode childNode : node.getChildren()) {
            builder.append(visit(childNode, new ArgumentPool()));
        }

        return builder.toString();
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
        builder.append(".").append(OllirUtils.convertType(returnType)).append(" {\n");

        builder.append(defaultVisit(jmmNode, null));

        builder.append("\n");
        // TODO: Should probably check if the return is already there: AST?
        if (returnType.getName().equals("void")) builder.append("ret.V").append(";\n");
        builder.append(OllirConstants.TAB).append("}").append("\n");
        return "";
    }

    private String binOpVisit(JmmNode jmmNode, boolean isNotTerminal, String operation, String returnType, String argumentType) {
        JmmNode lhsNode = jmmNode.getJmmChild(0);
        JmmNode rhsNode = jmmNode.getJmmChild(1);

        String lhs = visit(lhsNode, new ArgumentPool(returnType, OllirUtils.isNotTerminalNode(lhsNode)));
        String rhs = visit(rhsNode, new ArgumentPool(returnType, OllirUtils.isNotTerminalNode(rhsNode)));

        String calculation = lhs + " " + operation + "." + argumentType + " " + rhs;

        if (isNotTerminal) return createTempVariable(returnType, calculation);

        return calculation;
    }

    private String createTempVariable(String type, String calculation) {
        String tempVariableName = "temp" + tempCounter++;
        builder.append(tempVariableName).append(".").append(type).append(" :=.").append(type).append(" ").append(calculation).append(";\n");
        return tempVariableName + '.' + type;
    }

    private String ignore(JmmNode jmmNode, ArgumentPool argumentPool) {
        return "";
    }

    @Override
    public String visit(JmmNode jmmNode) {
        return super.visit(jmmNode, new ArgumentPool());
    }
}
