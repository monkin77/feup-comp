package pt.up.fe.comp.jasmin.instruction;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.specs.comp.ollir.*;
import pt.up.fe.comp.jasmin.JasminUtils;

import static org.junit.Assert.*;

public class OperationInstructionBuilderTest {
    private OperationInstructionBuilder unaryOperationInstructionBuilder;
    private OperationInstructionBuilder binaryOperationInstructionBuilder;
    private Operation operation;

    @BeforeClass
    public static void setupStatic() {
        MockedStatic<JasminUtils> mockedUtils = Mockito.mockStatic(JasminUtils.class);
        mockedUtils.when(() -> JasminUtils.buildLoadInstruction(Mockito.any(), Mockito.any())).thenReturn("iload 7\n");
    }

    @Before
    public void setup() {
        final Method method = Mockito.mock(Method.class);
        final ClassUnit classUnit = Mockito.mock(ClassUnit.class);

        operation = Mockito.mock(Operation.class);
        final UnaryOpInstruction unaryOpInstruction = Mockito.mock(UnaryOpInstruction.class);
        Mockito.when(unaryOpInstruction.getOperation()).thenReturn(operation);
        final BinaryOpInstruction binaryOpInstruction = Mockito.mock(BinaryOpInstruction.class);
        Mockito.when(binaryOpInstruction.getOperation()).thenReturn(operation);

        unaryOperationInstructionBuilder = new OperationInstructionBuilder(classUnit, method, unaryOpInstruction);
        binaryOperationInstructionBuilder = new OperationInstructionBuilder(classUnit, method, binaryOpInstruction);
    }

    @Test
    public void notOperation() {
        Mockito.when(operation.getOpType()).thenReturn(OperationType.NOT);

        assertEquals("ineg", unaryOperationInstructionBuilder.compile());
    }

    @Test
    public void andOperation() {
        Mockito.when(operation.getOpType()).thenReturn(OperationType.ANDI32);

        assertEquals("iload 7\n" +
                "iload 7\n" +
                "iand", binaryOperationInstructionBuilder.compile());
    }

    @Test
    public void addOperation() {
        Mockito.when(operation.getOpType()).thenReturn(OperationType.ADDI32);

        assertEquals("iload 7\n" +
                "iload 7\n" +
                "iadd", binaryOperationInstructionBuilder.compile());
    }

    @Test
    public void subOperation() {
        Mockito.when(operation.getOpType()).thenReturn(OperationType.SUBI32);

        assertEquals("iload 7\n" +
                "iload 7\n" +
                "isub", binaryOperationInstructionBuilder.compile());
    }

    @Test
    public void divOperation() {
        Mockito.when(operation.getOpType()).thenReturn(OperationType.DIVI32);

        assertEquals("iload 7\n" +
                "iload 7\n" +
                "idiv", binaryOperationInstructionBuilder.compile());
    }

    @Test
    public void mulOperation() {
        Mockito.when(operation.getOpType()).thenReturn(OperationType.MULI32);

        assertEquals("iload 7\n" +
                "iload 7\n" +
                "imul", binaryOperationInstructionBuilder.compile());
    }

    @Test
    public void lessThanOperation() {
        Mockito.when(operation.getOpType()).thenReturn(OperationType.LTHI32);

        assertEquals("iload 7\n" +
                "iload 7\n" +
                "    if_icmplt IS_LESS_THAN_0\n" +
                "    iconst_0\n" +
                "    goto NOT_LESS_THAN_0\n" +
                "    IS_LESS_THAN_0:\n" +
                "        iconst_1\n" +
                "    NOT_LESS_THAN_0:\n", binaryOperationInstructionBuilder.compile());
    }
}
