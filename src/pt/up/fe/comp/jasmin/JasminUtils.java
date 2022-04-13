package pt.up.fe.comp.jasmin;

import org.specs.comp.ollir.*;

import java.util.Locale;

import static pt.up.fe.comp.jasmin.JasminConstants.STRING_TYPE;

class JasminUtils {
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
}
