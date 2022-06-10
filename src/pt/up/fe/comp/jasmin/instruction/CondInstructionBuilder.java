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
            builder.append(JasminUtils.buildLoadInstruction(operand, method));
            builder.append(InstructionList.ifne(instruction.getLabel()));
        }
        else if (instruction instanceof OpCondInstruction) {
            OpInstruction opInstruction = ((OpCondInstruction) instruction).getCondition();
            compileOpCondition(opInstruction);
        }

        return builder.toString();
    }

    private void compileOpCondition(OpInstruction opInstruction) {
        OperationType type;
        if (opInstruction instanceof BinaryOpInstruction binaryOpInstruction) {
            type = binaryOpInstruction.getOperation().getOpType();
            builder.append(JasminUtils.buildLoadInstruction(binaryOpInstruction.getLeftOperand(), method));
            builder.append(JasminUtils.buildLoadInstruction(binaryOpInstruction.getRightOperand(), method));
        }
        else if (opInstruction instanceof UnaryOpInstruction unaryOpInstruction) {
            type = unaryOpInstruction.getOperation().getOpType();
            builder.append(JasminUtils.buildLoadInstruction(unaryOpInstruction.getOperand(), method));
        } else return;

        switch (type) {
            case AND, ANDB -> builder.append(InstructionList.iand()).append("\n")
                    .append(InstructionList.ifne(instruction.getLabel()));
            case OR, ORB -> builder.append(InstructionList.ior()).append("\n")
                    .append(InstructionList.ifne(instruction.getLabel()));
            case LTH -> builder.append(InstructionList.if_icmplt(instruction.getLabel()));
            case GTH -> builder.append(InstructionList.if_icmpgt(instruction.getLabel()));
            case LTE -> builder.append(InstructionList.if_icmple(instruction.getLabel()));
            case GTE -> builder.append(InstructionList.if_icmpge(instruction.getLabel()));
            case EQ -> builder.append(InstructionList.if_icmpeq(instruction.getLabel()));
            case NEQ -> builder.append(InstructionList.if_icmpne(instruction.getLabel()));
            case NOT, NOTB -> builder.append(InstructionList.ifeq(instruction.getLabel()));
        }
    }
}
