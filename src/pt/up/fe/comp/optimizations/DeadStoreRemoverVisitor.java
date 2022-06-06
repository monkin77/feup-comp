package pt.up.fe.comp.optimizations;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.*;

public class DeadStoreRemoverVisitor extends PostorderVisitorProhibited<Object, Boolean> {
    Map<String, Set<JmmNode>> pendingAssignments;
    JmmSemanticsResult result;

    public DeadStoreRemoverVisitor(JmmSemanticsResult result) {
        super(List.of("IfElse", "WhileSt"));
        this.result = result;
        this.pendingAssignments = new HashMap<>();

        addVisit("IfElse", this::visitIfElse);
        addVisit("WhileSt", this::visitWhileSt);
        addVisit("_Identifier", this::visitIdentifier);

        setReduceSimple(Boolean::logicalOr);
        setReduce((b, l) -> l.stream().reduce(b, Boolean::logicalOr));
        setDefaultValue(() -> false);
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
        JmmNode value = assignment.getJmmChild(1);
        boolean sideEffectFree = value.getKind().equals("IntegerLiteral") || value.getKind().equals("BooleanLiteral") || value.getKind().equals("_Identifier");
        if (sideEffectFree) {
            assignment.delete();
        } else {
            // TODO: This can't be done yet because sometimes we need the assignment to infer the return type - annotate with type checking visitor
//            assignment.replace(value);
        }
        return sideEffectFree;
    }

    private Boolean visitWhileSt(JmmNode node, Object o) {
        return false;
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
