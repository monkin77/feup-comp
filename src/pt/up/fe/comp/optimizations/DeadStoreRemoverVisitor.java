package pt.up.fe.comp.optimizations;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.*;

public class DeadStoreRemoverVisitor extends PostorderVisitorProhibited<Object, Boolean> {
    Map<String, Set<JmmNode>> pendingAssignments;
    JmmSemanticsResult result;

    public DeadStoreRemoverVisitor(JmmSemanticsResult result) {
        super(List.of("IfElse", "PublicMethod", "MainDecl"));
        this.result = result;
        this.pendingAssignments = new HashMap<>();

        addVisit("IfElse", this::visitIfElse);
        addVisit("_Identifier", this::visitIdentifier);
        addVisit("PublicMethod", this::visitPublicMethod);
        addVisit("MainDecl", this::visitPublicMethod);

        setReduceSimple(Boolean::logicalOr);
        setReduce((b, l) -> l.stream().reduce(b, Boolean::logicalOr));
        setDefaultValue(() -> false);
    }

    private Boolean visitPublicMethod(JmmNode jmmNode, Object o) {
        this.pendingAssignments.clear();
        visitAllChildren(jmmNode, o);
        this.pendingAssignments.forEach((k, v) -> v.forEach(this::dealWithOldAssignment));
        return this.pendingAssignments.isEmpty();
    }

    private Boolean visitIdentifier(JmmNode node, Object o) {
        String id = node.get("id");
        if (node.getJmmParent().getKind().equals("AssignmentExpr") && node.getIndexOfSelf() == 0) {
            if (pendingAssignments.containsKey(id)) {
                Set<JmmNode> assignments = pendingAssignments.get(id);
                boolean result = false;
                for (JmmNode assignment : assignments) {
                    boolean sideEffectFree = dealWithOldAssignment(assignment);
                    result |= sideEffectFree;
                }
                pendingAssignments.remove(id);
                Set<JmmNode> set = new HashSet<>();
                set.add(node.getJmmParent());
                pendingAssignments.put(id, set);
                return result;
            } else {
                Set<JmmNode> set = new HashSet<>();
                set.add(node.getJmmParent());
                pendingAssignments.put(id, set);
            }
        } else {
            pendingAssignments.remove(id);
        }
        return false;
    }

    private boolean dealWithOldAssignment(JmmNode assignment) {
        JmmNode parent = assignment.getJmmParent();
        JmmNode value = assignment.getJmmChild(1);
        List<JmmNode> sideEffects = sideEffects(value);
        int n = assignment.getIndexOfSelf();
        for (JmmNode sideEffect : sideEffects) parent.add(sideEffect, n++);
        assignment.delete();
        return !sideEffects.isEmpty();
    }

    private boolean isSideEffectFree(JmmNode value) {
        String kind = value.getKind();
        if (kind.equals("IntegerLiteral") || kind.equals("BooleanLiteral")) return true;
        if (kind.equals("_Identifier")) return true;
        if (kind.equals("DotExpression")) return value.getJmmChild(0).getKind().equals("DotLength");
        if (kind.equals("AddExpr") || kind.equals("SubExpr") || kind.equals("MultExpr") || kind.equals("DivExpr"))
            return true;
        if (kind.equals("NotExpr") || kind.equals("LessExpr") || kind.equals("LessEqualExpr") || kind.equals("GreaterExpr") ||
            kind.equals("GreaterEqualExpr") || kind.equals("EqualExpr") || kind.equals("NotEqualExpr"))
            return true;
        return value.getChildren().stream().allMatch(this::isSideEffectFree);
    }

    private List<JmmNode> sideEffects(JmmNode value) {
        List<JmmNode> sideEffects = new ArrayList<>();
        for (JmmNode child : value.getChildren()) {
            if (!isSideEffectFree(child)) sideEffects.add(child);
            sideEffects.addAll(sideEffects(child));
        }
        return sideEffects;
    }

    private Boolean visitIfElse(JmmNode node, Object o) {
        JmmNode condition = node.getJmmChild(0);
        visit(condition, o);
        Map<String, Set<JmmNode>> oldPendingAssignments = new HashMap<>(pendingAssignments);

        pendingAssignments.clear(); // TODO: Review
        JmmNode ifScopeBlock = node.getJmmChild(1).getJmmChild(0);
        visit(ifScopeBlock, o);
        Map<String, Set<JmmNode>> ifScopeAssignments = new HashMap<>(this.pendingAssignments);

        this.pendingAssignments.clear(); // TODO: Review
        JmmNode elseScopeBlock = node.getJmmChild(2).getJmmChild(0);
        visit(elseScopeBlock, o);
        Map<String, Set<JmmNode>> elseScopeAssignments = new HashMap<>(this.pendingAssignments);

        Set<String> overrides = new HashSet<>();
        overrides.addAll(ifScopeAssignments.keySet());
        overrides.retainAll(elseScopeAssignments.keySet());

        for (String override : overrides) {
            if (oldPendingAssignments.containsKey(override)) {
                for (JmmNode assignment : oldPendingAssignments.get(override)) {
                    dealWithOldAssignment(assignment);
                }
                oldPendingAssignments.remove(override);
            }
            Set<JmmNode> ifScopeAssignment = ifScopeAssignments.get(override);
            Set<JmmNode> elseScopeAssignment = elseScopeAssignments.get(override);
            Set<JmmNode> newAssignments = new HashSet<>();
            newAssignments.addAll(ifScopeAssignment);
            newAssignments.addAll(elseScopeAssignment);
            oldPendingAssignments.put(override, newAssignments);
        }

        this.pendingAssignments = oldPendingAssignments;
        return !overrides.isEmpty();
    }
}
