package pt.up.fe.comp.jasmin;

import org.specs.comp.ollir.*;
import pt.up.fe.comp.jasmin.instruction.InstructionBuilder;
import pt.up.fe.comp.jasmin.instruction.InstructionList;

import java.util.*;
import java.util.stream.IntStream;

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

        for (final Instruction instruction : instructions) {
            sb.append(TAB);
            sb.append((new InstructionBuilder(classUnit, method, instruction)).compile());
            if (instruction.getInstType() == InstructionType.CALL && ((CallInstruction) (instruction)).getReturnType().getTypeOfElement() != ElementType.VOID) {
                sb.append(InstructionList.pop()).append("\n");
            }
        }

        if (instructions.size() == 0 ||
            instructions.get(instructions.size() - 1).getInstType() != InstructionType.RETURN) {
            sb.append(TAB);
            sb.append(InstructionList.returnInstruction()).append("\n");
        }

        builder.append(".limit stack ").append(stackLimit).append("\n").append(sb);
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
