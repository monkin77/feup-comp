package pt.up.fe.comp.jasmin.instruction;

import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.ElementType;
import org.specs.comp.ollir.Method;
import org.specs.comp.ollir.ReturnInstruction;
import pt.up.fe.comp.jasmin.AbstractBuilder;
import pt.up.fe.comp.jasmin.JasminUtils;

public class ReturnInstructionBuilder extends AbstractBuilder {
    private final ReturnInstruction instruction;
    private final Method method;

    public ReturnInstructionBuilder(ClassUnit classUnit, Method method, ReturnInstruction instruction) {
        super(classUnit);
        this.instruction = instruction;
        this.method = method;
    }

    @Override
    public String compile() {
        instruction.setElementType(instruction.hasReturnValue() ?
                instruction.getOperand().getType().getTypeOfElement() :
                ElementType.VOID
        );

        if (instruction.hasReturnValue())
            builder.append(JasminUtils.buildLoadInstruction(instruction.getOperand(), method));

        switch (instruction.getElementType()) {
            case OBJECTREF: case CLASS: case STRING:
            case ARRAYREF:
                builder.append("areturn");
                break;
            case INT32: case BOOLEAN:
                builder.append("ireturn");
                break;
            case VOID:
                // TODO void instruction not appearing -> do it in OLLIR
                builder.append("return");
                break;
        }
        return builder.toString();
    }
}
