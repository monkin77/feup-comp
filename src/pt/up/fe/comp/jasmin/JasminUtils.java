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
}
