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

import java.util.HashMap;

public class AssignInstructionBuilderTest {
    private AssignInstructionBuilder assignInstructionBuilder;
    private Type assignType;
    private static MockedStatic<JasminUtils> mockedUtils;

    @BeforeClass
    public static void setupStatic() {
        mockedUtils = Mockito.mockStatic(JasminUtils.class);
        mockedUtils.when(() -> JasminUtils.getElementName(Mockito.any())).thenReturn("destiny");
        mockedUtils.when(() -> JasminUtils.buildLoadInstruction(Mockito.any(), Mockito.any())).thenReturn("ldc 0");
    }

    @AfterClass
    public static void teardown() {
        mockedUtils.close();
    }

    @Before
    public void setup() {
        final Descriptor descriptor = Mockito.mock(Descriptor.class);
        Mockito.when(descriptor.getVirtualReg()).thenReturn(7);

        final HashMap<String, Descriptor> varTable = new HashMap<>();
        varTable.put("destiny", descriptor);

        final Method method = Mockito.mock(Method.class);
        Mockito.when(method.getVarTable()).thenReturn(varTable);

        ClassUnit classUnit = Mockito.mock(ClassUnit.class);
        Mockito.when(classUnit.getClassName()).thenReturn("MyClassUnit");

        AssignInstruction instruction = Mockito.mock(AssignInstruction.class);

        SingleOpInstruction singleOpInstruction = Mockito.mock(SingleOpInstruction.class);
        Mockito.when(singleOpInstruction.getInstType()).thenReturn(InstructionType.NOPER);
        Mockito.when(instruction.getRhs()).thenReturn(singleOpInstruction);

        assignType = Mockito.mock(Type.class);
        Mockito.when(instruction.getTypeOfAssign()).thenReturn(assignType);

        assignInstructionBuilder = new AssignInstructionBuilder(classUnit, method, instruction);
    }

    @Test
    public void intAssignment() {
        Mockito.when(assignType.getTypeOfElement()).thenReturn(ElementType.INT32);

        assertEquals("ldc 0\n" +
                "istore 7", assignInstructionBuilder.compile());
    }

    @Test
    public void objectAssignment() {
        Mockito.when(assignType.getTypeOfElement()).thenReturn(ElementType.OBJECTREF);

        assertEquals("ldc 0\n" +
                "astore 7", assignInstructionBuilder.compile());
    }

    @Test
    public void arrayAssignment() {
        Mockito.when(assignType.getTypeOfElement()).thenReturn(ElementType.ARRAYREF);

        assertEquals("ldc 0\n" +
                "iastore 7", assignInstructionBuilder.compile());
    }
}
