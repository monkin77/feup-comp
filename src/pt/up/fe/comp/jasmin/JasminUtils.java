package pt.up.fe.comp.jasmin;

import org.specs.comp.ollir.*;
import pt.up.fe.comp.jasmin.instruction.InstructionList;

import java.util.Locale;

import static pt.up.fe.comp.jasmin.JasminConstants.STRING_TYPE;

public class JasminUtils {
    public static String getAccessModifier(AccessModifiers modifier) {
        if (modifier == AccessModifiers.DEFAULT)
            return "public";
        else
            return modifier.toString().toLowerCase(Locale.ROOT);
    }

    public static String getTypeName(Type type, ClassUnit classUnit) {
        return getTypeName(type, type.getTypeOfElement(), classUnit, false);
    }

    public static String getTypeName(Type type, ClassUnit classUnit, boolean isArgType) {
        return getTypeName(type, type.getTypeOfElement(), classUnit, isArgType);
    }

    public static String getTypeName(Type type, ElementType elementType, ClassUnit classUnit, boolean isArgType) {
        switch (elementType) {
            case INT32:
                return "I";
            case BOOLEAN:
                return "Z";
            case ARRAYREF:
                final ArrayType arrayType = (ArrayType) type;
                final String arrayFlag = "[".repeat(arrayType.getNumDimensions());
                // TODO Can't pass type here if array of objects is possible
                return arrayFlag + getTypeName(type, arrayType.getArrayType(), classUnit, isArgType);
            case OBJECTREF:
            case CLASS:
                final String className = ((ClassType) type).getName();
                final String fullClassName = getFullClassName(className, classUnit);
                return isArgType ? "L" + fullClassName + ";" : fullClassName;
            case THIS:
                return classUnit.getClassName();
            case STRING:
                return STRING_TYPE;
            case VOID:
                return "V";
        }
        return "";
    }

    public static String getElementName(Element element) {
        if (element.isLiteral())
            return ((LiteralElement) element).getLiteral();
        return ((Operand) element).getName();
    }

    public static String buildLoadInstruction(Element element, Method method) {
        final StringBuilder builder = new StringBuilder();
        final String elementName = getElementName(element);

        if (element.isLiteral()) {
            int x = Integer.parseInt(elementName);
            builder.append(InstructionList.loadIntConstant(x)).append("\n");
            return builder.toString();
        } else if (element.getType().getTypeOfElement() == ElementType.BOOLEAN && (elementName.equals("true") || elementName.equals("false"))) {
            if (elementName.equals("false")) builder.append(InstructionList.loadIntConstant(0)).append("\n");
            else builder.append(InstructionList.loadIntConstant(1)).append("\n");
            return builder.toString();
        }

        final Descriptor descriptor = method.getVarTable().get(elementName);
        final ElementType type = element.getType().getTypeOfElement();

        if (element instanceof ArrayOperand) builder.append(loadArrayElement(type, (ArrayOperand) element, method));
        else builder.append(loadPrimitiveElement(type, descriptor));

        builder.append("\n");
        return builder.toString();
    }

    public static String getFullClassName(String className, ClassUnit classUnit) {
        for (String importString : classUnit.getImports()) {
            String[] importArray = importString.split("\\.");
            String lastName = importArray.length == 0 ? importString : importArray[importArray.length - 1];
            if (lastName.equals(className)) {
                return importString.replace(".", "/");
            }
        }

        return className;
    }

    private static String loadArrayElement(ElementType elemType, ArrayOperand operand, Method method) {
        StringBuilder builder = new StringBuilder();

        // Load Array ref
        Descriptor arrayDescriptor = method.getVarTable().get(operand.getName());
        builder.append(InstructionList.aload(arrayDescriptor.getVirtualReg())).append("\n");

        // Load index
        Element index = operand.getIndexOperands().get(0);
        builder.append(JasminUtils.buildLoadInstruction(index, method));

        switch (elemType) {
            case THIS, OBJECTREF, CLASS, STRING, ARRAYREF -> builder.append(InstructionList.aaload());
            case INT32, BOOLEAN -> builder.append(InstructionList.iaload());
        }

        return builder.toString();
    }

    private static String loadPrimitiveElement(ElementType elemType, Descriptor descriptor) {
        return switch (elemType) {
            case THIS, OBJECTREF, CLASS, STRING, ARRAYREF -> InstructionList.aload(descriptor.getVirtualReg());
            case INT32, BOOLEAN -> InstructionList.iload(descriptor.getVirtualReg());
            case VOID -> null;
        };
    }
}
