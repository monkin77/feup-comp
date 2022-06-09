package pt.up.fe.comp.registerAllocation.dataflow;

import com.javacc.parser.tree.Literal;
import org.specs.comp.ollir.*;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class UsedVariables {
    private final Instruction instruction;

    public UsedVariables(Instruction instruction) {
        this.instruction = instruction;
    }

    public String[] getUsed() {
        switch (this.instruction.getInstType()) {
            case ASSIGN -> {
                return getAssign((AssignInstruction) instruction);
            }
            case CALL -> {
                return getCall((CallInstruction) instruction);
            }
            case PUTFIELD -> {
                getUsedPutField((PutFieldInstruction) instruction);
            }
            case BINARYOPER -> {
                getUsedBinaryOp((BinaryOpInstruction) instruction);
            }
        }

        return new String[]{};
    }

    private String[] getAssign(AssignInstruction instruction) {
        List<String> usedVars = new ArrayList<>();
        var rhs = instruction.getRhs();

        // Left side is an array operation
        Element dest = instruction.getDest();
        if (dest instanceof ArrayOperand)
            usedVars.addAll(getOperandUses(dest));

        usedVars.addAll(Arrays.asList(new UsedVariables(rhs).getUsed()));
        return usedVars.toArray(new String[0]);
    }

    private String[] getCall(CallInstruction instruction) {
        List<String> used = new ArrayList<>();
        ArrayList<Element> elements = instruction.getListOfOperands();
        Operand firstArg = (Operand) instruction.getFirstArg();

        if (instruction.getSecondArg() == null) return used.toArray(new String[0]);
        // If it's an initialization does not store
        if (instruction.getSecondArg().isLiteral() && ((LiteralElement) instruction.getSecondArg()).getLiteral().equals("<init>")) {
            return used.toArray(new String[0]);
        }

        // Classes don't have register
        if (firstArg.getType().getTypeOfElement() != ElementType.CLASS)
            used.addAll(getOperandUses(instruction.getFirstArg()));
        for (Element element : elements) {
            used.addAll(getOperandUses(element));
        }
        return used.toArray(new String[0]);
    }

    private String[] getUsedPutField(PutFieldInstruction instruction) {
        List<String> used = new ArrayList<>(getOperandUses(instruction.getThirdOperand()));
        return used.toArray(new String[0]);
    }

    private String[] getUsedBinaryOp(BinaryOpInstruction instruction) {
        OperationType instType = instruction.getOperation().getOpType();
        List<String> used = new ArrayList<>();

        if (instType == OperationType.NOTB) {
            used.addAll(getOperandUses(instruction.getRightOperand()));
        } else {
            used.addAll(getOperandUses(instruction.getLeftOperand()));
            used.addAll(getOperandUses(instruction.getRightOperand()));
        }

        return used.toArray(new String[0]);
    }

    /**
     * @return List of elements that are used in the given operand
     */
    private List<String> getOperandUses(Element element) {
        List<String> elementsName = new ArrayList<>();

        if (element.isLiteral()) return elementsName;
        else if (element instanceof ArrayOperand arrayOperand) {
            elementsName.add(getArrayIndexName(arrayOperand));
        }

        elementsName.add(((Operand) element).getName());

        return elementsName;
    }

    /**
     * Adds the element that is used as index in the array
     */
    public String getArrayIndexName(Element arrayElem) {
        ArrayOperand arrayOperand = (ArrayOperand) arrayElem;
        ArrayList<Element> indexOperand = arrayOperand.getIndexOperands();
        return ((Operand) indexOperand.get(0)).getName();
    }
}
