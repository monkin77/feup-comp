package pt.up.fe.comp.optimizations;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PostorderJmmVisitor;

public class DeadConditionalLoopsVisitor extends PostorderJmmVisitor<Object, Boolean> {
    public DeadConditionalLoopsVisitor() {
        addVisit("IfElse", this::visitIfElse);
        addVisit("WhileSt", this::visitWhileSt);

        setReduceSimple(Boolean::logicalOr);
        setReduce((b, l) -> l.stream().reduce(b, Boolean::logicalOr));
        setDefaultValue(() -> false);
    }

    private Boolean visitWhileSt(JmmNode node, Object o) {
        JmmNode condition = node.getJmmChild(0).getJmmChild(0);

        if (condition.getKind().equals("BooleanLiteral")) {
            boolean value = Boolean.parseBoolean(condition.get("value"));
            if (!value) {
                node.delete();
                return true;
            }
        }
        return false;
    }

    private Boolean visitIfElse(JmmNode node, Object o) {
        JmmNode condition = node.getJmmChild(0).getJmmChild(0);
        JmmNode ifScopeBlock = node.getJmmChild(1).getJmmChild(0);
        JmmNode elseScopeBlock = node.getJmmChild(2).getJmmChild(0);

        if (condition.getKind().equals("BooleanLiteral")) {
            boolean value = Boolean.parseBoolean(condition.get("value"));
            if (value) node.replace(ifScopeBlock);
            else node.replace(elseScopeBlock);
            return true;
        }

        return false;
    }
}
