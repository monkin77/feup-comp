package pt.up.fe.comp.jasmin;

import org.specs.comp.ollir.ClassUnit;

import static pt.up.fe.comp.jasmin.JasminConstants.DEFAULT_SUPERCLASS;

public class JasminBuilder extends AbstractBuilder {
    private final boolean optimize;
    public JasminBuilder(final ClassUnit classUnit, final boolean optimize) {
        super(classUnit);
        this.optimize = optimize;
    }

    public String compile() {
        compileClassDeclaration();
        compileSuperclassDeclaration();
        builder.append(new FieldsBuilder(classUnit).compile());
        builder.append(new MethodsBuilder(classUnit, optimize).compile());

        return builder.toString();
    }

    private void compileClassDeclaration() {
        final String accessModifier = JasminUtils.getAccessModifier(classUnit.getClassAccessModifier());
        builder.append(".class ").append(accessModifier).append(" ")
                .append(classUnit.getClassName()).append("\n");
    }

    private void compileSuperclassDeclaration() {
        builder.append(".super ");

        if (classUnit.getSuperClass() == null)
            builder.append(DEFAULT_SUPERCLASS);
        else
            builder.append(JasminUtils.getFullClassName(classUnit.getSuperClass(), classUnit));

        builder.append("\n");
    }
}
