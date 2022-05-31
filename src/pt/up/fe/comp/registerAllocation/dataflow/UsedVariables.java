package pt.up.fe.comp.registerAllocation.dataflow;

import org.specs.comp.ollir.AssignInstruction;
import org.specs.comp.ollir.Instruction;

public class UsedVariables {
    private final Instruction instruction;

    public UsedVariables(Instruction instruction) {
        this.instruction = instruction;
    }

    public String[] getUsed() {
        switch (this.instruction.getInstType()) {
            case ASSIGN -> {
                return getAssign((AssignInstruction) instruction);
            }
        }

        return new String[]{};
    }

    private String[] getAssign(AssignInstruction instruction) {

    }
}
