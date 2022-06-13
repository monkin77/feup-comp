package pt.up.fe.comp;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ollir.JmmOptimization;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.ollir.OllirBuilder;
import pt.up.fe.comp.optimizations.BooleanSimplifierVisitor;
import pt.up.fe.comp.optimizations.*;
import pt.up.fe.comp.optimizations.ConstantFolderVisitor;
import pt.up.fe.comp.optimizations.ConstantPropagatorVisitor;
import pt.up.fe.comp.registerAllocation.AllocateRegisters;


public class JmmOptimizer implements JmmOptimization {
    @Override
    public JmmSemanticsResult optimize(JmmSemanticsResult semanticsResult) {
        JmmNode rootNode = semanticsResult.getRootNode();
        ConstantPropagatorVisitor constantPropagatorVisitor = new ConstantPropagatorVisitor(semanticsResult);
        NeutralAbsorbentOperationsVisitor neutralAbsorbentOperationsVisitor = new NeutralAbsorbentOperationsVisitor();
        ConstantFolderVisitor constantFolderVisitor = new ConstantFolderVisitor();
        DeadConditionalLoopsVisitor deadConditionalLoopsVisitor = new DeadConditionalLoopsVisitor();
        DeadStoreRemoverVisitor deadStoreRemoverVisitor = new DeadStoreRemoverVisitor(semanticsResult);
        BooleanSimplifierVisitor booleanSimplifierVisitor = new BooleanSimplifierVisitor();
        boolean change;
        if (semanticsResult.getConfig().getOrDefault("optimize", "false").equals("true")) {
            do {
                change = booleanSimplifierVisitor.visit(semanticsResult.getRootNode());
                change |= constantPropagatorVisitor.visit(rootNode);
                change |= neutralAbsorbentOperationsVisitor.visit(rootNode);
                change |= constantFolderVisitor.visit(rootNode);
                change |= deadConditionalLoopsVisitor.visit(rootNode);
                change |= deadStoreRemoverVisitor.visit(rootNode);
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
        String numRegisterStr = ollirResult.getConfig().get("registerAllocation");
        if (numRegisterStr == null)
            return ollirResult;

        int numRegisters = Integer.parseInt(numRegisterStr);

        AllocateRegisters allocator = new AllocateRegisters(ollirResult, numRegisters);
        allocator.updateVarTable();

        return ollirResult;
    }
}
