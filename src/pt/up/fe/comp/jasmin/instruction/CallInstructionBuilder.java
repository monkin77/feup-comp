package pt.up.fe.comp.jasmin.instruction;

import org.specs.comp.ollir.*;
import pt.up.fe.comp.jasmin.AbstractBuilder;
import pt.up.fe.comp.jasmin.JasminUtils;

public class CallInstructionBuilder extends AbstractBuilder {
    private final Instruction instruction;
    private final Method method;
    public CallInstructionBuilder(ClassUnit classUnit, Method method, Instruction instruction) {
        super(classUnit);
        this.instruction = instruction;
        this.method = method;
    }

    @Override
    public String compile() {
        final CallInstruction callInstruction = (CallInstruction) instruction;
        switch (callInstruction.getInvocationType()) {
            case invokevirtual:
                buildInvokeVirtual(callInstruction);
                break;
            case invokespecial:
                buildInvokeSpecial(callInstruction);
                break;
            case invokestatic:
                buildInvokeStatic(callInstruction);
                break;
            case invokeinterface:
                // TODO
                break;
            // TODO: What to do with this?
            case NEW:
                // TODO
                break;
            case arraylength:
                // TODO
                break;
            case ldc:
                // TODO
                break;
        }

        return builder.toString();
    }

    private void buildInvokeVirtual(CallInstruction instruction) {
        final Element firstArg = instruction.getFirstArg();
        final String className = JasminUtils.getTypeName(firstArg.getType(), classUnit);

        final String rawMethodName = getElementName(instruction.getSecondArg());
        final String methodName = rawMethodName.substring(1, rawMethodName.length() - 1);

        /* Tentativa de por a variavel certa na stack
        final String firstArgName = getElementName(firstArg);
        final Descriptor descriptor = method.getVarTable().get(firstArgName);
        builder.append("aload_").append(descriptor.getVirtualReg()).append("\n");*/

        builder.append("invokevirtual ");
        builder.append(className).append("/").append(methodName).append("(");

        for (Element element : instruction.getListOfOperands()) {
            final String typeName = JasminUtils.getTypeName(element.getType(), classUnit);
            builder.append(typeName);
        }

        builder.append(")").append(JasminUtils.getTypeName(instruction.getReturnType(), classUnit));
    }

    private void buildInvokeSpecial(CallInstruction instruction) {
        final Element firstArg = instruction.getFirstArg();
        final String className = JasminUtils.getTypeName(firstArg.getType(), classUnit);

        final String rawMethodName = getElementName(instruction.getSecondArg());
        final String methodName = rawMethodName.substring(1, rawMethodName.length() - 1);

        builder.append("invokespecial ");
        builder.append(className).append("/").append(methodName).append("(");

        for (Element element : instruction.getListOfOperands()) {
            final String typeName = JasminUtils.getTypeName(element.getType(), classUnit);
            builder.append(typeName);
        }

        builder.append(")").append(JasminUtils.getTypeName(instruction.getReturnType(), classUnit));
    }

    private void buildInvokeStatic(CallInstruction instruction) {
        final String className = getElementName(instruction.getFirstArg());

        final String rawMethodName = getElementName(instruction.getSecondArg());
        final String methodName = rawMethodName.substring(1, rawMethodName.length() - 1);

        builder.append("invokestatic ");
        builder.append(className).append("/").append(methodName).append("(");

        for (Element element : instruction.getListOfOperands()) {
            final String typeName = JasminUtils.getTypeName(element.getType(), classUnit);
            builder.append(typeName);
        }

        builder.append(")").append(JasminUtils.getTypeName(instruction.getReturnType(), classUnit));
    }

    private String getElementName(Element element) {
        if (element.isLiteral())
            return ((LiteralElement) element).getLiteral();
        return ((Operand) element).getName();
    }
}
