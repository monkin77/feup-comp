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
        builder.append((new InstructionBuilder(classUnit, method, instruction.getRhs())).compile());

        final String storeInstruction = switch (elemType) {
            case THIS, OBJECTREF, CLASS, STRING, ARRAYREF -> InstructionList.astore(descriptor.getVirtualReg());
            case INT32, BOOLEAN -> InstructionList.istore(descriptor.getVirtualReg());
            case VOID -> null;
        };
        builder.append(storeInstruction);
    }
}
