package pt.up.fe.comp.jasmin;

import org.specs.comp.ollir.*;

import java.util.ArrayList;

import static pt.up.fe.comp.jasmin.JasminConstants.TAB;

public class MethodsBuilder extends AbstractBuilder {
    public MethodsBuilder(final ClassUnit classUnit) {
        super(classUnit);
    }

    @Override
    public String compile() {
        //TODO: Constructor
        final ArrayList<Method> methods = classUnit.getMethods();
        for (final Method method : methods) {
            final String accessModifier = JasminUtils.getAccessModifier(method.getMethodAccessModifier());

            builder.append(".method ").append(accessModifier).append(" ");
            if (method.isStaticMethod()) builder.append("static ");
            if (method.isConstructMethod()) builder.append("<init>");
            else builder.append(method.getMethodName());

            builder.append("(");
            for (final Element element : method.getParams()) {
                // TODO: Parameter separator
                builder.append(JasminUtils.getTypeName(element.getType(), classUnit));
                builder.append(";");
            }
            builder.append(")");
            builder.append(JasminUtils.getTypeName(method.getReturnType(), classUnit)).append("\n");

            compileMethodBody(method);
            builder.append(".end method\n");
        }
        
        return builder.toString();
    }

    private void compileMethodBody(Method method) {
        final ArrayList<Instruction> instructions = method.getInstructions();
        for (final Instruction instruction : instructions) {
            builder.append(TAB);
            switch (instruction.getInstType()) {
                case ASSIGN:
                    break;
                case CALL:
                    final CallInstruction callInstruction = (CallInstruction) instruction;
                    switch (callInstruction.getInvocationType()) {
                        case invokevirtual:
                            builder.append("invokevirtual ");
                            break;
                        case invokespecial:
                            builder.append("invokespecial ");
                            break;
                        case invokestatic:
                            builder.append("invokestatic ");
                            break;
                        case invokeinterface:
                            builder.append("invokeinterface ");
                            break;
                        // TODO: What to do with this?
                        case NEW:
                            builder.append("new ");
                            break;
                        case arraylength:
                            builder.append("arraylength ");
                            break;
                        case ldc:
                            builder.append("ldc ");
                            break;
                    }

                    if (callInstruction.getFirstArg() instanceof Operand) {
                        builder.append((((Operand) callInstruction.getFirstArg()).getName()));
                    } else {
                        // TODO: Remaining cases
                        builder.append(JasminUtils.getTypeName(callInstruction.getFirstArg().getType(), classUnit));
                    }

                    if (callInstruction.getSecondArg().isLiteral()) {
                        final LiteralElement literalElement = (LiteralElement) callInstruction.getSecondArg();
                        builder.append("/").append(literalElement.getLiteral()).append(" ");
                    } else {
                        // TODO: Remaining cases
//                        builder.append("/").append(callInstruction.getSecondArg().getName()).append(" ");
                    }

                    for (final Element element : callInstruction.getListOfOperands()) {

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
}
