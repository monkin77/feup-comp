package pt.up.fe.comp.jasmin.instruction;

import static org.junit.Assert.assertEquals;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.specs.comp.ollir.*;
import pt.up.fe.comp.jasmin.JasminUtils;

public class CondInstructionBuilderTest {
    private CondInstructionBuilder singleOpCondInstructionBuilder;
    private CondInstructionBuilder opCondInstructionBuilder;
    private static MockedStatic<JasminUtils> mockedUtils;

    @BeforeClass
    public static void setupStatic() {
        mockedUtils = Mockito.mockStatic(JasminUtils.class);
        mockedUtils.when(() -> JasminUtils.buildLoadInstruction(Mockito.any(), Mockito.any())).thenReturn("iload 7\n");
    }

    @AfterClass
    public static void teardown() {
        mockedUtils.close();
    }

    @Before
    public void setup() {
        final Method method = Mockito.mock(Method.class);
        final ClassUnit classUnit = Mockito.mock(ClassUnit.class);
        final SingleOpCondInstruction singleOpCondInstruction = Mockito.mock(SingleOpCondInstruction.class);
        Mockito.when(singleOpCondInstruction.getLabel()).thenReturn("SINGLE_LABEL");

        final SingleOpInstruction singleOpInstruction = Mockito.mock(SingleOpInstruction.class);
        Mockito.when(singleOpCondInstruction.getCondition()).thenReturn(singleOpInstruction);

        final OpCondInstruction opCondInstruction = Mockito.mock(OpCondInstruction.class);
        final UnaryOpInstruction opInstruction = Mockito.mock(UnaryOpInstruction.class);
        Mockito.when(opCondInstruction.getCondition()).thenReturn(opInstruction);
        Mockito.when(opCondInstruction.getLabel()).thenReturn("OP_LABEL");

        final Operation operation = Mockito.mock(Operation.class);
        Mockito.when(operation.getOpType()).thenReturn(OperationType.NOT);
        Mockito.when(opInstruction.getOperation()).thenReturn(operation);

        singleOpCondInstructionBuilder = new CondInstructionBuilder(classUnit, method, singleOpCondInstruction);
        opCondInstructionBuilder = new CondInstructionBuilder(classUnit, method, opCondInstruction);
    }

    @Test
    public void singleOpCond() {
        assertEquals("iload 7\n" +
                "ifne SINGLE_LABEL", singleOpCondInstructionBuilder.compile());
    }

    @Test
    public void opCond() {
        assertEquals("""
                iload 7
                iconst_1
                ixor
                ifne OP_LABEL""", opCondInstructionBuilder.compile());
    }
}