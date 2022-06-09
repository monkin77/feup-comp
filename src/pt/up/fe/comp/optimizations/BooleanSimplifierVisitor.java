package pt.up.fe.comp.optimizations;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.JmmNodeImpl;
import pt.up.fe.comp.jmm.ast.PostorderJmmVisitor;

import java.util.*;

public class BooleanSimplifierVisitor extends PostorderJmmVisitor<Object, Boolean> {
    private final Map<List<String>, String> normalAndReduce;
    private final Map<List<String>, String> normalOrReduce;
    private final Set<List<String>> normalOrTrue;
    private final Set<List<String>> normalAndFalse;
    private final Map<List<String>, String> switchedAndReduce;
    private final Map<List<String>, String> switchedOrReduce;
    private final Set<List<String>> switchedAndFalse;
    private final Set<List<String>> switchedOrTrue;

    Map<String, String> notReduce = new HashMap<>();
    Map<String, String> notDeMorgan = new HashMap<>();


    public BooleanSimplifierVisitor() {
        setReduceSimple(Boolean::logicalOr);
        setReduce((b, l) -> l.stream().reduce(b, Boolean::logicalOr));
        setDefaultValue(() -> false);

        normalAndReduce = new HashMap<>();
        normalAndReduce.put(List.of("LessExpr", "LessEqualExpr"), "LessExpr");
        normalAndReduce.put(List.of("LessExpr", "NotEqualExpr"), "LessExpr");
        normalAndReduce.put(List.of("GreaterExpr", "GreaterEqualExpr"), "GreaterExpr");
        normalAndReduce.put(List.of("GreaterExpr", "NotEqualExpr"), "GreaterExpr");
        normalAndReduce.put(List.of("EqualExpr", "GreaterEqualExpr"), "EqualExpr");
        normalAndReduce.put(List.of("EqualExpr", "LessEqualExpr"), "EqualExpr");

        normalAndReduce.put(List.of("LessEqualExpr", "GreaterEqualExpr"), "EqualExpr");
        normalAndReduce.put(List.of("GreaterEqualExpr", "NotEqualExpr"), "GreaterExpr");
        normalAndReduce.put(List.of("LessEqualExpr", "NotEqualExpr"), "LessExpr");

        switchedAndReduce = new HashMap<>();
        switchedAndReduce.put(List.of("LessExpr", "GreaterExpr"), "LessExpr");
        switchedAndReduce.put(List.of("LessExpr", "GreaterEqualExpr"), "LessExpr");
        switchedAndReduce.put(List.of("LessExpr", "NotEqualExpr"), "LessExpr");
        switchedAndReduce.put(List.of("GreaterExpr", "LessEqualExpr"), "GreaterExpr");
        switchedAndReduce.put(List.of("GreaterExpr", "NotEqualExpr"), "GreaterExpr");
        switchedAndReduce.put(List.of("GreaterEqualExpr", "LessEqualExpr"), "GreaterEqualExpr");
        switchedAndReduce.put(List.of("EqualExpr", "GreaterEqualExpr"), "EqualExpr");
        switchedAndReduce.put(List.of("EqualExpr", "LessEqualExpr"), "EqualExpr");
        switchedAndReduce.put(List.of("GreaterEqualExpr", "NotEqualExpr"), "GreaterExpr");
        switchedAndReduce.put(List.of("LessEqualExpr", "NotEqualExpr"), "LessExpr");

        normalAndFalse = new HashSet<>();
        normalAndFalse.add(List.of("LessExpr", "GreaterExpr"));
        normalAndFalse.add(List.of("LessExpr", "GreaterEqualExpr"));
        normalAndFalse.add(List.of("LessExpr", "EqualExpr"));
        normalAndFalse.add(List.of("GreaterExpr", "LessExpr"));
        normalAndFalse.add(List.of("GreaterExpr", "LessEqualExpr"));
        normalAndFalse.add(List.of("GreaterExpr", "EqualExpr"));
        normalAndFalse.add(List.of("EqualExpr", "NotEqualExpr"));

        switchedAndFalse = new HashSet<>();
        switchedAndFalse.add(List.of("LessExpr", "LessEqualExpr"));
        switchedAndFalse.add(List.of("LessExpr", "EqualExpr"));
        switchedAndFalse.add(List.of("GreaterExpr", "GreaterEqualExpr"));
        switchedAndFalse.add(List.of("GreaterExpr", "EqualExpr"));
        switchedAndFalse.add(List.of("EqualExpr", "NotEqualExpr"));

        normalOrTrue = new HashSet<>();
        normalOrTrue.add(List.of("GreaterExpr", "LessEqualExpr"));
        normalOrTrue.add(List.of("LessExpr", "GreaterEqualExpr"));
        normalOrTrue.add(List.of("LessEqualExpr", "GreaterEqualExpr"));
        normalOrTrue.add(List.of("GreaterEqualExpr", "NotEqualExpr"));
        normalOrTrue.add(List.of("LessEqualExpr", "NotEqualExpr"));
        normalOrTrue.add(List.of("EqualExpr", "NotEqualExpr"));

        switchedOrTrue = new HashSet<>();
        switchedOrTrue.add(List.of("LessExpr", "LessEqualExpr"));
        switchedOrTrue.add(List.of("GreaterExpr", "GreaterEqualExpr"));
        switchedOrTrue.add(List.of("GreaterEqualExpr", "GreaterExpr"));
        switchedOrTrue.add(List.of("GreaterEqualExpr", "NotEqualExpr"));
        switchedOrTrue.add(List.of("LessEqualExpr", "LessExpr"));
        switchedOrTrue.add(List.of("LessEqualExpr", "NotEqualExpr"));
        switchedOrTrue.add(List.of("EqualExpr", "NotEqualExpr"));
        switchedOrTrue.add(List.of("NotEqualExpr", "GreaterEqualExpr"));
        switchedOrTrue.add(List.of("NotEqualExpr", "LessEqualExpr"));
        switchedOrTrue.add(List.of("NotEqualExpr", "EqualExpr"));

        normalOrReduce = new HashMap<>();
        normalOrReduce.put(List.of("GreaterEqualExpr", "GreaterExpr"), "GreaterEqualExpr");
        normalOrReduce.put(List.of("GreaterEqualExpr", "EqualExpr"), "GreaterEqualExpr");
        normalOrReduce.put(List.of("LessEqualExpr", "LessExpr"), "LessEqualExpr");
        normalOrReduce.put(List.of("LessEqualExpr", "EqualExpr"), "LessEqualExpr");
        normalOrReduce.put(List.of("NotEqualExpr", "LessExpr"), "NotEqualExpr");
        normalOrReduce.put(List.of("NotEqualExpr", "GreaterExpr"), "NotEqualExpr");
        normalOrReduce.put(List.of("GreaterExpr", "LessExpr"), "NotEqualExpr");
        normalOrReduce.put(List.of("LessExpr", "EqualExpr"), "LessEqualExpr");
        normalOrReduce.put(List.of("GreaterExpr", "EqualExpr"), "GreaterEqualExpr");
        normalOrReduce.put(List.of("LessExpr ", "EqualExpr"), "LessEqualExpr");

        switchedOrReduce = new HashMap<>();
        switchedOrReduce.put(List.of("LessExpr", "GreaterExpr"), "LessExpr");
        switchedOrReduce.put(List.of("GreaterEqualExpr", "LessExpr"), "GreaterEqualExpr");
        switchedOrReduce.put(List.of("GreaterEqualExpr", "LessEqualExpr"), "GreaterEqualExpr");
        switchedOrReduce.put(List.of("GreaterEqualExpr", "EqualExpr"), "GreaterEqualExpr");
        switchedOrReduce.put(List.of("LessEqualExpr", "GreaterExpr"), "LessEqualExpr");
        switchedOrReduce.put(List.of("LessEqualExpr", "EqualExpr"), "LessEqualExpr");
        switchedOrReduce.put(List.of("NotEqualExpr", "LessExpr"), "NotEqualExpr");
        switchedOrReduce.put(List.of("NotEqualExpr", "GreaterExpr"), "NotEqualExpr");
        switchedOrReduce.put(List.of("LessExpr", "EqualExpr"), "LessEqualExpr");
        switchedOrReduce.put(List.of("GreaterExpr", "EqualExpr"), "GreaterEqualExpr");

        notReduce.put("LessExpr", "GreaterEqualExpr");
        notReduce.put("GreaterExpr", "LessEqualExpr");
        notReduce.put("EqualExpr", "NotEqualExpr");
        notReduce.put("GreaterEqualExpr", "LessExpr");
        notReduce.put("LessEqualExpr", "GreaterExpr");
        notReduce.put("NotEqualExpr", "EqualExpr");

        notDeMorgan.put("AndExpr", "OrExpr");
        notDeMorgan.put("OrExpr", "AndExpr");

        addVisit("AndExpr", this::andExprVisit);
        addVisit("OrExpr", this::orExprVisit);
        addVisit("NotExpr", this::notExprVisit);
    }

