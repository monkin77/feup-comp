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

public class ReturnInstructionBuilderTest {
    private ReturnInstructionBuilder returnInstructionBuilder;
    private ReturnInstruction returnInstruction;
    private static MockedStatic<JasminUtils> mockedUtils;

    @BeforeClass
    public static void setupStatic() {
        mockedUtils = Mockito.mockStatic(JasminUtils.class);
    }

    @AfterClass
    public static void teardown() {
        mockedUtils.close();
    }

    @Before
    public void setup() {
        final Method method = Mockito.mock(Method.class);
        final ClassUnit classUnit = Mockito.mock(ClassUnit.class);

        Type type = Mockito.mock(Type.class);

        returnInstruction = Mockito.mock(ReturnInstruction.class);
        Mockito.when(returnInstruction.hasReturnValue()).thenReturn(false);
        Mockito.when(returnInstruction.getReturnType()).thenReturn(type);

        final Element operand = Mockito.mock(Element.class);
        Mockito.when(returnInstruction.getOperand()).thenReturn(operand);

        Mockito.when(returnInstruction.getElementType()).thenReturn(ElementType.VOID);
        Mockito.when(operand.getType()).thenReturn(type);

        returnInstructionBuilder = new ReturnInstructionBuilder(classUnit, method, returnInstruction);
    }

    @Test
    public void voidReturn() {
        assertEquals("return", returnInstructionBuilder.compile());
    }

    @Test
    public void objectReturn() {
        mockedUtils.when(() -> JasminUtils.buildLoadInstruction(Mockito.any(), Mockito.any())).thenReturn("aload 7\n");

        Mockito.when(returnInstruction.getElementType()).thenReturn(ElementType.OBJECTREF);
        Mockito.when(returnInstruction.hasReturnValue()).thenReturn(true);

        assertEquals("aload 7\n" +
                "areturn", returnInstructionBuilder.compile());
    }

    @Test
    public void intReturn() {
        mockedUtils.when(() -> JasminUtils.buildLoadInstruction(Mockito.any(), Mockito.any())).thenReturn("iload 7\n");

        Mockito.when(returnInstruction.getElementType()).thenReturn(ElementType.INT32);
        Mockito.when(returnInstruction.hasReturnValue()).thenReturn(true);

        assertEquals("iload 7\n" +
                "ireturn", returnInstructionBuilder.compile());
    }
}