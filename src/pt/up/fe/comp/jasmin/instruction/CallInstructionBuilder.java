package pt.up.fe.comp.jasmin.instruction;

import org.specs.comp.ollir.*;
import pt.up.fe.comp.jasmin.AbstractBuilder;
import pt.up.fe.comp.jasmin.JasminUtils;

public class CallInstructionBuilder extends AbstractBuilder {
    private final CallInstruction instruction;
    private final Method method;
    public CallInstructionBuilder(ClassUnit classUnit, Method method, CallInstruction instruction) {
        super(classUnit);
        this.instruction = instruction;
        this.method = method;
    }

    @Override
    public String compile() {
        switch (instruction.getInvocationType()) {
            case invokevirtual:
                buildInvokeVirtual(instruction);
                break;
            case invokespecial:
                buildInvokeSpecial(instruction);
                break;
            case invokestatic:
                buildInvokeStatic(instruction);
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
        final Element secondArg = instruction.getSecondArg();
        final String className = JasminUtils.getTypeName(firstArg.getType(), classUnit);

        final String rawMethodName = JasminUtils.getElementName(secondArg);
        final String methodName = rawMethodName.substring(1, rawMethodName.length() - 1);

        buildLoadInstructions(firstArg);
        for (Element element : instruction.getListOfOperands())
            buildLoadInstructions(element);

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

        final String rawMethodName = JasminUtils.getElementName(instruction.getSecondArg());
        final String methodName = rawMethodName.substring(1, rawMethodName.length() - 1);

        buildLoadInstructions(firstArg);
        for (Element element : instruction.getListOfOperands())
            buildLoadInstructions(element);

        builder.append("invokespecial ");
        builder.append(className).append("/").append(methodName).append("(");

        for (Element element : instruction.getListOfOperands()) {
            final String typeName = JasminUtils.getTypeName(element.getType(), classUnit);
            builder.append(typeName);
        }

        builder.append(")").append(JasminUtils.getTypeName(instruction.getReturnType(), classUnit));
    }

    private void buildInvokeStatic(CallInstruction instruction) {
        final String className = JasminUtils.getElementName(instruction.getFirstArg());

        final String rawMethodName = JasminUtils.getElementName(instruction.getSecondArg());
        final String methodName = rawMethodName.substring(1, rawMethodName.length() - 1);

        buildLoadInstructions(instruction.getFirstArg());
        for (Element element : instruction.getListOfOperands())
            buildLoadInstructions(element);

        builder.append("invokestatic ");
        builder.append(className).append("/").append(methodName).append("(");

        for (Element element : instruction.getListOfOperands()) {
            final String typeName = JasminUtils.getTypeName(element.getType(), classUnit);
            builder.append(typeName);
        }

        builder.append(")").append(JasminUtils.getTypeName(instruction.getReturnType(), classUnit));
    }

    private void buildLoadInstructions(Element element) {
        final String elementName = JasminUtils.getElementName(element);

        if (element.isLiteral()) {
            builder.append("ldc ").append(elementName).append("\n");
            return;
        }

        final Descriptor descriptor = method.getVarTable().get(elementName);
        final ElementType type = element.getType().getTypeOfElement();

        switch (type) {
            case THIS: case OBJECTREF: case CLASS: case STRING:
                builder.append("aload_").append(descriptor.getVirtualReg());
                break;
            case INT32: case BOOLEAN:
                builder.append("iload_").append(descriptor.getVirtualReg());
                break;
            case ARRAYREF:
                builder.append("iaload").append(descriptor.getVirtualReg());
                break;
        }

        builder.append("\n");
    }
}
