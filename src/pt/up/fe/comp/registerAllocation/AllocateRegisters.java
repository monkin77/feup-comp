package pt.up.fe.comp.registerAllocation;

import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.Method;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.registerAllocation.coloring.GraphColoring;
import pt.up.fe.comp.registerAllocation.coloring.InterferenceGraph;
import pt.up.fe.comp.registerAllocation.dataflow.DataflowAnalysis;

import java.util.ArrayList;
import java.util.HashMap;

public class AllocateRegisters {
    private OllirResult ollirResult;
    private ClassUnit classUnit;
    final private int maxRegisters;

    public AllocateRegisters(OllirResult ollirResult, int maxRegisters) {
        this.ollirResult = ollirResult;
        this.classUnit = ollirResult.getOllirClass();
        this.maxRegisters = maxRegisters;
    }

    /**
     * This method will build the var table for all the methods.
     */
    public void buildVarTable() {
        for (Method method: this.classUnit.getMethods()) {
            this.allocateMethodRegisters(method);
        }
    }

    private boolean allocateMethodRegisters(Method method) {
        DataflowAnalysis dataflowAnalysis = new DataflowAnalysis(method);
        dataflowAnalysis.build();

        HashMap<String, ArrayList<String>> analysisInterference = dataflowAnalysis.getInterference();
        InterferenceGraph interferenceGraph = new InterferenceGraph(analysisInterference);

        GraphColoring graphColoring = new GraphColoring(this.maxRegisters, interferenceGraph);
        if (!graphColoring.buildStack()) // TODO: CHECK IF WE SHOULD ADD A REPORT
            throw new RuntimeException("Not possible to execute the program with the number of register provided.");
        if (!graphColoring.coloring())
            throw new RuntimeException("Unable to color the graph.");

        var varTable = method.getVarTable();
        for (var node : interferenceGraph.getNodeList()) {
            // Adds the offset for the first registers corresponding to the parameters
            varTable.get(node.getValue()).setVirtualReg(node.getRegister() + method.getParams().size());
        }

        return true;
    }
}
