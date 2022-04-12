package pt.up.fe.comp.jasmin;

import org.specs.comp.ollir.*;

import java.util.ArrayList;
import java.util.Locale;

import static pt.up.fe.comp.jasmin.JasminConstants.*;

public class JasminCompiler {
    private final ClassUnit classUnit;
    private final StringBuilder builder;

    public JasminCompiler(ClassUnit classUnit) {
        this.classUnit = classUnit;
        this.builder = new StringBuilder();
    }

    public String compile() {
        compileClassDeclaration();
        compileSuperclassDeclaration();
        compileFieldDeclarations();
        compileMethodDeclarations();

        return builder.toString();
    }

    private void compileClassDeclaration() {
        String accessModifier = getAccessModifier(classUnit.getClassAccessModifier());
        builder.append(".class ").append(accessModifier).append(" ")
                .append(classUnit.getClassName()).append("\n");
    }

    private void compileSuperclassDeclaration() {
        builder.append(".super ");

        if (classUnit.getSuperClass() == null)
            builder.append(DEFAULT_SUPERCLASS);
        else
            builder.append(classUnit.getSuperClass());

        builder.append("\n");
    }

    private void compileFieldDeclarations() {
        ArrayList<Field> fields = classUnit.getFields();
        for (Field field : fields) {
            String accessModifier = getAccessModifier(field.getFieldAccessModifier());

            builder.append(".field ").append(accessModifier).append(" ").append(field.getFieldName())
                    .append(" ").append(getTypeName(field.getFieldType())).append("\n");
        }
    }

    private void compileMethodDeclarations() {

    }

    private String getAccessModifier(AccessModifiers modifier) {
        if (modifier == AccessModifiers.DEFAULT)
            return "public";
        else
            return modifier.toString().toLowerCase(Locale.ROOT);
    }

    private String getTypeName(Type type) {
        return this.getTypeName(type, type.getTypeOfElement());
    }

    private String getTypeName(Type type, ElementType elementType) {
        switch (elementType) {
            case INT32:
                return "I";
            case BOOLEAN:
                return "Z";
            case ARRAYREF:
                ArrayType arrayType = (ArrayType) type;
                return "[" + getTypeName(type, arrayType.getTypeOfElements());
            case OBJECTREF:
            case CLASS:
                return ((ClassType) type).getName();
            case THIS:
                return classUnit.getClassName();
            case STRING:
                return "Ljava/lang/String";
            case VOID:
                return "V";
        }
        return "";
    }
}
