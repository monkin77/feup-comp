package pt.up.fe.comp.jasmin;

import org.specs.comp.ollir.*;

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
        return getTypeName(type, type.getTypeOfElement(), classUnit);
    }

    public static String getTypeName(Type type, ElementType elementType, ClassUnit classUnit) {
        switch (elementType) {
            case INT32:
                return "I";
            case BOOLEAN:
                return "Z";
            case ARRAYREF:
                final ArrayType arrayType = (ArrayType) type;
                final String arrayFlag = "[".repeat(arrayType.getNumDimensions());
                return arrayFlag + getTypeName(type, arrayType.getTypeOfElements(), classUnit);
            case OBJECTREF:
            case CLASS:
                return ((ClassType) type).getName();
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

    public static String buildLoadInstructions(Element element, Method method) {
        final StringBuilder builder = new StringBuilder();
        final String elementName = getElementName(element);

        if (element.isLiteral()) {
            // TODO Use iconst, etc. when possible
            builder.append("ldc ").append(elementName).append("\n");
            return builder.toString();
        }

        final Descriptor descriptor = method.getVarTable().get(elementName);
        final ElementType type = element.getType().getTypeOfElement();

        // TODO Difference between iload 0 and iload_0
        switch (type) {
            case THIS: case OBJECTREF: case CLASS: case STRING:
                builder.append("aload ").append(descriptor.getVirtualReg());
                break;
            case INT32: case BOOLEAN:
                builder.append("iload ").append(descriptor.getVirtualReg());
                break;
            case ARRAYREF:
                builder.append("iaload ").append(descriptor.getVirtualReg());
                break;
        }

        builder.append("\n");
        return builder.toString();
    }
}
