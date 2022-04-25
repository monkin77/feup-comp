package pt.up.fe.comp.jasmin.instruction;

import org.specs.comp.ollir.*;
import pt.up.fe.comp.jasmin.AbstractBuilder;

import java.util.List;

import static pt.up.fe.comp.jasmin.JasminConstants.TAB;

public class InstructionBuilder extends AbstractBuilder {
    private final Instruction instruction;
    private final Method method;

    public InstructionBuilder(ClassUnit classUnit, Method method, Instruction instruction) {
        super(classUnit);
        this.method = method;
        this.instruction = instruction;
    }

    @Override
    public String compile() {
        compileInstructionLabels(method, instruction);
        switch (instruction.getInstType()) {
            case ASSIGN:
                AssignInstruction assignInstruction = (AssignInstruction) instruction;
                builder.append((new AssignInstructionBuilder(classUnit, method, assignInstruction)).compile());
                break;
            case CALL:
                CallInstruction callInstruction = (CallInstruction) instruction;
                builder.append((new CallInstructionBuilder(classUnit, method, callInstruction)).compile());
                break;
            case GOTO:
                // TODO
                break;
            case BRANCH:
                // TODO
                break;
            case RETURN:
                ReturnInstruction returnInstruction = (ReturnInstruction) instruction;
                builder.append((new ReturnInstructionBuilder(classUnit, returnInstruction)).compile());
                break;
            case PUTFIELD:
                // TODO
                break;
            case GETFIELD:
                // TODO
                break;
            case UNARYOPER:
            case BINARYOPER:
                OpInstruction opInstruction = (OpInstruction) instruction;
                builder.append((new OperationInstructionBuilder(classUnit, method, opInstruction)).compile());
                break;
            case NOPER:
                // TODO Check if this is right
                builder.append("nop");
                break;
        }

        builder.append("\n");
        return builder.toString();
    }

    private void compileInstructionLabels(final Method method, final Instruction instruction) {
        final List<String> labels = method.getLabels(instruction);
        for (String label : labels)
            builder.append(label).append(":\n").append(TAB);
    }
}
