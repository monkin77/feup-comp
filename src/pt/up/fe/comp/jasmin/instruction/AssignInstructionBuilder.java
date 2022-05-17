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

        builder.append((new InstructionBuilder(classUnit, method, instruction.getRhs())).compile());
        final ElementType assignType = instruction.getTypeOfAssign().getTypeOfElement();

        if (destiny instanceof ArrayOperand) storeArrayElement(assignType, (ArrayOperand) destiny, descriptor);
        else storePrimitiveElement(assignType, descriptor);

        return builder.toString();
    }

    private void storeArrayElement(ElementType elemType, ArrayOperand operand, Descriptor descriptor) {
        // Load index
        Element index = operand.getIndexOperands().get(0);
        builder.append(JasminUtils.buildLoadInstruction(index, method));

        // TODO Load Array ref

        switch (elemType) {
            case THIS, OBJECTREF, CLASS, STRING, ARRAYREF -> builder.append("aastore ").append(descriptor.getVirtualReg());
            case INT32, BOOLEAN -> builder.append("iastore ").append(descriptor.getVirtualReg());
        }
    }

    private void storePrimitiveElement(ElementType elemType, Descriptor descriptor) {
        // TODO Difference between istore 0 and istore_0
        switch (elemType) {
            case THIS, OBJECTREF, CLASS, STRING, ARRAYREF -> builder.append("astore ").append(descriptor.getVirtualReg());
            case INT32, BOOLEAN -> builder.append("istore ").append(descriptor.getVirtualReg());
        }
    }
}
