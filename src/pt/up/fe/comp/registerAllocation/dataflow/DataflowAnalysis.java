package pt.up.fe.comp.registerAllocation.dataflow;

import org.specs.comp.ollir.*;

import java.util.*;
import java.util.stream.IntStream;

public class DataflowAnalysis {
    private final Method method;
    private final String[][] use;
    private final String[][] def;
    private final Integer[][] succ;

    private final String[][] in;
    private final String [][] out;

    private final HashMap<String, int[]> liveRange;
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
        this.livenessAnalysis();
        this.calculateLiveRange();
        this.calculateInterference();
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

    /**
     * Calculates the live range for each defined variable
     */
    private void calculateLiveRange() {
        for (String varName : this.variables) {
            int[] varLiveRange = new int[]{};
            Integer lastIn = this.getLastIn(varName);
            Integer firstDef = this.getFirstDef(varName);
            if (lastIn == null && firstDef != null) {
                // If the last usage of the variable cannot be determined, assume it is alive until the end
                varLiveRange = IntStream.range(firstDef, this.in.length).toArray();
            } else if (lastIn != null && firstDef != null) {
                varLiveRange = IntStream.range(firstDef, lastIn).toArray();
            }

            this.liveRange.put(varName, varLiveRange);
        }
    }

    /**
     * Get the last instruction that contains the given variable in the In Array.
     * @param varName variable name
     * @return Instruction index
     */
    private Integer getLastIn(String varName) {
        for (int i = this.in.length - 1; i >= 0; i--) {
            ArrayList<String> inTemp = new ArrayList<>(Arrays.asList(this.in[i]));
            if (inTemp.contains(varName))
                return i;
        }
        return null;
    }

    /**
     * Get the first instruction that contains the given variable in the Def Array
     * @param varName variable name
     * @return Instruction index
     */
    private Integer getFirstDef(String varName) {
        for (int i = 0; i < this.def.length; i++) {
            ArrayList<String> defTemp = new ArrayList<>(Arrays.asList(this.def[i]));
            if (defTemp.contains(varName))
                return i;
        }
        return null;
    }

    /**
     * Calculates the conflict between variables and
     * updates the interference HashMap for each variable
     */
    private void calculateInterference() {
        for (String variable : this.variables) {
            ArrayList<String> temp = new ArrayList<>();
            if (!this.liveRange.containsKey(variable)) {
                interference.put(variable, temp);
                continue;
            }
            for (String variableCompare : this.liveRange.keySet()) {
                if (variableCompare.equals(variable))
                    continue;
                if (this.hasConflict(this.liveRange.get(variable), this.liveRange.get(variableCompare))) {
                    temp.add(variableCompare);
                }
            }
            this.interference.put(variable, temp);
        }
    }

    /**
     * Receives 2 variables live ranges and checks if they conflict
     * @return true if conflicts. False otherwise
     */
    private boolean hasConflict(int[] arr1, int[] arr2) {
        return Arrays.stream(arr1)
                .distinct()
                .filter(x -> Arrays.stream(arr2).anyMatch(y -> y == x))
                .toArray().length > 0;
    }
}
