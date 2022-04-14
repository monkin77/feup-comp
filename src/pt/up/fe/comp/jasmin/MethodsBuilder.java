package pt.up.fe.comp.jasmin;

import org.specs.comp.ollir.*;
import pt.up.fe.comp.jasmin.instruction.CallInstructionBuilder;

import java.util.ArrayList;
import java.util.List;

import static pt.up.fe.comp.jasmin.JasminConstants.TAB;

public class MethodsBuilder extends AbstractBuilder {
    public MethodsBuilder(final ClassUnit classUnit) {
        super(classUnit);
    }

    @Override
    public String compile() {
        final ArrayList<Method> methods = classUnit.getMethods();
        for (final Method method : methods) {
            final String accessModifier = JasminUtils.getAccessModifier(method.getMethodAccessModifier());
            builder.append(".method ").append(accessModifier).append(" ");

            if (method.isStaticMethod()) builder.append("static ");
            if (method.isFinalMethod()) builder.append("final ");
            if (method.isConstructMethod()) builder.append("<init>");
            else builder.append(method.getMethodName());

            builder.append("(");
            for (final Element element : method.getParams())
                builder.append(JasminUtils.getTypeName(element.getType(), classUnit));

            builder.append(")");
            builder.append(JasminUtils.getTypeName(method.getReturnType(), classUnit)).append("\n");

            compileMethodBody(method);
            builder.append(".end method\n");
        }

        return builder.toString();
    }

    private void compileMethodBody(final Method method) {
        final ArrayList<Instruction> instructions = method.getInstructions();
        method.buildVarTable();

        for (final Instruction instruction : instructions) {
            builder.append(TAB);
            compileInstructionLabels(method, instruction);

            // TODO Maybe create package with instruction builders
            switch (instruction.getInstType()) {
                case ASSIGN:
                    // TODO
                    break;
                case CALL:
                    builder.append((new CallInstructionBuilder(classUnit, method, instruction)).compile());
                    break;
                case GOTO:
                    // TODO
                    break;
                case BRANCH:
                    // TODO
                    break;
                case RETURN:
                    // TODO
                    break;
                case PUTFIELD:
                    // TODO
                    break;
                case GETFIELD:
                    // TODO
                    break;
                case UNARYOPER:
                    // TODO
                    break;
                case BINARYOPER:
                    // TODO
                    break;
                case NOPER:
                    // TODO
                    break;
                default:
                    // TODO Check which cases fit in default
            }
            builder.append("\n");
        }
    }

    private void compileInstructionLabels(final Method method, final Instruction instruction) {
        final List<String> labels = method.getLabels(instruction);
        for (String label : labels)
            builder.append(label).append(":\n").append(TAB);
    }
}
