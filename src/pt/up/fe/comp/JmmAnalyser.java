package pt.up.fe.comp;

import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.parser.JmmParserResult;

import java.util.Collections;
import java.util.List;

public class JmmAnalyser implements JmmAnalysis {
    @Override
    public JmmSemanticsResult semanticAnalysis(JmmParserResult parserResult) {
        MySymbolTable symbolTable = new MySymbolTable();

        VisitorEval eval = new VisitorEval(symbolTable);
        JmmNode root = parserResult.getRootNode();

        System.out.println("visitor eval: " + eval.visit(root, null));
        // visitor code

        System.out.println("Imports: " + symbolTable.getImports());
        System.out.println("Class Name: " + symbolTable.getClassName());
        System.out.println("Super: " + symbolTable.getSuper());
        System.out.println("Return type: " + symbolTable.getReturnType("quicksort"));

        return new JmmSemanticsResult(parserResult, symbolTable, Collections.emptyList() /* LIST OF REPORTS */);
    }
}
