package pt.up.fe.comp.optimizations;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PostorderJmmVisitor;

public class NeutralAbsorbentOperationsVisitor extends PostorderJmmVisitor<Object, Boolean> {
    public NeutralAbsorbentOperationsVisitor() {
        addVisit("AndExpr", this::visitAndExpr);

        addVisit("AddExpr", this::visitAddExpr);
        addVisit("SubExpr", this::visitSubExpr);
        addVisit("MultExpr", this::visitMultExpr);
        addVisit("DivExpr", this::visitDivExpr);

        setReduceSimple(Boolean::logicalOr);
        setReduce((b, l) -> l.stream().reduce(b, Boolean::logicalOr));
        setDefaultValue(() -> false);
    }

    private Boolean visitAndExpr(JmmNode node, Object o) {
        JmmNode left = node.getJmmChild(0);
        JmmNode right = node.getJmmChild(1);
        if (left.getKind().equals("BooleanLiteral") && left.get("value").equals("false") ||
                right.getKind().equals("BooleanLiteral") && right.get("value").equals("true")) {
            OptimizerUtils.replaceWithPosition(node, left);
            return true;
        } else if (left.getKind().equals("BooleanLiteral") && left.get("value").equals("true") ||
                right.getKind().equals("BooleanLiteral") && right.get("value").equals("false")) {
            OptimizerUtils.replaceWithPosition(node, right);
            return true;
        }
        return false;
    }

    private Boolean visitAddExpr(JmmNode node, Object o) {
        JmmNode left = node.getJmmChild(0);
        JmmNode right = node.getJmmChild(1);
        if (left.getKind().equals("IntegerLiteral") && left.get("value").equals("0")) {
            OptimizerUtils.replaceWithPosition(node, right);
            return true;
        } else if (right.getKind().equals("IntegerLiteral") && right.get("value").equals("0")) {
            OptimizerUtils.replaceWithPosition(node, left);
            return true;
        }
        return false;
    }

    private Boolean visitSubExpr(JmmNode node, Object o) {
        JmmNode left = node.getJmmChild(0);
        JmmNode right = node.getJmmChild(1);
        if (right.getKind().equals("IntegerLiteral") && right.get("value").equals("0")) {
            OptimizerUtils.replaceWithPosition(node, left);
            return true;
        }
        return false;
    }

    private Boolean visitMultExpr(JmmNode node, Object o) {
        JmmNode left = node.getJmmChild(0);
        JmmNode right = node.getJmmChild(1);
        if (left.getKind().equals("IntegerLiteral") && left.get("value").equals("1")) {
            OptimizerUtils.replaceWithPosition(node, right);
            return true;
        } else if (right.getKind().equals("IntegerLiteral") && right.get("value").equals("1")) {
            OptimizerUtils.replaceWithPosition(node, left);
            return true;
        } else if (right.getKind().equals("IntegerLiteral") && right.get("value").equals("0")) {
            boolean sideEffectFree = left.getKind().equals("_Identifier");
            if (sideEffectFree) {
                OptimizerUtils.replaceWithPosition(node, right);
                return true;
            }
        } else if (left.getKind().equals("IntegerLiteral") && left.get("value").equals("0")) {
            boolean sideEffectFree = right.getKind().equals("_Identifier");
            if (sideEffectFree) {
                OptimizerUtils.replaceWithPosition(node, left);
                return true;
            }
        }
        return false;
    }

    private Boolean visitDivExpr(JmmNode node, Object o) {
        JmmNode left = node.getJmmChild(0);
        JmmNode right = node.getJmmChild(1);
        if (right.getKind().equals("IntegerLiteral") && right.get("value").equals("1")) {
            OptimizerUtils.replaceWithPosition(node, left);
            return true;
        }
        return false;
    }
}
