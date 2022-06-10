package pt.up.fe.comp.registerAllocation.dataflow;

import org.specs.comp.ollir.*;

import java.util.*;

public class DataflowAnalysis {
    private final Method method;
    private final String[][] use;
    private final String[][] def;
    private final Integer[][] succ;

    private final String[][] in;
    private final String [][] out;

    private final HashMap<String, Integer[]> liveRange;
    private final HashSet<String> variables;

    private final HashMap<String, ArrayList<String>> interference;

    public DataflowAnalysis(Method method) {
        this.method = method;
        this.method.buildCFG();
        this.use = new String[method.getInstructions().size()][];
        this.def = new String[method.getInstructions().size()][];
        this.succ = new Integer[method.getInstructions().size()][];
        this.variables = new HashSet<>();

        this.in = new String[method.getInstructions().size()][];
        this.out = new String[method.getInstructions().size()][];
        this.liveRange = new HashMap<>();
        this.interference = new HashMap<>();
    }

    public void build() {
        this.prepareDataFlowAnalysis(method.getBeginNode().getSucc1());

    }

    /**
     * Builds the successors, in, out, def and use parameters
     * for the Dataflow Analysis
     */
    private void prepareDataFlowAnalysis(Node node) {
        if (node == null || node.getNodeType() == NodeType.END || succ[node.getId() - 1] != null)
            return;

        this.storeDefined(node);
        this.storeSucc(node);
        this.storeUsed(node);
        this.prepareDataFlowAnalysis(node.getSucc1());
        this.prepareDataFlowAnalysis(node.getSucc2());
    }

    /**
     * Stores the defined variable in a given instruction
     * @param node
     */
    private void storeDefined(Node node) {
        int index = node.getId() - 1;
        if (method.getInstr(index).getInstType() != InstructionType.ASSIGN) {
            // If instruction isn't an assign, it does not define anything
            this.def[index] = new String[]{};
        } else {
            AssignInstruction instr = (AssignInstruction) method.getInstr(index);
            Operand dest = (Operand) instr.getDest();
            if (dest instanceof ArrayOperand) {
                // if the destination of the assignment is an array element, it doesn't define a variable
                this.def[index] = new String[]{};
            } else {
               this.def[index] = new String[]{dest.getName()};
               this.variables.add(dest.getName());
            }
        }
    }

    /**
     * Stores the used variables in a given instruction
     * @param node
     */
    private void storeUsed(Node node) {
        int index = node.getId() - 1;
        Instruction instr = method.getInstr(index);
        String[] usedVariables = new UsedVariables(instr).getUsed();
        Set<String> set = new HashSet<>(Arrays.asList(usedVariables));
        use[index] = set.toArray(new String[0]);
    }

    /**
     * Stores the successor instructions of the given node
     */
    private void storeSucc(Node node) {
        List<Integer> succsNodes = new ArrayList<>();

        for (Node successor : node.getSuccessors())
            succsNodes.add(successor.getId() - 1);

        this.succ[node.getId() - 1] = succsNodes.toArray(new Integer[0]);
    }


    /**
     * Computation of backward Liveness Analysis
     */
    private void livenessAnalysis() {
        String[][] prevOut = null;
        String[][] prevIn = null;

        do {
            prevIn = Utils.deepCopyMatrix(prevIn);
            prevOut = Utils.deepCopyMatrix(prevOut);

            for (int i = this.use.length - 1; i >= 0; i--) {
                // Remove nulls
                if (this.out[i] == null) this.out[i] = new String[]{};
                if (this.in[i] == null) this.in[i] = new String[]{};

                this.out[i] = this.removeParams(this.getOut(i));
                this.in[i] = this.removeParams(this.getIn(i));
            }
        } while(!Utils.compareMatrix(this.out, prevOut) || !Utils.compareMatrix(this.in, prevIn));
    }

    /**
     * Calculates the Out array of a certain instruction in a Backward Liveness Analysis
     * @param index Instruction index
     * @return Out array
     */
    private String[] getOut(int index) {
        HashSet<String> out = new HashSet<>();

        for (int i = 0; i < this.succ[index].length; i++) {
            int instrId = this.succ[index][i];

            if (instrId < 0) continue;
            if (this.in[instrId] == null)
                this.in[instrId] = new String[]{};

            out.addAll(Arrays.asList(this.in[instrId]));
        }
        return out.toArray(new String[0]);
    }

    /**
     * Calculates the In array of a certain instruction in a Backward Liveness Analysis
     * @param index Instruction index
     * @return In array
     */
    private String[] getIn(int index) {
        HashSet<String> in = new HashSet<>(Arrays.asList(this.out[index]));
        in.removeAll(Arrays.asList(this.def[index]));
        in.addAll(Arrays.asList(this.use[index]));
        return in.toArray(new String[0]);
    }

    /**
     * Remove the parameters from the array
     */
    private String[] removeParams(String[] array) {
        ArrayList<Element> parameters = this.method.getParams();
        ArrayList<String> paramsName = new ArrayList<>();

        for (int i = 0; i < parameters.size(); i++)
            paramsName.add(((Operand) parameters.get(i)).getName());

        ArrayList<String> temp = new ArrayList<>(Arrays.asList(array));
        temp.removeAll(paramsName);

        return temp.toArray(new String[0]);
    }
}
