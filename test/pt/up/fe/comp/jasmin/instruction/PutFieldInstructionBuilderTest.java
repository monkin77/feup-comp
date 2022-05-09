package pt.up.fe.comp.jasmin.instruction;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.specs.comp.ollir.*;
import pt.up.fe.comp.jasmin.JasminUtils;

import static org.junit.Assert.*;

public class PutFieldInstructionBuilderTest {
    private PutFieldInstructionBuilder putFieldInstructionBuilder;
    private static MockedStatic<JasminUtils> mockedUtils;

    @BeforeClass
    public static void setupStatic() {
        mockedUtils = Mockito.mockStatic(JasminUtils.class);
        mockedUtils.when(() -> JasminUtils.getElementName(Mockito.any())).thenReturn("fieldName");
        mockedUtils.when(() -> JasminUtils.getTypeName(Mockito.any(), Mockito.any())).thenReturn("className");
        mockedUtils.when(() -> JasminUtils.buildLoadInstruction(Mockito.any(), Mockito.any())).thenReturn("aload 7\n");
    }

    @AfterClass
    public static void teardown() {
        mockedUtils.close();
    }

    @Before
    public void setup() {
        final Method method = Mockito.mock(Method.class);
        final ClassUnit classUnit = Mockito.mock(ClassUnit.class);
        final PutFieldInstruction putFieldInstruction = Mockito.mock(PutFieldInstruction.class);

        final Element element = Mockito.mock(Element.class);
        Mockito.when(putFieldInstruction.getFirstOperand()).thenReturn(element);

        putFieldInstructionBuilder = new PutFieldInstructionBuilder(classUnit, method, putFieldInstruction);
    }

    @Test
    public void putField() {
        // Both registers are only 7 on this test
        assertEquals("aload 7\naload 7\n" +
                "putfield className/fieldName className", putFieldInstructionBuilder.compile());
    }
}