package pt.up.fe.comp;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ollir.JmmOptimization;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.ollir.OllirBuilder;
import pt.up.fe.comp.optimizations.ConstantFolderVisitor;
import pt.up.fe.comp.optimizations.ConstantPropagatorVisitor;


public class JmmOptimizer implements JmmOptimization {
    @Override
    public JmmSemanticsResult optimize(JmmSemanticsResult semanticsResult) {
        ConstantPropagatorVisitor constantPropagatorVisitor = new ConstantPropagatorVisitor(semanticsResult);
        ConstantFolderVisitor constantFolderVisitor = new ConstantFolderVisitor();
        JmmNode rootNode = semanticsResult.getRootNode();
        boolean change;
        do {
            change = false;
            change |= constantPropagatorVisitor.visit(rootNode);
            change |= constantFolderVisitor.visit(rootNode);
        } while (change);
        return semanticsResult;
    }

    @Override
    public OllirResult toOllir(JmmSemanticsResult semanticsResult) {
        String code = new OllirBuilder(semanticsResult).compile();
        System.out.println("Ollir code:");
        System.out.println(code);
        return new OllirResult(semanticsResult, code, semanticsResult.getReports());
    }

    @Override
    public OllirResult optimize(OllirResult ollirResult) {
        return ollirResult;
    }
}
