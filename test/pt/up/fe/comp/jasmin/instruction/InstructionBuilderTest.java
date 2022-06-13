package pt.up.fe.comp.jasmin.instruction;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.specs.comp.ollir.*;
import pt.up.fe.comp.jasmin.JasminUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;

public class InstructionBuilderTest {
    private InstructionBuilder gotoInstructionBuilder;
    private InstructionBuilder noperInstructionBuilder;
    private List<String> labels;
    private static MockedStatic<JasminUtils> mockedUtils;

    @BeforeClass
    public static void setupStatic() {
        mockedUtils = Mockito.mockStatic(JasminUtils.class);
        mockedUtils.when(() -> JasminUtils.buildLoadInstruction(Mockito.any(), Mockito.any())).thenReturn("aload 7\n");
    }

    @AfterClass
    public static void teardown() {
        mockedUtils.close();
    }

    @Before
    public void setup() {
        final Method method = Mockito.mock(Method.class);

        final GotoInstruction gotoInstruction = Mockito.mock(GotoInstruction.class);
        Mockito.when(gotoInstruction.getInstType()).thenReturn(InstructionType.GOTO);
        Mockito.when(gotoInstruction.getLabel()).thenReturn("LABEL");

        final Instruction uselessInstruction = Mockito.mock(Instruction.class);
        Mockito.when(uselessInstruction.getInstType()).thenReturn(InstructionType.NOPER);

        labels = new ArrayList<>();
        final HashMap<String, Instruction> labelsMap = new HashMap<>();
        labelsMap.put("LABEL", uselessInstruction);

        Mockito.when(method.getLabels(Mockito.any())).thenReturn(labels);
        Mockito.when(method.getLabels()).thenReturn(labelsMap);

        final ClassUnit classUnit = Mockito.mock(ClassUnit.class);

        final SingleOpInstruction singleOpInstruction = Mockito.mock(SingleOpInstruction.class);
        Mockito.when(singleOpInstruction.getInstType()).thenReturn(InstructionType.NOPER);

        gotoInstructionBuilder = new InstructionBuilder(classUnit, method, gotoInstruction);
        noperInstructionBuilder = new InstructionBuilder(classUnit, method, singleOpInstruction);
    }

    @Test
    public void gotoInstruction() {
        assertEquals("goto LABEL\n", gotoInstructionBuilder.compile());
    }

    @Test
    public void noperInstruction() {
        assertEquals("aload 7\n\n", noperInstructionBuilder.compile());
    }

    @Test
    public void instructionWithLabels() {
        labels.add("LABEL");
        labels.add("LABEL2");

        assertEquals("LABEL:\n" +
                "    LABEL2:\n" +
                "    goto LABEL\n", gotoInstructionBuilder.compile());
    }
}