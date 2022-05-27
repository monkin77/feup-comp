package pt.up.fe.comp.jasmin;

import org.specs.comp.ollir.*;
import pt.up.fe.comp.jasmin.instruction.InstructionBuilder;

import java.util.ArrayList;

import static pt.up.fe.comp.jasmin.JasminConstants.TAB;

public class MethodsBuilder extends AbstractBuilder {
    public static int labelCounter = 0;
    private static int stackLimit = 0;
    private static int currentStack = 0;

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
                builder.append(JasminUtils.getTypeName(element.getType(), classUnit, true));

            builder.append(")");
            builder.append(JasminUtils.getTypeName(method.getReturnType(), classUnit, true)).append("\n");

            compileMethodBody(method);
            builder.append(".end method\n\n");
        }

        return builder.toString();
    }

    private void compileMethodBody(final Method method) {
        final StringBuilder sb = new StringBuilder();
        stackLimit = 0;
        currentStack = 0;

        sb.append(".limit locals ").append(getLocalsLimits(method)).append("\n");
        final ArrayList<Instruction> instructions = method.getInstructions();

        method.buildVarTable();

        for (final Instruction instruction : instructions) {
            sb.append(TAB);
            sb.append((new InstructionBuilder(classUnit, method, instruction)).compile());
        }

        if (instructions.size() == 0 ||
                instructions.get(instructions.size() - 1).getInstType() != InstructionType.RETURN) {
            sb.append(TAB);
            sb.append("return").append("\n");
        }

        builder.append(".limit stack ").append(stackLimit).append("\n").append(sb);
    }

    private int getLocalsLimits(final Method method) {
        int locals = method.getVarTable().size();
        if (!method.isStaticMethod()) locals++; // this
        return locals;
    }

    public static void updateStackLimit(int sizeChange) {
        currentStack += sizeChange;
        stackLimit = Math.max(stackLimit, currentStack);
    }
}
