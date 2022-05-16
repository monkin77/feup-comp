package pt.up.fe.comp.jasmin.instruction;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.specs.comp.ollir.*;
import pt.up.fe.comp.jasmin.JasminConstants;
import pt.up.fe.comp.jasmin.JasminUtils;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class CallInstructionBuilderTest {
    private CallInstructionBuilder callInstructionBuilder;
    private ArrayList<Element> operands;
    private CallInstruction callInstruction;
    private Method method;
    private static MockedStatic<JasminUtils> mockedUtils;
    private ClassUnit classUnit;

    @BeforeClass
    public static void setupStatic() {
        mockedUtils = Mockito.mockStatic(JasminUtils.class);
        mockedUtils.when(() -> JasminUtils.getElementName(Mockito.any())).thenReturn("\"elem\"");
        mockedUtils.when(() -> JasminUtils.getTypeName(Mockito.any(), Mockito.any())).thenReturn("T");
        mockedUtils.when(() -> JasminUtils.getTypeName(Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn("T");
        mockedUtils.when(() -> JasminUtils.buildLoadInstruction(Mockito.any(), Mockito.any())).thenReturn("aload 7\n");
    }

    @AfterClass
    public static void teardown() {
        mockedUtils.close();
    }

    @Before
    public void setup() {
        method = Mockito.mock(Method.class);
        classUnit = Mockito.mock(ClassUnit.class);
        callInstruction = Mockito.mock(CallInstruction.class);

        operands = new ArrayList<>();
        Mockito.when(callInstruction.getListOfOperands()).thenReturn(operands);

        final Element firstArg = Mockito.mock(Element.class);
        final Element secondArg = Mockito.mock(Element.class);
        Mockito.when(callInstruction.getFirstArg()).thenReturn(firstArg);
        Mockito.when(callInstruction.getSecondArg()).thenReturn(secondArg);

        callInstructionBuilder = new CallInstructionBuilder(classUnit, method, callInstruction);
    }

    @Test
    public void invokeVirtual() {
        Mockito.when(callInstruction.getInvocationType()).thenReturn(CallType.invokevirtual);

        assertEquals("aload 7\n" +
                "invokevirtual T/elem()T", callInstructionBuilder.compile());
    }

    @Test
    public void invokeSpecial() {
        Mockito.when(callInstruction.getInvocationType()).thenReturn(CallType.invokespecial);

        assertEquals("aload 7\n" +
                "invokespecial T/elem()T", callInstructionBuilder.compile());
    }

    @Test
    public void invokeDefaultConstructor() {
        Mockito.when(callInstruction.getInvocationType()).thenReturn(CallType.invokespecial);
        Mockito.when(method.isConstructMethod()).thenReturn(true);
        mockedUtils.when(() -> JasminUtils.getElementName(Mockito.any())).thenReturn("\"<init>\"");

        assertEquals("aload 7\n" +
                "invokespecial " + JasminConstants.DEFAULT_SUPERCLASS + "/<init>()T", callInstructionBuilder.compile());
    }

    @Test
    public void invokeSuperConstructor() {
        Mockito.when(callInstruction.getInvocationType()).thenReturn(CallType.invokespecial);
        Mockito.when(method.isConstructMethod()).thenReturn(true);
        Mockito.when(classUnit.getSuperClass()).thenReturn("Super");
        mockedUtils.when(() -> JasminUtils.getElementName(Mockito.any())).thenReturn("\"<init>\"");

        assertEquals("aload 7\n" +
                "invokespecial Super/<init>()T", callInstructionBuilder.compile());

        mockedUtils.when(() -> JasminUtils.getElementName(Mockito.any())).thenReturn("\"elem\"");
    }

    @Test
    public void invokeStatic() {
        Mockito.when(callInstruction.getInvocationType()).thenReturn(CallType.invokestatic);

        assertEquals("invokestatic \"elem\"/elem()T", callInstructionBuilder.compile());
    }

    @Test
    public void invokeInterface() {
        Mockito.when(callInstruction.getInvocationType()).thenReturn(CallType.invokeinterface);

        assertEquals("aload 7\n" +
                "invokeinterface T/elem()T 0", callInstructionBuilder.compile());
    }

    @Test
    public void invokeInterfaceMultipleOperands() {
        Mockito.when(callInstruction.getInvocationType()).thenReturn(CallType.invokeinterface);

        final Element operand = Mockito.mock(Element.class);
        operands.add(operand); operands.add(operand);

        assertEquals("aload 7\n" +
                "aload 7\n" +
                "aload 7\n" +
                "invokeinterface T/elem(TT)T 2", callInstructionBuilder.compile());
    }

    @Test
    public void invokeNew() {
        Mockito.when(callInstruction.getInvocationType()).thenReturn(CallType.NEW);

        assertEquals("new T", callInstructionBuilder.compile());
    }

    @Test
    public void loadConstant() {
        Mockito.when(callInstruction.getInvocationType()).thenReturn(CallType.ldc);

        final LiteralElement element = Mockito.mock(LiteralElement.class);
        Mockito.when(element.getLiteral()).thenReturn("10");
        Mockito.when(callInstruction.getFirstArg()).thenReturn(element);

        assertEquals("ldc 10", callInstructionBuilder.compile());
    }

    @Test
    public void arrayLength() {
        Mockito.when(callInstruction.getInvocationType()).thenReturn(CallType.arraylength);

        // TODO
        assertEquals("aload 7\narraylength", callInstructionBuilder.compile());
    }
}
