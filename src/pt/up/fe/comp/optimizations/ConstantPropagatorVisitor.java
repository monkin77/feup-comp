package pt.up.fe.comp.optimizations;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.JmmNodeImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConstantPropagatorVisitor extends PostorderVisitorProhibited<Object, Boolean> {
    private final JmmSemanticsResult result;
    private final Map<String, String> constantMap;
    private String currentMethod;

    public ConstantPropagatorVisitor(JmmSemanticsResult result) {
        super(List.of("IfElse", "WhileSt", "PublicMethod", "MainDecl"));
        this.result = result;
        constantMap = new HashMap<>();
        addVisit("_Identifier", this::visitIdentifier);
        addVisit("AssignmentExpr", this::visitAssignmentExpr);
        addVisit("IfElse", this::visitIfElse);
        addVisit("WhileSt", this::visitWhileSt);
        addVisit("DotMethod", this::visitDotMethod);
        addVisit("PublicMethod", this::visitPublicMethod);
        addVisit("MainDecl", this::visitMainDecl);

        setReduceSimple(Boolean::logicalOr);
        setReduce((b, l) -> l.stream().reduce(b, Boolean::logicalOr));
        setDefaultValue(() -> false);
    }

    private Boolean visitMainDecl(JmmNode node, Object o) {
        this.constantMap.clear();
        this.currentMethod = "main";
        visitAllChildren(node, o);
        return false;
    }

    private Boolean visitPublicMethod(JmmNode node, Object o) {
        this.constantMap.clear();
        this.currentMethod = node.get("name");
        visitAllChildren(node, o);
        return false;
    }

    private Boolean visitDotMethod(JmmNode node, Object o) {
        constantMap.clear();
        return false;
    }

    private Boolean visitWhileSt(JmmNode node, Object o) {
        JmmNode block = node.getJmmChild(1);
        JmmNode condition = node.getJmmChild(0);
        OptimizerUtils.getAssignTargets(block).forEach(this.constantMap::remove);
        return visit(condition, o) || visit(block, o);
    }

    private Boolean visitIfElse(JmmNode node, Object o) {
        JmmNode condition = node.getJmmChild(0);
        JmmNode ifBlock = node.getJmmChild(1);
        JmmNode elseBlock = node.getJmmChild(2);
        boolean b = visit(condition, o) || visit(ifBlock, o) || visit(elseBlock, o);
        OptimizerUtils.getAssignTargets(ifBlock).forEach(this.constantMap::remove);
        OptimizerUtils.getAssignTargets(elseBlock).forEach(this.constantMap::remove);
        return b;
    }

    private Boolean visitAssignmentExpr(JmmNode node, Object o) {
        JmmNode target = node.getJmmChild(0);
        if (!target.getKind().equals("_Identifier")) return false;
        String id = target.get("id");
        JmmNode value = node.getJmmChild(1);
        if (value.getKind().contains("Literal")) {
            constantMap.put(id, value.get("value"));
            return false;
        } else {
            constantMap.remove(id);
        }
        return false;
    }

    private Boolean visitIdentifier(JmmNode node, Object o) {
        // TODO: eh
        if (node.getJmmParent().getKind().equals("AssignmentExpr") && node.getIndexOfSelf() == 0) {
            return false;
        }
        String id = node.get("id");
        if (constantMap.containsKey(id)) {
            // TODO: A type-checking visitor should probably have annotated this.
            List<Symbol> symbols = result.getSymbolTable().getLocalVariables(currentMethod);
            symbols.addAll(result.getSymbolTable().getParameters(currentMethod));
            symbols.addAll(result.getSymbolTable().getFields());
            Symbol symbol = symbols.stream().filter(v -> v.getName().equals(id)).findFirst().orElseThrow();
            String symbolType = symbol.getType().getName();
            String kind = symbolType.equals("int") ? "IntegerLiteral" : "BooleanLiteral";
            JmmNode newNode = new JmmNodeImpl(kind);
            newNode.put("value", constantMap.get(id));
            OptimizerUtils.replaceWithPosition(node, newNode);
            return true;
        }
        return false;
    }
}
