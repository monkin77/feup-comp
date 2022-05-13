package pt.up.fe.comp.jasmin.instruction;

import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.Element;
import org.specs.comp.ollir.GetFieldInstruction;
import org.specs.comp.ollir.Method;
import pt.up.fe.comp.jasmin.AbstractBuilder;
import pt.up.fe.comp.jasmin.JasminUtils;

public class GetFieldInstructionBuilder extends AbstractBuilder {
    private final Method method;
    private final GetFieldInstruction instruction;

    public GetFieldInstructionBuilder(ClassUnit classUnit, Method method, GetFieldInstruction instruction) {
        super(classUnit);
        this.method = method;
        this.instruction = instruction;
    }

    @Override
    public String compile() {
        final Element firstArg = instruction.getFirstOperand();
        final Element secondArg = instruction.getSecondOperand();

        final String className = JasminUtils.getTypeName(firstArg.getType(), classUnit);
        final String fieldName = JasminUtils.getElementName(secondArg);

        builder.append(JasminUtils.buildLoadInstruction(firstArg, method));

        final String typeName = JasminUtils.getTypeName(instruction.getFieldType(), classUnit, true);

        builder.append("getfield ").append(className).append("/").append(fieldName);
        builder.append(" ").append(typeName);

        return builder.toString();
    }
}
