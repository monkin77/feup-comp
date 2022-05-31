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
        instruction.getReturnType().setTypeOfElement(instruction.hasReturnValue() ?
                instruction.getOperand().getType().getTypeOfElement() :
                ElementType.VOID
        );

        if (instruction.hasReturnValue())
            builder.append(JasminUtils.buildLoadInstruction(instruction.getOperand(), method));

        switch (instruction.getElementType()) {
            case OBJECTREF, CLASS, STRING, ARRAYREF -> builder.append(InstructionList.areturn());
            case INT32, BOOLEAN -> builder.append(InstructionList.ireturn());
            case VOID -> builder.append(InstructionList.returnInstruction());
        }
        return builder.toString();
    }
}
