package pt.up.fe.comp.optimizations;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.JmmNodeImpl;

import java.util.List;
import java.util.function.BiFunction;

public class ConstantFolderVisitor extends PostorderVisitorProhibited<Object, Boolean> {
    public ConstantFolderVisitor() {
        super(List.of("WhileSt"));

        addVisit("WhileSt", this::visitWhileSt);

        addVisit("LessExpr", this::visitLessExpr);

        addVisit("AndExpr", this::visitAndExpr);
        addVisit("NotExpr", this::visitNotExpr);

        addVisit("AddExpr", this::visitAddExpr);
        addVisit("SubExpr", this::visitSubExpr);
        addVisit("MultExpr", this::visitMultExpr);
        addVisit("DivExpr", this::visitDivExpr);

        setReduceSimple(Boolean::logicalOr);
        setReduce((b, l) -> l.stream().reduce(b, Boolean::logicalOr));
        setDefaultValue(() -> false);
    }

    private Boolean visitWhileSt(JmmNode node, Object o) {
        JmmNode condition = node.getJmmChild(0).getJmmChild(0);
        JmmNode body = node.getJmmChild(1).getJmmChild(0);
        JmmNode mutableCondition = node.getJmmChild(2);
        visit(mutableCondition, o);
        if (mutableCondition.getJmmChild(0).getKind().equals("BooleanLiteral")) {
            node.put("doWhile", mutableCondition.getJmmChild(0).get("value"));
        }
        return visit(condition, o) || visit(body, o);
    }

    private Boolean visitAndExpr(JmmNode node, Object o) {
        JmmNode left = node.getJmmChild(0);
        JmmNode right = node.getJmmChild(1);
        if (left.getKind().equals("BooleanLiteral") && right.getKind().equals("BooleanLiteral")) {
            boolean leftValue = Boolean.parseBoolean(left.get("value"));
            boolean rightValue = Boolean.parseBoolean(right.get("value"));
            boolean result = leftValue && rightValue;

            JmmNodeImpl newNode = new JmmNodeImpl("BooleanLiteral");
            newNode.put("value", String.valueOf(result));
            OptimizerUtils.replaceWithPosition(node, newNode);
            return true;
        }
        return false;
    }

    private Boolean visitLessExpr(JmmNode node, Object o) {
        JmmNode left = node.getJmmChild(0);
        JmmNode right = node.getJmmChild(1);
        if (left.getKind().equals("IntegerLiteral") && right.getKind().equals("IntegerLiteral")) {
            int leftValue = Integer.parseInt(left.get("value"));
            int rightValue = Integer.parseInt(right.get("value"));
            boolean result = leftValue < rightValue;

            JmmNodeImpl newNode = new JmmNodeImpl("BooleanLiteral");
            newNode.put("value", String.valueOf(result));
            OptimizerUtils.replaceWithPosition(node, newNode);
            return true;
        }
        return false;
    }

    private Boolean visitNotExpr(JmmNode node, Object o) {
        JmmNode child = node.getJmmChild(0);
        if (child.getKind().equals("BooleanLiteral")) {
            JmmNodeImpl newNode = new JmmNodeImpl("BooleanLiteral");
            boolean value = Boolean.parseBoolean(child.get("value"));
            newNode.put("value", String.valueOf(!value));
            OptimizerUtils.replaceWithPosition(node, newNode);
            return true;
        }
        return false;
    }

    private Boolean visitAddExpr(JmmNode node, Object o) {
        return visitArithmeticExpr(node, Integer::sum);
    }

    private Boolean visitSubExpr(JmmNode node, Object o) {
        return visitArithmeticExpr(node, (a, b) -> a - b);
    }

    private Boolean visitMultExpr(JmmNode node, Object o) {
        return visitArithmeticExpr(node, (l, r) -> l * r);
    }

    private Boolean visitDivExpr(JmmNode node, Object o) {
        return visitArithmeticExpr(node, (l, r) -> l / r);
    }

    private Boolean visitArithmeticExpr(JmmNode node, BiFunction<Integer, Integer, Integer> f) {
        JmmNode left = node.getJmmChild(0);
        JmmNode right = node.getJmmChild(1);
        if (left.getKind().equals("IntegerLiteral") && right.getKind().equals("IntegerLiteral")) {
            int leftValue = Integer.parseInt(left.get("value"));
            int rightValue = Integer.parseInt(right.get("value"));
            Integer result = f.apply(leftValue, rightValue);

            JmmNodeImpl newNode = new JmmNodeImpl("IntegerLiteral");
            newNode.put("value", String.valueOf(result));
            OptimizerUtils.replaceWithPosition(node, newNode);
            return true;
        }
        return false;
    }
}