    private Boolean notExprVisit(JmmNode node, Object o) {
        JmmNode child = node.getJmmChild(0);
        String childKind = child.getKind();
        if (childKind.equals("NotExpr")) {
            JmmNode grandChild = child.getJmmChild(0);
            node.replace(grandChild);
            return true;
        } else if (child.getNumChildren() == 2) {
            if (notReduce.containsKey(childKind)) {
                String newKind = notReduce.get(childKind);
                JmmNode lhs = child.getJmmChild(0);
                JmmNode rhs = child.getJmmChild(1);
                replaceWithKindCopy2Children(node, newKind, lhs, rhs);
                return true;
            } else if (notDeMorgan.containsKey(childKind)) {
                JmmNode lhs = child.getJmmChild(0);
                if (!lhs.getKind().equals("NotExpr")) return false;
                JmmNode rhs = child.getJmmChild(1);
                if (!rhs.getKind().equals("NotExpr")) return false;
                JmmNode lhsChild = lhs.getJmmChild(0);
                JmmNode rhsChild = rhs.getJmmChild(0);
                String newKind = notDeMorgan.get(childKind);
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
            } else simplify(node, lhsKind, rhsKind, lhsLhs, lhsRhs, normalAndFalse, normalAndReduce, false);
        } else if (switchedVariableOrder) {
            if (sameOperation) {
                boolean equal = lhsKind.equals("LessEqualExpr") || lhsKind.equals("GreaterEqualExpr");
                boolean isFalse = lhsKind.equals("LessExpr") || lhsKind.equals("GreaterExpr");
                if (equal) replaceWithKindCopy2Children(node, "EqualExpr", lhsLhs, lhsRhs);
                else if (isFalse) replaceWithFalse(node);
                else if (lhsKind.equals("EqualExpr") || lhsKind.equals("NotEqualExpr")) node.replace(lhs);
                else return false;
            } else {
                simplify(node, lhsKind, rhsKind, lhsLhs, lhsRhs, switchedAndFalse, switchedAndReduce, true);
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
            else simplify(node, lhsKind, rhsKind, lhsLhs, lhsRhs, normalOrTrue, normalOrReduce, false);
        } else if (switchedVariableOrder) {
            if (sameOperation) {
                boolean equal = lhsKind.equals("LessEqualExpr") || lhsKind.equals("GreaterEqualExpr");
                boolean isTrue = lhsKind.equals("LessExpr") || lhsKind.equals("GreaterExpr");
                if (equal) replaceWithKindCopy2Children(node, "NotEqualExpr", lhsLhs, lhsRhs);
                else if (isTrue) replaceWithTrue(node);
                else return false;
            } else {
                simplify(node, lhsKind, rhsKind, lhsLhs, lhsRhs, switchedOrTrue, switchedOrReduce, true);
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