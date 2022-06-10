package pt.up.fe.comp.optimizations;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.JmmNodeImpl;
import pt.up.fe.comp.jmm.ast.PostorderJmmVisitor;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class BooleanSimplifierVisitor extends PostorderJmmVisitor<Object, Boolean> {
    public BooleanSimplifierVisitor() {
        addVisit("AndExpr", this::andExprVisit);
        addVisit("OrExpr", this::orExprVisit);
        addVisit("NotExpr", this::notExprVisit);

        setReduceSimple(Boolean::logicalOr);
        setReduce((b, l) -> l.stream().reduce(b, Boolean::logicalOr));
        setDefaultValue(() -> false);
    }

    private Boolean notExprVisit(JmmNode node, Object o) {
        JmmNode child = node.getJmmChild(0);
        String childKind = child.getKind();
        if (childKind.equals("NotExpr")) {
            JmmNode grandChild = child.getJmmChild(0);
            node.replace(grandChild);
            return true;
        } else if (child.getNumChildren() == 2) {
            if (BooleanRulesMap.notReduce.containsKey(childKind)) {
                String newKind = BooleanRulesMap.notReduce.get(childKind);
                JmmNode lhs = child.getJmmChild(0);
                JmmNode rhs = child.getJmmChild(1);
                replaceWithKindCopy2Children(node, newKind, lhs, rhs);
                return true;
            } else if (BooleanRulesMap.notDeMorgan.containsKey(childKind)) {
                JmmNode lhs = child.getJmmChild(0);
                if (!lhs.getKind().equals("NotExpr")) return false;
                JmmNode rhs = child.getJmmChild(1);
                if (!rhs.getKind().equals("NotExpr")) return false;
                JmmNode lhsChild = lhs.getJmmChild(0);
                JmmNode rhsChild = rhs.getJmmChild(0);
                String newKind = BooleanRulesMap.notDeMorgan.get(childKind);
                replaceWithKindCopy2Children(node, newKind, lhsChild, rhsChild);
                return true;
            }
        }
        return false;
    }

    private Boolean andExprVisit(JmmNode node, Object o) {
        JmmNode lhs = node.getJmmChild(0);
        JmmNode rhs = node.getJmmChild(1);
        if (lhs.getNumChildren() != 2) return false;
        if (rhs.getNumChildren() != 2) return false;
        String lhsKind = lhs.getKind();
        String rhsKind = rhs.getKind();
        JmmNode lhsLhs = lhs.getJmmChild(0);
        if (!lhsLhs.getKind().equals("_Identifier") && !lhsLhs.getKind().equals("IntegerLiteral")) return false;
        JmmNode lhsRhs = lhs.getJmmChild(1);
        if (!lhsRhs.getKind().equals("_Identifier") && !lhsRhs.getKind().equals("IntegerLiteral")) return false;
        JmmNode rhsLhs = rhs.getJmmChild(0);
        if (!rhsLhs.getKind().equals("_Identifier") && !rhsLhs.getKind().equals("IntegerLiteral")) return false;
        JmmNode rhsRhs = rhs.getJmmChild(1);
        if (!rhsRhs.getKind().equals("_Identifier") && !rhsRhs.getKind().equals("IntegerLiteral")) return false;
        String lhsLhsId = lhsLhs.getKind().equals("_Identifier") ? lhsLhs.get("id") : lhsLhs.get("value");
        String lhsRhsId = lhsRhs.getKind().equals("_Identifier") ? lhsRhs.get("id") : lhsRhs.get("value");
        String rhsLhsId = rhsLhs.getKind().equals("_Identifier") ? rhsLhs.get("id") : rhsLhs.get("value");
        String rhsRhsId = rhsRhs.getKind().equals("_Identifier") ? rhsRhs.get("id") : rhsRhs.get("value");
        boolean sameVariableOrder = lhsLhsId.equals(rhsLhsId) && lhsRhsId.equals(rhsRhsId);
        boolean switchedVariableOrder = lhsLhsId.equals(rhsRhsId) && lhsRhsId.equals(rhsLhsId);
        boolean sameOperation = lhsKind.equals(rhsKind);

        if (sameVariableOrder) {
            if (sameOperation) {
                node.replace(lhs);
            } else simplify(node, lhsKind, rhsKind, lhsLhs, lhsRhs, BooleanRulesMap.normalAndFalse, BooleanRulesMap.normalAndReduce, false);
        } else if (switchedVariableOrder) {
            if (sameOperation) {
                boolean equal = lhsKind.equals("LessEqualExpr") || lhsKind.equals("GreaterEqualExpr");
                boolean isFalse = lhsKind.equals("LessExpr") || lhsKind.equals("GreaterExpr");
                if (equal) replaceWithKindCopy2Children(node, "EqualExpr", lhsLhs, lhsRhs);
                else if (isFalse) replaceWithFalse(node);
                else if (lhsKind.equals("EqualExpr") || lhsKind.equals("NotEqualExpr")) node.replace(lhs);
                else return false;
            } else {
                simplify(node, lhsKind, rhsKind, lhsLhs, lhsRhs, BooleanRulesMap.switchedAndFalse, BooleanRulesMap.switchedAndReduce, true);
            }
        } else {
            return false;
        }
        return true;
    }

    private Boolean orExprVisit(JmmNode node, Object o) {
        JmmNode lhs = node.getJmmChild(0);
        JmmNode rhs = node.getJmmChild(1);
        if (lhs.getNumChildren() != 2) return false;
        if (rhs.getNumChildren() != 2) return false;
        String lhsKind = lhs.getKind();
        String rhsKind = rhs.getKind();
        JmmNode lhsLhs = lhs.getJmmChild(0);
        if (!lhsLhs.getKind().equals("_Identifier") && !lhsLhs.getKind().equals("IntegerLiteral")) return false;
        JmmNode lhsRhs = lhs.getJmmChild(1);
        if (!lhsRhs.getKind().equals("_Identifier") && !lhsRhs.getKind().equals("IntegerLiteral")) return false;
        JmmNode rhsLhs = rhs.getJmmChild(0);
        if (!rhsLhs.getKind().equals("_Identifier") && !rhsLhs.getKind().equals("IntegerLiteral")) return false;
        JmmNode rhsRhs = rhs.getJmmChild(1);
        if (!rhsRhs.getKind().equals("_Identifier") && !rhsRhs.getKind().equals("IntegerLiteral")) return false;
        String lhsLhsId = lhsLhs.getKind().equals("_Identifier") ? lhsLhs.get("id") : lhsLhs.get("value");
        String lhsRhsId = lhsRhs.getKind().equals("_Identifier") ? lhsRhs.get("id") : lhsRhs.get("value");
        String rhsLhsId = rhsLhs.getKind().equals("_Identifier") ? rhsLhs.get("id") : rhsLhs.get("value");
        String rhsRhsId = rhsRhs.getKind().equals("_Identifier") ? rhsRhs.get("id") : rhsRhs.get("value");
        boolean sameVariableOrder = lhsLhsId.equals(rhsLhsId) && lhsRhsId.equals(rhsRhsId);
        boolean switchedVariableOrder = lhsLhsId.equals(rhsRhsId) && lhsRhsId.equals(rhsLhsId);
        boolean sameOperation = lhsKind.equals(rhsKind);

        if (sameVariableOrder) {
            if (sameOperation) node.replace(lhs);
            else simplify(node, lhsKind, rhsKind, lhsLhs, lhsRhs, BooleanRulesMap.normalOrTrue, BooleanRulesMap.normalOrReduce, false);
        } else if (switchedVariableOrder) {
            if (sameOperation) {
                boolean equal = lhsKind.equals("LessEqualExpr") || lhsKind.equals("GreaterEqualExpr");
                boolean isTrue = lhsKind.equals("LessExpr") || lhsKind.equals("GreaterExpr");
                if (equal) replaceWithKindCopy2Children(node, "NotEqualExpr", lhsLhs, lhsRhs);
                else if (isTrue) replaceWithTrue(node);
                else return false;
            } else {
                simplify(node, lhsKind, rhsKind, lhsLhs, lhsRhs, BooleanRulesMap.switchedOrTrue, BooleanRulesMap.switchedOrReduce, true);
            }
        } else {
            return false;
        }
        return true;
    }

    private void simplify(JmmNode node, String lhsKind, String rhsKind, JmmNode lhsLhs, JmmNode lhsRhs, Set<List<String>> falseMap, Map<List<String>, String> reduceMap, boolean switched) {
        List<String> kinds = List.of(lhsKind, rhsKind);
        List<String> reversedKinds = List.of(rhsKind, lhsKind);
        if (falseMap.contains(kinds) || falseMap.contains(reversedKinds)) {
            replaceWithFalse(node);
        } else if (reduceMap.containsKey(kinds)) {
            String newKind = reduceMap.get(kinds);
            replaceWithKindCopy2Children(node, newKind, lhsLhs, lhsRhs);
        } else if (reduceMap.containsKey(reversedKinds)) {
            String newKind = reduceMap.get(reversedKinds);
            if (switched) replaceWithKindCopy2Children(node, newKind, lhsRhs, lhsLhs);
            else replaceWithKindCopy2Children(node, newKind, lhsLhs, lhsRhs);
        }
    }

    private void replaceWithKindCopy2Children(JmmNode node, String newKind, JmmNode lhs, JmmNode rhs) {
        JmmNode newNode = new JmmNodeImpl(newKind);
        newNode.add(lhs);
        newNode.add(rhs);
        node.replace(newNode);
    }

    private void replaceWithFalse(JmmNode node) {
        JmmNode newNode = new JmmNodeImpl("BooleanLiteral");
        newNode.put("value", "false");
        node.replace(newNode);
    }

    private void replaceWithTrue(JmmNode node) {
        JmmNode newNode = new JmmNodeImpl("BooleanLiteral");
        newNode.put("value", "true");
        node.replace(newNode);
    }
}