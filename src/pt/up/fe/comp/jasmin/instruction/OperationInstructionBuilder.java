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
            case AND, ANDB -> builder.append("iand");
            case LTH -> {
                String operatorStr = "LESS_THAN";
                String operation = InstructionList.if_icmplt("IS_" + operatorStr + "_" + MethodsBuilder.labelCounter);
                appendOperation(operatorStr, operation);
            }
            case ADD -> builder.append(InstructionList.iadd());
            case SUB -> builder.append(InstructionList.isub());
            case DIV -> builder.append(InstructionList.idiv());
            case MUL -> builder.append(InstructionList.imul());
        }
    }

    private void appendOperation(String operatorStr, String operation) {
        builder.append(JasminConstants.TAB);
        builder.append(operation).append("\n");
        builder.append(JasminConstants.TAB);
        builder.append(InstructionList.loadIntConstant(0)).append("\n");
        builder.append(JasminConstants.TAB);
        builder.append(InstructionList.gotoInstruction("NOT_" + operatorStr + "_" + MethodsBuilder.labelCounter)).append("\n");
        builder.append(JasminConstants.TAB);
        builder.append("IS_").append(operatorStr).append("_").append(MethodsBuilder.labelCounter).append(":\n");
        builder.append(JasminConstants.TAB.repeat(2));
        builder.append(InstructionList.loadIntConstant(1)).append("\n");
        builder.append(JasminConstants.TAB);
        builder.append("NOT_").append(operatorStr).append("_").append(MethodsBuilder.labelCounter).append(":\n");
        ++MethodsBuilder.labelCounter;
    }

    private void compileUnaryOperation() {
        final UnaryOpInstruction unaryOpInstruction = (UnaryOpInstruction) instruction;
        final OperationType type = unaryOpInstruction.getOperation().getOpType();

        if (type != OperationType.NOT && type != OperationType.NOTB) return; // Not supported
        builder.append(JasminUtils.buildLoadInstruction(unaryOpInstruction.getOperand(), method));
        builder.append(InstructionList.loadIntConstant(1)).append("\n");
        builder.append(InstructionList.ixor());
    }
}
