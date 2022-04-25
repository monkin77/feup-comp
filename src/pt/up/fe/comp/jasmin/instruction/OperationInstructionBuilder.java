package pt.up.fe.comp.jasmin.instruction;

import org.specs.comp.ollir.*;
import pt.up.fe.comp.jasmin.AbstractBuilder;
import pt.up.fe.comp.jasmin.JasminUtils;

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

        builder.append(JasminUtils.buildLoadInstructions(binaryOpInstruction.getLeftOperand(), method));
        builder.append(JasminUtils.buildLoadInstructions(binaryOpInstruction.getRightOperand(), method));

        switch (type) {
            // TODO Why I32 everywhere?
            case AND: case ANDI32:
                builder.append("iand");
                break;
            case LTH: case LTHI32:
                // TODO Ensure unique labels
                builder.append("if_icmplt IS_LESS_THAN\n");
                builder.append("iconst_0\n");
                builder.append("goto NOT_LESS_THAN\n");
                builder.append("IS_LESS_THAN:\n");
                builder.append("iconst_1\n");
                builder.append("NOT_LESS_THAN:\n");
                break;
            case ADD: case ADDI32:
                builder.append("iadd");
                break;
            case SUB: case SUBI32:
                builder.append("isub");
                break;
            case DIV: case DIVI32:
                builder.append("idiv");
                break;
            case MUL: case MULI32:
                builder.append("imul");
                break;
        }
    }

    private void compileUnaryOperation() {
        final UnaryOpInstruction unaryOpInstruction = (UnaryOpInstruction) instruction;
        final OperationType type = unaryOpInstruction.getOperation().getOpType();

        if (type != OperationType.NOT) return; // Not supported
        builder.append("ineg");
    }
}
