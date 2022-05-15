package pt.up.fe.comp.jasmin.instruction;

import static org.junit.Assert.assertEquals;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.Element;
import org.specs.comp.ollir.GetFieldInstruction;
import org.specs.comp.ollir.Method;
import pt.up.fe.comp.jasmin.JasminUtils;

public class GetFieldInstructionBuilderTest {
    private GetFieldInstructionBuilder getFieldInstructionBuilder;
    private static MockedStatic<JasminUtils> mockedUtils;

    @BeforeClass
    public static void setupStatic() {
        mockedUtils = Mockito.mockStatic(JasminUtils.class);
        mockedUtils.when(() -> JasminUtils.getElementName(Mockito.any())).thenReturn("fieldName");
        mockedUtils.when(() -> JasminUtils.getTypeName(Mockito.any(), Mockito.any())).thenReturn("className");
        mockedUtils.when(() -> JasminUtils.getTypeName(Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn("className");
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
        final GetFieldInstruction getFieldInstruction = Mockito.mock(GetFieldInstruction.class);

        final Element element = Mockito.mock(Element.class);
        Mockito.when(getFieldInstruction.getFirstOperand()).thenReturn(element);

        getFieldInstructionBuilder = new GetFieldInstructionBuilder(classUnit, method, getFieldInstruction);
    }

    @Test
    public void getField() {
        assertEquals("aload 7\n" +
                "getfield className/fieldName className", getFieldInstructionBuilder.compile());
    }
}
