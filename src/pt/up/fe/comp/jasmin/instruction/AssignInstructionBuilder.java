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
        // TODO Difference between istore 0 and istore_0
        switch (assignType) {
            case THIS, OBJECTREF, CLASS, STRING -> builder.append("astore ").append(descriptor.getVirtualReg());
            case INT32, BOOLEAN -> builder.append("istore ").append(descriptor.getVirtualReg());
            case ARRAYREF -> builder.append("astore ").append(descriptor.getVirtualReg());
        }

        return builder.toString();
    }
}
