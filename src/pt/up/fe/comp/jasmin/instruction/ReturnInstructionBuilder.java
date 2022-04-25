package pt.up.fe.comp.jasmin.instruction;

import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.ElementType;
import org.specs.comp.ollir.ReturnInstruction;
import pt.up.fe.comp.jasmin.AbstractBuilder;

public class ReturnInstructionBuilder extends AbstractBuilder {
    private final ReturnInstruction instruction;

    public ReturnInstructionBuilder(ClassUnit classUnit, ReturnInstruction instruction) {
        super(classUnit);
        this.instruction = instruction;
    }

    @Override
    public String compile() {
        instruction.setElementType(instruction.hasReturnValue() ?
                instruction.getOperand().getType().getTypeOfElement() :
                ElementType.VOID
        );

        switch (instruction.getElementType()) {
            case OBJECTREF: case CLASS: case STRING:
                builder.append("areturn");
                break;
            case INT32: case BOOLEAN: case ARRAYREF:
                // TODO Check array return
                builder.append("ireturn");
                break;
            case VOID:
                // TODO void instruction not appearing -.-'
                builder.append("return");
                break;
        }
        return builder.toString();
    }
}
