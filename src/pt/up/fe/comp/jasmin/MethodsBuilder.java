package pt.up.fe.comp.jasmin;

import org.specs.comp.ollir.*;
import pt.up.fe.comp.jasmin.instruction.InstructionBuilder;
import pt.up.fe.comp.jasmin.instruction.InstructionList;

import java.util.*;

import static pt.up.fe.comp.jasmin.JasminConstants.TAB;

public class MethodsBuilder extends AbstractBuilder {
    public static int labelCounter = 0;
    public static HashSet<Instruction> instructionsToInvert = new HashSet<>();
    private static int stackLimit = 0;
    private static int currentStack = 0;
    private final boolean optimizeIfStatements;

    public MethodsBuilder(final ClassUnit classUnit, final boolean optimizeIfStatements) {
        super(classUnit);
        this.optimizeIfStatements = optimizeIfStatements;
    }

    public MethodsBuilder(final ClassUnit classUnit) {
        this(classUnit, false);
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
        instructionsToInvert.clear();

        if (optimizeIfStatements) invertIfInstructions(method);

        sb.append(".limit locals ").append(getLocalsLimits(method)).append("\n");
        final ArrayList<Instruction> instructions = method.getInstructions();

        for (int i = 0; i < instructions.size(); i++) {
            Instruction instruction = instructions.get(i);
            if (instruction instanceof OpCondInstruction condBranchInstruction && optimizeIfStatements) {
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
                sb.append(InstructionList.pop()).append("\n");
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

        SortedMap<String, String> switchCases = collectCases(instructions, variableOperand);
        if (switchCases == null) return 0;
        switchCases.put(literalElement.getLiteral(), instruction.getLabel());
        if (switchCases.size() < 3) return 0;
        Set<String> labels = new TreeSet<>(switchCases.keySet());
        labels.remove("default");
        Integer[] cases = labels.stream().map(Integer::valueOf).toArray(Integer[]::new);
        int averageDistance = 0;
        for (int i = 0; i < cases.length - 1; i++) averageDistance += cases[i + 1] - cases[i];
        averageDistance /= cases.length - 1;
        if (averageDistance < 3) {
            sb.append(buildTableSwitch(method, switchCases, variableOperand));
        } else {
            sb.append(buildLookupSwitch(method, switchCases, variableOperand));
        }
        sb.append("default_switch_").append(labelCounter++).append(":\n");
        return switchCases.size();
    }

    private SortedMap<String, String> collectCases(List<Instruction> instructions, Operand switchVariable) {
        SortedMap<String, String> switchCases = new TreeMap<>();
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

    private String buildLookupSwitch(Method method, Map<String, String> cases, Element element) {
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

    private String buildTableSwitch(Method method, SortedMap<String, String> cases, Element element) {
        StringBuilder sb = new StringBuilder();
        sb.append(JasminUtils.buildLoadInstruction(element, method));
        Set<String> labels = new TreeSet<>(cases.keySet());
        labels.remove("default");
        SortedSet<Integer> values = new TreeSet<>(labels.stream().map(Integer::valueOf).toList());
        int lower = values.first();
        int upper = values.last();
        for (int i = lower; i <= upper; i++) {
            if (!cases.containsKey(String.valueOf(i))) {
                cases.put(String.valueOf(i), "default_switch_" + labelCounter);
            }
        }
        sb.append("tableswitch").append(" ").append(lower).append(" ").append(upper).append("\n");
        for (Map.Entry<String, String> entry : cases.entrySet()) {
            String caseValue = entry.getKey();
            if (caseValue.equals("default")) sb.append("default: ");
            String caseTarget = entry.getValue();
            sb.append(caseTarget).append("\n");
        }
        sb.append("\n");
        return sb.toString();
    }

    private int getLocalsLimits(final Method method) {
        if (method.getVarTable().isEmpty()) return method.isStaticMethod() ? 0 : 1;

        ArrayList<Descriptor> locals = new ArrayList<>(method.getVarTable().values());
        if (!method.isStaticMethod()) locals.add(new Descriptor(0));
        return (int) locals.stream().mapToInt(Descriptor::getVirtualReg).distinct().count();
    }

    private void invertIfInstructions(final Method method) {
        ArrayList<Instruction> instructions = method.getInstructions();
        HashMap<String, Instruction> labels = method.getLabels();

        for (int i = 0; i < instructions.size(); ++i) {
            Instruction instruction = method.getInstructions().get(i);
            if (instruction.getInstType() != InstructionType.BRANCH)
                continue;

            CondBranchInstruction condBranchInstruction = (CondBranchInstruction) instruction;
            int n = attemptSwitchReplacement(condBranchInstruction, instructions.subList(i, instructions.size()), method, new StringBuilder());
            if (n > 0) {
                i += n - 2;
                continue;
            }

            String ifLabel = condBranchInstruction.getLabel();

            int underscorePosition = ifLabel.indexOf('_');
            // Only try to optimize if statements from our ollir
            if (underscorePosition == -1 || !ifLabel.substring(0, underscorePosition).equals("ifbody"))
                continue;
            // String endifLabel = "endif_" + ifLabel.substring(underscorePosition + 1);

            ArrayList<Instruction> elseInstructions = new ArrayList<>();
            int j = i + 1;
            // Move the else instructions
            while (labels.get(ifLabel) != instructions.get(j))
                elseInstructions.add(instructions.remove(j));
            boolean hasElseBlock = elseInstructions.size() > 1;
            String endifLabel = ((GotoInstruction) elseInstructions.get(elseInstructions.size() - 1)).getLabel();

            if (hasElseBlock) {
                // Replace the previous ifbody label
                labels.replace(ifLabel, elseInstructions.get(0));
            } else {
                labels.remove(ifLabel);
            }

            while (labels.get(endifLabel) != instructions.get(j))
                ++j;

            // Move the labels from the goto to the same instructions as endif
            // This is needed because the goto always goes to endif but that was changed
            for (String key : labels.keySet()) {
                if (labels.get(key) == elseInstructions.get(elseInstructions.size() - 1))
                    labels.replace(key, instructions.get(j));
            }

            if (hasElseBlock) {
                // Re-add the goto instruction and else instructions
                instructions.add(j, elseInstructions.remove(elseInstructions.size() - 1));
                instructions.addAll(j + 1, elseInstructions);
            } else {
                // Jump to the end of the if
                condBranchInstruction.setLabel(endifLabel);
            }

            instructionsToInvert.add(instruction);
        }
    }

    public static void updateStackLimit(int sizeChange) {
        currentStack += sizeChange;
        stackLimit = Math.max(stackLimit, currentStack);
    }
}
