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
}
