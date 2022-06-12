package pt.up.fe.comp.jasmin.instruction;

import org.specs.comp.ollir.*;
import pt.up.fe.comp.jasmin.AbstractBuilder;
import pt.up.fe.comp.jasmin.JasminUtils;

public class AssignInstructionBuilder extends AbstractBuilder {
    private final AssignInstruction instruction;
    private final Method method;

    public AssignInstructionBuilder(ClassUnit classUnit, Method method, AssignInstruction instruction) {
        super(classUnit);
        this.method = method;
        this.instruction = instruction;
    }

    @Override
    public String compile() {
        final Element destiny = instruction.getDest();
        final String destName = JasminUtils.getElementName(destiny);
        final Descriptor descriptor = method.getVarTable().get(destName);

        final ElementType assignType = instruction.getTypeOfAssign().getTypeOfElement();

        if (destiny instanceof ArrayOperand) storeArrayElement(assignType, (ArrayOperand) destiny);
        else storePrimitiveElement(assignType, descriptor);

        return builder.toString();
    }

    private void storeArrayElement(ElementType elemType, ArrayOperand operand) {
        // Load Array ref
        Descriptor arrayDescriptor = method.getVarTable().get(operand.getName());
        builder.append(InstructionList.aload(arrayDescriptor.getVirtualReg())).append("\n");

        // Load index
        Element index = operand.getIndexOperands().get(0);
        builder.append(JasminUtils.buildLoadInstruction(index, method));

        // Load value
        builder.append((new InstructionBuilder(classUnit, method, instruction.getRhs())).compile());

        switch (elemType) {
            case THIS, OBJECTREF, CLASS, STRING, ARRAYREF -> builder.append(InstructionList.aastore());
            case INT32, BOOLEAN -> builder.append(InstructionList.iastore());
        }
    }

    private void storePrimitiveElement(ElementType elemType, Descriptor descriptor) {
        if (checkIncrementAndCompile(descriptor)) return;
        builder.append((new InstructionBuilder(classUnit, method, instruction.getRhs())).compile());

        final String storeInstruction = switch (elemType) {
            case THIS, OBJECTREF, CLASS, STRING, ARRAYREF -> InstructionList.astore(descriptor.getVirtualReg());
            case INT32, BOOLEAN -> InstructionList.istore(descriptor.getVirtualReg());
            case VOID -> null;
        };
        builder.append(storeInstruction);
    }

    private boolean checkIncrementAndCompile(Descriptor lhsDescriptor) {
        final Instruction rhsInstruction = instruction.getRhs();
        if (!(rhsInstruction instanceof BinaryOpInstruction binaryOpInstruction)) return false;

        OperationType operationType = binaryOpInstruction.getOperation().getOpType();
        if (operationType != OperationType.ADD) return false;

        final Element leftOperand = binaryOpInstruction.getLeftOperand();
        final Element rightOperand = binaryOpInstruction.getRightOperand();
        final Descriptor leftDescriptor = method.getVarTable().get(JasminUtils.getElementName(leftOperand));
        final Descriptor rightDescriptor = method.getVarTable().get(JasminUtils.getElementName(rightOperand));

        Descriptor varDecriptor;
        Element complement;

        if (leftDescriptor == null || leftDescriptor.getVirtualReg() != lhsDescriptor.getVirtualReg()) {
            if (rightDescriptor == null || rightDescriptor.getVirtualReg() != lhsDescriptor.getVirtualReg()) return false;
            varDecriptor = rightDescriptor;
            complement = leftOperand;
        } else {
            varDecriptor = leftDescriptor;
            complement = rightOperand;
        }

        if (!complement.isLiteral() || complement.getType().getTypeOfElement() != ElementType.INT32) return false;

        final int increment = Integer.parseInt(JasminUtils.getElementName(complement));
        if (increment < -128 || increment > 127) return false; // Max 1 byte

        builder.append(InstructionList.iinc(varDecriptor.getVirtualReg(), increment));

        return true;
    }
}
