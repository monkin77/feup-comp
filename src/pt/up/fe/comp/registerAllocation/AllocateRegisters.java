package pt.up.fe.comp.registerAllocation;

import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.Method;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp.registerAllocation.coloring.GraphColoring;
import pt.up.fe.comp.registerAllocation.coloring.InterferenceGraph;
import pt.up.fe.comp.registerAllocation.dataflow.DataflowAnalysis;

import java.util.ArrayList;
import java.util.HashMap;

public class AllocateRegisters {
    private final OllirResult ollirResult;
    private final ClassUnit classUnit;
    private final int maxRegisters;

    public AllocateRegisters(OllirResult ollirResult, int maxRegisters) {
        this.ollirResult = ollirResult;
        this.classUnit = ollirResult.getOllirClass();
        if (maxRegisters == -1) {
            this.maxRegisters = this.getMaxLocalVar();
        } else if(maxRegisters == 0) {
            this.maxRegisters = this.getMinRegisters();
        } else {
            this.maxRegisters = maxRegisters;
        }
    }

    /**
     * Gets the maximum of local variables from all methods
     */
    private int getMaxLocalVar() {
        int max = 0;
        for (Method method : this.classUnit.getMethods()) {
            // var table has both local variables and params so remove the params
            int size = method.getVarTable().size() - method.getParams().size();
            if (size > max)
                max = size;
        }

        return max;
    }

    /**
     * Iterates all the methods and returns the minimum number of registers needed
     * @return number of registers
     */
    private int getMinRegisters() {
        int minRegisters = 0;
        for (Method method: this.classUnit.getMethods()) {
            DataflowAnalysis dataflowAnalysis = new DataflowAnalysis(method);
            dataflowAnalysis.build();

            HashMap<String, ArrayList<String>> analysisInterference = dataflowAnalysis.getInterference();
            InterferenceGraph interferenceGraph = new InterferenceGraph(analysisInterference);

            GraphColoring graphColoring = new GraphColoring(this.maxRegisters, interferenceGraph);
            int currRegisters = graphColoring.getMinLocalVar();
            minRegisters = Math.max(minRegisters, currRegisters);
        }

        System.out.println("Minimum amount of registers required: " + minRegisters);
        return minRegisters;
    }

    /**
     * This method will update the var table for all the methods.
     */
    public void updateVarTable() {
        for (Method method: this.classUnit.getMethods()) {
            if (!this.allocateMethodRegisters(method)) return;
        }
    }

    private boolean allocateMethodRegisters(Method method) {
        DataflowAnalysis dataflowAnalysis = new DataflowAnalysis(method);
        dataflowAnalysis.build();

        /*
        System.out.println("Showing DataFlow Analysis for method " + method.getMethodName() + ":");
        dataflowAnalysis.show();
        dataflowAnalysis.showLiveRange();
        dataflowAnalysis.showInterference();
        System.out.println("========================================\n");
         */

        HashMap<String, ArrayList<String>> analysisInterference = dataflowAnalysis.getInterference();
        InterferenceGraph interferenceGraph = new InterferenceGraph(analysisInterference);

        int staticOffset = method.isStaticMethod() ? 0 : 1;
        int numParams = method.getParams().size();
        GraphColoring graphColoring = new GraphColoring(this.maxRegisters - numParams - staticOffset, interferenceGraph);

        if (!graphColoring.buildStack()) {
            int minRegisters = graphColoring.getMinLocalVar() + numParams + staticOffset;
            this.ollirResult.getReports().add(Report.newError(Stage.OPTIMIZATION, -1, -1,
                    "Unable to build the graph stack with the number of registers provided. Minimum Registers: " + minRegisters,
                    null));
            return false;
        }

        if (!graphColoring.coloring()) {
            int minRegisters = graphColoring.getMinLocalVar() + numParams + staticOffset;
            this.ollirResult.getReports().add(Report.newError(Stage.OPTIMIZATION, -1, -1,
                    "Unable to color the graph with the number of registers provided. Minimum Registers: " + minRegisters,
                    null));
            return false;
        }

        System.out.printf("Local variables used in method %s:\n", method.getMethodName());
        var varTable = method.getVarTable();
        for (var node : interferenceGraph.getNodeList()) {
            // Adds the offset for the first registers corresponding to the parameters
            varTable.get(node.getValue()).setVirtualReg(node.getRegister() + method.getParams().size() + staticOffset);
            System.out.printf("Reg %d <- %s\n", node.getRegister(), node.getValue());
        }

        /*
        System.out.println("\nSHOWING REGISTERS");
        for (String node : varTable.keySet()) {
            System.out.println("NODE : " + node + " Reg: " + varTable.get(node).getVirtualReg());
        }
        */

        System.out.println();
        return true;
    }
}
