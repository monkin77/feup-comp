package pt.up.fe.comp;

import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.visitors.ExistenceVisitor;
import pt.up.fe.comp.visitors.VisitorEval;

import java.util.Collections;

public class JmmAnalyser implements JmmAnalysis {
    @Override
    public JmmSemanticsResult semanticAnalysis(JmmParserResult parserResult) {
        MySymbolTable symbolTable = new MySymbolTable();

        VisitorEval eval = new VisitorEval(symbolTable);
        JmmNode root = parserResult.getRootNode();

        System.out.println("visitor eval: " + eval.visit(root, null));
        // visitor code

        /*
        System.out.println("Imports: " + symbolTable.getImports());
        System.out.println("Class Name: " + symbolTable.getClassName());
        System.out.println("Super: " + symbolTable.getSuper());
        System.out.println("Fields: " + symbolTable.getFields());
        System.out.println("Methods: " + symbolTable.getMethods());
        System.out.println("Return type: " + symbolTable.getReturnType("init"));
        System.out.println("Method Parameters: " + symbolTable.getParameters("trIdx"));
        System.out.println("Method Local Variables: " + symbolTable.getLocalVariables("main"));
        */

        ExistenceVisitor analyser = new ExistenceVisitor(symbolTable);
        System.out.println("visitor analyser: " + analyser.visit(root, null));

        return new JmmSemanticsResult(parserResult, symbolTable, Collections.emptyList() /* LIST OF REPORTS */);
    }
}
