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

    public OllirVisitor(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        builder = new StringBuilder();

        addVisit("Start", this::startVisit);
        addVisit("MainDecl", this::mainDeclVisit);
        addVisit("PublicMethod", this::publicMethodVisit);
        addVisit("AssignmentExpr", this::assignExprVisit);
        /*addVisit("DotMethod", this::dotMethodVisit);
        addVisit("IntArray", this::intArrayVisit);*/

        addVisit("IntegerLiteral", this::integerLiteralVisit);
        /*addVisit("BooleanLiteral", this::booleanLiteralVisit);
        addVisit("_Identifier", this::identifierVisit);*/

        addVisit("AddExpr", this::addExprVisit);
        /*addVisit("SubExpr", this::subExprVisit);
        addVisit("MultExpr", this::mulExprVisit);
        addVisit("DivExpr", this::divExprVisit);
        addVisit("LessExpr", this::lessExprVisit);

        addVisit("AndExpr", this::andExprVisit);
        addVisit("NotExpr", this::notExprVisit);

        addVisit("ArrayExpr", this::arrayExprVisit);

        addVisit("IfElse", this::conditionalVisit);
        addVisit("WhileSt", this::whileVisit);*/

        setDefaultVisit(this::defaultVisit);
    }

    private String assignExprVisit(JmmNode jmmNode, Object dummy) {

        return "";
    }

    private String integerLiteralVisit(JmmNode jmmNode, Object dummy) {
        return jmmNode.get("value") + ".i32";
    }

    private String addExprVisit(JmmNode jmmNode, Object dummy) {
        String lhs = visit(jmmNode.getJmmChild(0));
        String rhs = visit(jmmNode.getJmmChild(1));
        builder.append(lhs).append(" + ").append(rhs);

        return "";
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
}
