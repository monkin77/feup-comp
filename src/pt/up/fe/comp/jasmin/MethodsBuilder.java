package pt.up.fe.comp.jasmin;

import org.specs.comp.ollir.*;
import pt.up.fe.comp.jasmin.instruction.InstructionBuilder;

import java.util.ArrayList;

import static pt.up.fe.comp.jasmin.JasminConstants.TAB;

public class MethodsBuilder extends AbstractBuilder {
    public static int labelCounter = 0;

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
            builder.append(".end method\n\n");
        }

        return builder.toString();
    }

    private void compileMethodBody(final Method method) {
        builder.append(".limit stack 99\n"); // TODO Stack limits
        builder.append(".limit locals 99\n"); // TODO Locals limits
        final ArrayList<Instruction> instructions = method.getInstructions();

        method.buildVarTable();

        for (final Instruction instruction : instructions) {
            builder.append(TAB);
            builder.append((new InstructionBuilder(classUnit, method, instruction)).compile());
        }
    }
}
