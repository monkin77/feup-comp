package pt.up.fe.comp.optimizations;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.JmmNodeImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConstantPropagatorVisitor extends PostorderVisitorProhibited<Object, Boolean> {
    private final JmmSemanticsResult result;
    private final Map<String, String> constantMap;

    public ConstantPropagatorVisitor(JmmSemanticsResult result) {
        super(List.of("IfElse", "WhileSt"));
        this.result = result;
        constantMap = new HashMap<>();
        addVisit("_Identifier", this::visitIdentifier);
        addVisit("AssignmentExpr", this::visitAssignmentExpr);
        addVisit("IfElse", this::visitIfElse);
        addVisit("WhileSt", this::visitWhileSt);

        setReduceSimple(Boolean::logicalOr);
        setReduce((b, l) -> l.stream().reduce(b, Boolean::logicalOr));
        setDefaultValue(() -> false);
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
        String id = node.getJmmChild(0).get("id");
        JmmNode value = node.getJmmChild(1);
        if (value.getKind().contains("Literal")) {
            constantMap.put(id, value.get("value"));
            return false;
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
            String kind = "IntegerLiteral";
            JmmNode newNode = new JmmNodeImpl(kind);
            newNode.put("value", constantMap.get(id));
            OptimizerUtils.replaceWithPosition(node, newNode);
            return true;
        }
        return false;
    }
}
