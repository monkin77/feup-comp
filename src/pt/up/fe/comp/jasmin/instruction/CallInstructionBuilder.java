package pt.up.fe.comp.jasmin.instruction;

import org.specs.comp.ollir.*;
import pt.up.fe.comp.jasmin.AbstractBuilder;
import pt.up.fe.comp.jasmin.JasminConstants;
import pt.up.fe.comp.jasmin.JasminUtils;
import pt.up.fe.comp.jasmin.MethodsBuilder;

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
            case invokevirtual -> buildInvokeVirtual();
            case invokespecial -> buildInvokeSpecial();
            case invokestatic -> buildInvokeStatic();
            case invokeinterface -> builInvokeInterface();
            case NEW -> buildNew();
            case arraylength -> buildArrayLength();
            case ldc -> {
                LiteralElement literal = (LiteralElement) (instruction.getFirstArg());
                builder.append(InstructionList.ldc(literal.getLiteral()));
            }
        }

        return builder.toString();
    }

    private void buildArrayLength() {
        builder.append(JasminUtils.buildLoadInstruction(instruction.getFirstArg(), method));
        builder.append(InstructionList.arraylength());
    }

    private void buildInvokeVirtual() {
        this.buildInvocation("invokevirtual");
        MethodsBuilder.updateStackLimit(-1); // object reference
    }

    private void buildInvokeSpecial() {
        final Element firstArg = instruction.getFirstArg();
        String className;

        if (method.isConstructMethod() && JasminUtils.getElementName(instruction.getSecondArg()).equals("\"<init>\"")) {
            className = classUnit.getSuperClass() == null ? JasminConstants.DEFAULT_SUPERCLASS : classUnit.getSuperClass();
        } else {
            className = JasminUtils.getTypeName(firstArg.getType(), classUnit);
        }
        // TODO: What is invokenonvirtual ?
        this.buildInvocation("invokespecial", className, true);
        MethodsBuilder.updateStackLimit(-1); // object reference
    }

    private void buildInvokeStatic() {
        final String className = JasminUtils.getElementName(instruction.getFirstArg());
        this.buildInvocation("invokestatic", className, false);
    }

    private void builInvokeInterface() {
        this.buildInvocation("invokeinterface");
        MethodsBuilder.updateStackLimit(-1); // object reference

        builder.append(" ").append(instruction.getListOfOperands().size());
    }

    private void buildNew() {
        if (instruction.getReturnType().getTypeOfElement() == ElementType.ARRAYREF) {
            final String className = "int"; // TODO Only works for int
            builder.append(JasminUtils.buildLoadInstruction(instruction.getListOfOperands().get(0), method));
            builder.append(InstructionList.newarray(className));
            return;
        }

        final String className = JasminUtils.getTypeName(instruction.getFirstArg().getType(), classUnit);
        builder.append(InstructionList.newInstruction(className));
    }

    private void buildInvocation(String invokeInstruction) {
        final Element firstArg = instruction.getFirstArg();
        final String className = JasminUtils.getTypeName(firstArg.getType(), classUnit);
        this.buildInvocation(invokeInstruction, className, true);
    }

    private void buildInvocation(String invokeInstruction, String className, boolean loadObject) {
        final Element firstArg = instruction.getFirstArg();
        final Element secondArg = instruction.getSecondArg();

        final String rawMethodName = JasminUtils.getElementName(secondArg);
        final String methodName = rawMethodName.substring(1, rawMethodName.length() - 1);

        if (loadObject) builder.append(JasminUtils.buildLoadInstruction(firstArg, method));
        for (Element element : instruction.getListOfOperands())
            builder.append(JasminUtils.buildLoadInstruction(element, method));

        MethodsBuilder.updateStackLimit(-instruction.getListOfOperands().size());
        if (instruction.getReturnType().getTypeOfElement() != ElementType.VOID) {
            MethodsBuilder.updateStackLimit(1);
        }

        builder.append(invokeInstruction).append(" ");
        builder.append(className).append("/").append(methodName).append("(");

        for (Element element : instruction.getListOfOperands()) {
            final String typeName = JasminUtils.getTypeName(element.getType(), classUnit);
            builder.append(typeName);
        }

        builder.append(")").append(JasminUtils.getTypeName(instruction.getReturnType(), classUnit, true));
    }
}
