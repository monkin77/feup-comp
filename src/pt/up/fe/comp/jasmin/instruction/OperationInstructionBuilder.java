package pt.up.fe.comp.jasmin.instruction;

import org.specs.comp.ollir.*;
import pt.up.fe.comp.jasmin.AbstractBuilder;
import pt.up.fe.comp.jasmin.JasminConstants;
import pt.up.fe.comp.jasmin.JasminUtils;
import pt.up.fe.comp.jasmin.MethodsBuilder;

public class OperationInstructionBuilder extends AbstractBuilder {
    private final OpInstruction instruction;
    private final Method method;

    public OperationInstructionBuilder(ClassUnit classUnit, Method method, OpInstruction instruction) {
        super(classUnit);
        this.instruction = instruction;
        this.method = method;
    }

    @Override
    public String compile() {
        if (instruction instanceof BinaryOpInstruction)
            compileBinaryOperation();
        else if (instruction instanceof UnaryOpInstruction)
            compileUnaryOperation();
        return builder.toString();
    }

    private void compileBinaryOperation() {
        final BinaryOpInstruction binaryOpInstruction = (BinaryOpInstruction) instruction;
        final OperationType type = binaryOpInstruction.getOperation().getOpType();

        builder.append(JasminUtils.buildLoadInstruction(binaryOpInstruction.getLeftOperand(), method));
        builder.append(JasminUtils.buildLoadInstruction(binaryOpInstruction.getRightOperand(), method));

        switch (type) {
            case AND, ANDI32, ANDB -> builder.append("iand");
            case LTH, LTHI32 -> {
                // TODO Conversion to >= with neg
                builder.append(JasminConstants.TAB);
                builder.append("if_icmplt IS_LESS_THAN_").append(MethodsBuilder.labelCounter).append("\n");
                builder.append(JasminConstants.TAB);
                builder.append("iconst_0\n");
                builder.append(JasminConstants.TAB);
                builder.append("goto NOT_LESS_THAN_").append(MethodsBuilder.labelCounter).append("\n");
                builder.append(JasminConstants.TAB);
                builder.append("IS_LESS_THAN_").append(MethodsBuilder.labelCounter).append(":\n");
                builder.append(JasminConstants.TAB.repeat(2));
                builder.append("iconst_1\n");
                builder.append(JasminConstants.TAB);
                builder.append("NOT_LESS_THAN_").append(MethodsBuilder.labelCounter).append(":\n");
                ++MethodsBuilder.labelCounter;
            }
            case ADD, ADDI32 -> builder.append("iadd");
            case SUB, SUBI32 -> builder.append("isub");
            case DIV, DIVI32 -> builder.append("idiv");
            case MUL, MULI32 -> builder.append("imul");
        }
    }

    private void compileUnaryOperation() {
        final UnaryOpInstruction unaryOpInstruction = (UnaryOpInstruction) instruction;
        final OperationType type = unaryOpInstruction.getOperation().getOpType();

        if (type != OperationType.NOT && type != OperationType.NOTB) return; // Not supported
        builder.append(JasminUtils.buildLoadInstruction(unaryOpInstruction.getOperand(), method));
        builder.append("ineg");
    }
}
