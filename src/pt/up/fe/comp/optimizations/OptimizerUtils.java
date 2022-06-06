package pt.up.fe.comp.optimizations;

import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.HashSet;
import java.util.Set;

public class OptimizerUtils {
    public static Set<String> getAssignTargets(JmmNode block) {
        Set<String> blockAssignTargets = new HashSet<>();
        for (JmmNode child : block.getChildren()) {
            if (child.getKind().equals("AssignmentExpr")) {
                JmmNode target = child.getJmmChild(0);
                blockAssignTargets.add(target.get("id"));
            }
            blockAssignTargets.addAll(getAssignTargets(child));
        }
        return blockAssignTargets;
    }

    public static void replaceWithPosition(JmmNode node, JmmNode newNode) {
        newNode.put("col", node.get("col"));
        newNode.put("line", node.get("line"));
        node.replace(newNode);
    }
}