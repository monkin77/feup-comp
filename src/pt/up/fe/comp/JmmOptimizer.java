package pt.up.fe.comp;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ollir.JmmOptimization;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.ollir.OllirBuilder;
import pt.up.fe.comp.optimizations.*;


public class JmmOptimizer implements JmmOptimization {
    @Override
    public JmmSemanticsResult optimize(JmmSemanticsResult semanticsResult) {
        JmmNode rootNode = semanticsResult.getRootNode();
        ConstantPropagatorVisitor constantPropagatorVisitor = new ConstantPropagatorVisitor(semanticsResult);
        ConstantFolderSimplifierVisitor constantFolderSimplifierVisitor = new ConstantFolderSimplifierVisitor();
        ConstantFolderVisitor constantFolderVisitor = new ConstantFolderVisitor();
        DeadConditionalLoopsVisitor deadConditionalLoopsVisitor = new DeadConditionalLoopsVisitor();
        boolean change;
        if (semanticsResult.getConfig().getOrDefault("optimize", "false").equals("true")) {
            do {
                change = false;
                change |= constantPropagatorVisitor.visit(rootNode);
                change |= constantFolderSimplifierVisitor.visit(rootNode);
                change |= constantFolderVisitor.visit(rootNode);
                change |= deadConditionalLoopsVisitor.visit(rootNode);
            } while (change);
        }
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
