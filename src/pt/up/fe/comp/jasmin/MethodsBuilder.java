package pt.up.fe.comp.jasmin;

import org.specs.comp.ollir.*;
import pt.up.fe.comp.jasmin.instruction.InstructionBuilder;
import pt.up.fe.comp.jasmin.instruction.InstructionList;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static pt.up.fe.comp.jasmin.JasminConstants.TAB;

public class MethodsBuilder extends AbstractBuilder {
    public static int labelCounter = 0;
    private static int stackLimit = 0;
    private static int currentStack = 0;

    public MethodsBuilder(final ClassUnit classUnit) {
        super(classUnit);
    }

    @Override
    public String compile() {
        final ArrayList<Method> methods = classUnit.getMethods();
        for (final Method method : methods) {
            final String accessModifier = JasminUtils.getAccessModifier(method.getMethodAccessModifier());
            builder.append(".method ").append(accessModifier).append(" ");

            if (method.isStaticMethod()) builder.append("static ");
            if (method.isFinalMethod()) builder.append("final ");
            if (method.isConstructMethod()) builder.append("<init>");
            else builder.append(method.getMethodName());

            builder.append("(");
            for (final Element element : method.getParams())
                builder.append(JasminUtils.getTypeName(element.getType(), classUnit, true));

            builder.append(")");
            builder.append(JasminUtils.getTypeName(method.getReturnType(), classUnit, true)).append("\n");

            compileMethodBody(method);
            builder.append(".end method\n\n");
        }

        return builder.toString();
    }

    private void compileMethodBody(final Method method) {
        final StringBuilder sb = new StringBuilder();
        stackLimit = 0;
        currentStack = 0;

        method.buildVarTable();

        sb.append(".limit locals ").append(getLocalsLimits(method)).append("\n");
        final ArrayList<Instruction> instructions = method.getInstructions();

        for (int i = 0; i < instructions.size(); i++) {
            Instruction instruction = instructions.get(i);
            if (instruction instanceof OpCondInstruction condBranchInstruction) {
                List<Instruction> nextInstructions = instructions.subList(i, instructions.size());
                int n = attemptSwitchReplacement(condBranchInstruction, nextInstructions, method, sb);
                if (n > 0) {
                    i += n - 2;
                    continue;
                }
            }
            sb.append(TAB);
            sb.append((new InstructionBuilder(classUnit, method, instruction)).compile());
            if (instruction.getInstType() == InstructionType.CALL && ((CallInstruction) (instruction)).getReturnType().getTypeOfElement() != ElementType.VOID) {
                sb.append("pop").append("\n");
            }
        }
        if (instructions.size() == 0 || instructions.get(instructions.size() - 1).getInstType() != InstructionType.RETURN) {
            sb.append(TAB);
            sb.append(InstructionList.returnInstruction()).append("\n");
        }

        builder.append(".limit stack ").append(stackLimit).append("\n").append(sb);
    }

    private int attemptSwitchReplacement(CondBranchInstruction instruction, List<Instruction> instructions, Method method, StringBuilder sb) {
        if (!(instruction.getCondition() instanceof BinaryOpInstruction condition)) return 0;
        if (!condition.getOperation().getOpType().equals(OperationType.EQ)) return 0;

        Map<String, String> switchCases = new LinkedHashMap<>();
        final Operand variableOperand;
        final LiteralElement literalElement;

        if (condition.getLeftOperand() instanceof Operand leftOperand &&
            condition.getRightOperand() instanceof LiteralElement rightLiteral) {
            variableOperand = leftOperand;
            literalElement = rightLiteral;
        } else if (condition.getLeftOperand() instanceof LiteralElement leftLiteral &&
                   condition.getRightOperand() instanceof Operand rightOperand) {
            variableOperand = rightOperand;
            literalElement = leftLiteral;
        } else {
            return 0;
        }

        switchCases.put(literalElement.getLiteral(), instruction.getLabel());
        Map<String, String> nextCases = collectCases(instructions, variableOperand);
        if (nextCases == null) return 0;
        switchCases.putAll(nextCases);
        sb.append(buildSwitch(method, switchCases, variableOperand));
        sb.append("default_switch_").append(labelCounter++).append(":\n");
        return switchCases.size();
    }

    private Map<String, String> collectCases(List<Instruction> instructions, Operand switchVariable) {
        Map<String, String> switchCases = new LinkedHashMap<>();
        for (Instruction next : instructions) {
            if (!(next instanceof OpCondInstruction condInstruction)) {
                switchCases.put("default", "default_switch_" + labelCounter);
                return switchCases;
            }

            if (!(condInstruction.getCondition() instanceof BinaryOpInstruction binaryOpInstruction)) return null;
            Operation operation = binaryOpInstruction.getOperation();
            if (!operation.getOpType().equals(OperationType.EQ)) {
                return null;
            }
            Element leftElement = binaryOpInstruction.getLeftOperand();
            Element rightElement = binaryOpInstruction.getRightOperand();
            final Operand variable;
            final LiteralElement value;
            if (leftElement instanceof Operand leftOperand && rightElement instanceof LiteralElement rightLiteral) {
                variable = leftOperand;
                value = rightLiteral;
            } else if (leftElement instanceof LiteralElement leftLiteral && rightElement instanceof Operand rightOperand) {
                variable = rightOperand;
                value = leftLiteral;
            } else {
                return null;
            }
            if (!variable.getName().equals(switchVariable.getName())) return null;
            switchCases.put(value.getLiteral(), condInstruction.getLabel());
        }
        return null;
    }

    private String buildSwitch(Method method, Map<String, String> cases, Element element) {
        StringBuilder sb = new StringBuilder();
        sb.append(JasminUtils.buildLoadInstruction(element, method));
        sb.append("lookupswitch").append("\n");
        for (Map.Entry<String, String> entry : cases.entrySet()) {
            String caseValue = entry.getKey();
            String caseTarget = entry.getValue();
            sb.append(caseValue).append(": ").append(caseTarget).append("\n");
        }
        sb.append("\n");
        return sb.toString();
    }

    private int getLocalsLimits(final Method method) {
        if (method.getVarTable().isEmpty()) return method.isStaticMethod() ? 0 : 1;
        int maxReg = method.getVarTable().values().stream().mapToInt(Descriptor::getVirtualReg).max().orElse(-1);
        return maxReg + 1;
    }

    public static void updateStackLimit(int sizeChange) {
        currentStack += sizeChange;
        stackLimit = Math.max(stackLimit, currentStack);
    }
}
