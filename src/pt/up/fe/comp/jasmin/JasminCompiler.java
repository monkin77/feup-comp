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
        //TODO: Constructor
        ArrayList<Method> methods = classUnit.getMethods();
        for (Method method : methods) {
            String accessModifier = getAccessModifier(method.getMethodAccessModifier());

            builder.append(".method ").append(accessModifier).append(" ");
            if (method.isStaticMethod()) builder.append("static ");
            if (method.isConstructMethod()) builder.append("<init>");
            else builder.append(method.getMethodName());

            builder.append("(");
            for (Element element : method.getParams()) {
                // TODO: Parameter separator
                builder.append(getTypeName(element.getType()));
                builder.append(";");
            }
            builder.append(")");
            builder.append(getTypeName(method.getReturnType())).append("\n");

            compileMethodBody(method);
            builder.append(".end method\n");
        }
    }

    private void compileMethodBody(Method method) {
        ArrayList<Instruction> instructions = method.getInstructions();
        for (Instruction instruction : instructions) {
            builder.append(TAB);
            switch (instruction.getInstType()) {
                case ASSIGN:
                    break;
                case CALL:
                    CallInstruction callInstruction = (CallInstruction) instruction;
                    switch (callInstruction.getInvocationType()) {
                        case invokevirtual -> builder.append("invokevirtual ");
                        case invokespecial -> builder.append("invokespecial ");
                        case invokestatic -> builder.append("invokestatic ");
                        case invokeinterface -> builder.append("invokeinterface ");
                        // TODO: What to do with this?
                        case NEW -> builder.append("new ");
                        case arraylength -> builder.append("arraylength ");
                        case ldc -> builder.append("ldc ");
                    }

                    if (callInstruction.getFirstArg() instanceof Operand) {
                        builder.append((((Operand) callInstruction.getFirstArg()).getName()));
                    } else {
                        // TODO: Remaining cases
                        builder.append(getTypeName(callInstruction.getFirstArg().getType()));
                    }

                    if (callInstruction.getSecondArg().isLiteral()) {
                        LiteralElement literalElement = (LiteralElement) callInstruction.getSecondArg();
                        builder.append("/").append(literalElement.getLiteral()).append(" ");
                    } else {
                        // TODO: Remaining cases
//                        builder.append("/").append(callInstruction.getSecondArg().getName()).append(" ");
                    }

                    for (Element element : callInstruction.getListOfOperands()) {

                    }

                    break;
                case GOTO:
                    break;
                case BRANCH:
                    break;
                case RETURN:
                    break;
                case PUTFIELD:
                    break;
                case GETFIELD:
                    break;
                case UNARYOPER:
                    break;
                case BINARYOPER:
                    break;
                case NOPER:
                    break;
            }
            builder.append("\n");
        }
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
