package pt.up.fe.comp.optimizations;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PostorderJmmVisitor;

import java.util.List;

public class PostorderVisitorProhibited<D, R> extends PostorderJmmVisitor<D, R> {
    private final List<String> prohibitedNodes;

    public PostorderVisitorProhibited(List<String> prohibitedNodes) {
        this.prohibitedNodes = prohibitedNodes;
    }

    @Override
    public R visit(JmmNode jmmNode, D data) {
        if (prohibitedNodes.contains(jmmNode.getKind())) {
            var visit = getVisit(jmmNode.getKind());
            visit.apply(jmmNode, data);
            return (R) Boolean.FALSE;
        }
        return super.visit(jmmNode, data);
    }
}
