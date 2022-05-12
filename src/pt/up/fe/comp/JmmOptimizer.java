package pt.up.fe.comp;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ollir.JmmOptimization;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.ollir.OllirBuilder;

public class JmmOptimizer implements JmmOptimization {
    @Override
    public JmmSemanticsResult optimize(JmmSemanticsResult semanticsResult) {
        return null;
    }

    @Override
    public OllirResult toOllir(JmmSemanticsResult semanticsResult) {
        String code = new OllirBuilder(semanticsResult).compile();
        System.out.println("OLLIR:");
        System.out.println(code);
        // TODO Return OllirResult
        // return new OllirResult(semanticsResult, code, semanticsResult.getReports());
        return null;
    }

    @Override
    public OllirResult optimize(OllirResult ollirResult) {
        return null;
    }
}
