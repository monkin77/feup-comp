package pt.up.fe.comp.jasmin.instruction;

import org.specs.comp.ollir.*;
import pt.up.fe.comp.jasmin.AbstractBuilder;
import pt.up.fe.comp.jasmin.JasminUtils;

public class CondInstructionBuilder extends AbstractBuilder {
    private final Method method;
    private final CondBranchInstruction instruction;

    public CondInstructionBuilder(ClassUnit classUnit, Method method, CondBranchInstruction instruction) {
        super(classUnit);
        this.method = method;
        this.instruction = instruction;
    }

    @Override
    public String compile() {
        if (instruction instanceof SingleOpCondInstruction) {
            Element operand = ((SingleOpCondInstruction) instruction).getCondition().getSingleOperand();
            builder.append(JasminUtils.buildLoadInstructions(operand, method));
        }
        else if (instruction instanceof OpCondInstruction) {
            OpInstruction opInstruction = ((OpCondInstruction) instruction).getCondition();
            builder.append((new OperationInstructionBuilder(classUnit, method, opInstruction)).compile());
        }

        builder.append("ifne ").append(instruction.getLabel());
        return builder.toString();
    }
}
