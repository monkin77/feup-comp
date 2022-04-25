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
        System.out.println("type=" + assignType.toString());
        switch (assignType) {
            case THIS: case OBJECTREF: case CLASS: case STRING:
                builder.append("astore_").append(descriptor.getVirtualReg());
                break;
            case INT32: case BOOLEAN:
                builder.append("istore_").append(descriptor.getVirtualReg());
                break;
            case ARRAYREF:
                builder.append("iastore").append(descriptor.getVirtualReg());
                break;
        }

        return builder.toString();
    }
}
