package pt.up.fe.comp.jasmin.instruction;

import org.specs.comp.ollir.*;
import pt.up.fe.comp.jasmin.AbstractBuilder;
import pt.up.fe.comp.jasmin.JasminUtils;

public class PutFieldInstructionBuilder extends AbstractBuilder {
    private final Method method;
    private final PutFieldInstruction instruction;

    public PutFieldInstructionBuilder(ClassUnit classUnit, Method method, PutFieldInstruction instruction) {
        super(classUnit);
        this.method = method;
        this.instruction = instruction;
    }

    @Override
    public String compile() {
        final Element firstArg = instruction.getFirstOperand();
        final Element secondArg = instruction.getSecondOperand();
        final Element thirdArg = instruction.getThirdOperand();

        final String className = JasminUtils.getTypeName(firstArg.getType(), classUnit);
        final String fieldName = JasminUtils.getElementName(secondArg);

        builder.append(JasminUtils.buildLoadInstructions(firstArg, method));
        builder.append(JasminUtils.buildLoadInstructions(thirdArg, method));

        final String typeName = JasminUtils.getTypeName(instruction.getFieldType(), classUnit);

        builder.append("putfield ").append(className).append("/").append(fieldName);
        builder.append(" ").append(typeName);

        return builder.toString();
    }
}
