package pt.up.fe.comp.jasmin.instruction;

import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.Element;
import org.specs.comp.ollir.Method;
import org.specs.comp.ollir.PutFieldInstruction;
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

        builder.append(JasminUtils.buildLoadInstruction(firstArg, method));
        builder.append(JasminUtils.buildLoadInstruction(thirdArg, method));

        final String typeName = JasminUtils.getTypeName(secondArg.getType(), classUnit, true);

        builder.append(InstructionList.putfield(className, fieldName, typeName));

        return builder.toString();
    }
}
