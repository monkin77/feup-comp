package pt.up.fe.comp.jasmin.instruction;

import org.specs.comp.ollir.*;
import pt.up.fe.comp.jasmin.AbstractBuilder;
import pt.up.fe.comp.jasmin.JasminUtils;
import pt.up.fe.comp.jasmin.MethodsBuilder;

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
            builder.append(MethodsBuilder.instructionsToInvert.contains(instruction) ?
                    InstructionList.ifeq(instruction.getLabel()) : InstructionList.ifne(instruction.getLabel()));
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
            if (checkComparisonWithZeroAndCompile(binaryOpInstruction))
                return;

            type = binaryOpInstruction.getOperation().getOpType();
            builder.append(JasminUtils.buildLoadInstruction(binaryOpInstruction.getLeftOperand(), method));
            builder.append(JasminUtils.buildLoadInstruction(binaryOpInstruction.getRightOperand(), method));
        }
        else if (opInstruction instanceof UnaryOpInstruction unaryOpInstruction) {
            type = unaryOpInstruction.getOperation().getOpType();
            builder.append(JasminUtils.buildLoadInstruction(unaryOpInstruction.getOperand(), method));
        } else return;

        if (MethodsBuilder.instructionsToInvert.contains(instruction)) {
            // The instructions are inverted for optimization. The if/else bodies have already been inverted
            switch (type) {
                case AND, ANDB -> builder.append(InstructionList.iand()).append("\n")
                        .append(InstructionList.ifeq(instruction.getLabel()));
                case OR, ORB -> builder.append(InstructionList.ior()).append("\n")
                        .append(InstructionList.ifeq(instruction.getLabel()));
                case LTH -> builder.append(InstructionList.if_icmpge(instruction.getLabel()));
                case GTH -> builder.append(InstructionList.if_icmple(instruction.getLabel()));
                case LTE -> builder.append(InstructionList.if_icmpgt(instruction.getLabel()));
                case GTE -> builder.append(InstructionList.if_icmplt(instruction.getLabel()));
                case EQ -> builder.append(InstructionList.if_icmpne(instruction.getLabel()));
                case NEQ -> builder.append(InstructionList.if_icmpeq(instruction.getLabel()));
                case NOT, NOTB -> builder.append(InstructionList.ifne(instruction.getLabel()));
            }
        } else {
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

    private boolean checkComparisonWithZeroAndCompile(BinaryOpInstruction binaryOpInstruction) {
        OperationType type = binaryOpInstruction.getOperation().getOpType();
        if (type != OperationType.LTH && type != OperationType.GTH && type != OperationType.LTE && type != OperationType.GTE
                && type != OperationType.EQ && type != OperationType.NEQ)
            return false;

        String leftName = JasminUtils.getElementName(binaryOpInstruction.getLeftOperand());
        String rightName = JasminUtils.getElementName(binaryOpInstruction.getRightOperand());
        if (!leftName.equals("0") && !rightName.equals("0"))
            return false;

        boolean loadRight = leftName.equals("0");
        Element elementToLoad = loadRight ? binaryOpInstruction.getRightOperand()
                : binaryOpInstruction.getLeftOperand();
        builder.append(JasminUtils.buildLoadInstruction(elementToLoad, method));

        if (MethodsBuilder.instructionsToInvert.contains(instruction)) {
            // The instructions are inverted for optimization. The if/else bodies have already been inverted
            switch (type) {
                case LTH -> builder.append(loadRight ?
                        InstructionList.ifle(instruction.getLabel()) :
                        InstructionList.ifge(instruction.getLabel())
                );
                case GTH -> builder.append(loadRight ?
                        InstructionList.ifge(instruction.getLabel()) :
                        InstructionList.ifle(instruction.getLabel())
                );
                case LTE -> builder.append(loadRight ?
                        InstructionList.iflt(instruction.getLabel()) :
                        InstructionList.ifgt(instruction.getLabel())
                );
                case GTE -> builder.append(loadRight ?
                        InstructionList.ifgt(instruction.getLabel()) :
                        InstructionList.iflt(instruction.getLabel())
                );
                case EQ -> builder.append(InstructionList.ifne(instruction.getLabel()));
                case NEQ -> builder.append(InstructionList.ifeq(instruction.getLabel()));
            }
        } else {
            switch (type) {
                case LTH -> builder.append(loadRight ?
                        InstructionList.ifgt(instruction.getLabel()) :
                        InstructionList.iflt(instruction.getLabel())
                );
                case GTH -> builder.append(loadRight ?
                        InstructionList.iflt(instruction.getLabel()) :
                        InstructionList.ifgt(instruction.getLabel())
                );
                case LTE -> builder.append(loadRight ?
                        InstructionList.ifge(instruction.getLabel()) :
                        InstructionList.ifle(instruction.getLabel())
                );
                case GTE -> builder.append(loadRight ?
                        InstructionList.ifle(instruction.getLabel()) :
                        InstructionList.ifge(instruction.getLabel())
                );
                case EQ -> builder.append(InstructionList.ifeq(instruction.getLabel()));
                case NEQ -> builder.append(InstructionList.ifne(instruction.getLabel()));
            }
        }

        return true;
    }
}
