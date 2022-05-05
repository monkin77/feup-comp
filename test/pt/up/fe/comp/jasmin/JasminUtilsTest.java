package pt.up.fe.comp.jasmin;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.specs.comp.ollir.*;

import java.util.HashMap;

public class JasminUtilsTest {
    private ClassUnit classUnit;
    private LiteralElement literalElement;

    @Before
    public void setup() {
        classUnit = Mockito.mock(ClassUnit.class);
        Mockito.when(classUnit.getClassName()).thenReturn("MyClassUnit");

        literalElement = Mockito.mock(LiteralElement.class);
        Mockito.when(literalElement.isLiteral()).thenReturn(true);
        Mockito.when(literalElement.getLiteral()).thenReturn("LiteralElement");
    }

    @Test
    public void getAccessModifier() {
        assertEquals("public", JasminUtils.getAccessModifier(AccessModifiers.PUBLIC));
        assertEquals("private", JasminUtils.getAccessModifier(AccessModifiers.PRIVATE));
        assertEquals("protected", JasminUtils.getAccessModifier(AccessModifiers.PROTECTED));
        assertEquals("public", JasminUtils.getAccessModifier(AccessModifiers.DEFAULT));
    }

    @Test
    public void getTypeName() {
        final ArrayType arrayType = Mockito.mock(ArrayType.class);
        Mockito.when(arrayType.getNumDimensions()).thenReturn(2);
        Mockito.when(arrayType.getTypeOfElements()).thenReturn(ElementType.INT32);

        final ClassType classType = Mockito.mock(ClassType.class);
        Mockito.when(classType.getName()).thenReturn("MyClass");

        assertEquals("[[I", JasminUtils.getTypeName(arrayType, ElementType.ARRAYREF, classUnit));
        assertEquals("MyClass", JasminUtils.getTypeName(classType, ElementType.CLASS, classUnit));
        assertEquals("MyClassUnit", JasminUtils.getTypeName(classType, ElementType.THIS, classUnit));
        assertEquals(JasminConstants.STRING_TYPE, JasminUtils.getTypeName(classType, ElementType.STRING, classUnit));
    }

    @Test
    public void getElementName() {
        final Operand operand = Mockito.mock(Operand.class);
        Mockito.when(operand.isLiteral()).thenReturn(false);
        Mockito.when(operand.getName()).thenReturn("Operand");

        assertEquals("LiteralElement", JasminUtils.getElementName(literalElement));
        assertEquals("Operand", JasminUtils.getElementName(operand));
    }

    @Test
    public void buildLoadInstructions() {
        final Descriptor descriptor = Mockito.mock(Descriptor.class);
        Mockito.when(descriptor.getVirtualReg()).thenReturn(7);

        final HashMap<String, Descriptor> varTable = new HashMap<>();
        varTable.put("LiteralElement", descriptor);
        varTable.put("OperandElement", descriptor);

        final Method method = Mockito.mock(Method.class);
        Mockito.when(method.getVarTable()).thenReturn(varTable);

        final Type objectType = Mockito.mock(Type.class);
        Mockito.when(objectType.getTypeOfElement()).thenReturn(ElementType.THIS);
        final Operand objectElement = Mockito.mock(Operand.class);
        Mockito.when(objectElement.isLiteral()).thenReturn(false);
        Mockito.when(objectElement.getType()).thenReturn(objectType);
        Mockito.when(objectElement.getName()).thenReturn("OperandElement");

        final Type intType = Mockito.mock(Type.class);
        Mockito.when(intType.getTypeOfElement()).thenReturn(ElementType.INT32);
        final Operand intElement = Mockito.mock(Operand.class);
        Mockito.when(intElement.isLiteral()).thenReturn(false);
        Mockito.when(intElement.getType()).thenReturn(intType);
        Mockito.when(intElement.getName()).thenReturn("OperandElement");

        final Type arrayType = Mockito.mock(Type.class);
        Mockito.when(arrayType.getTypeOfElement()).thenReturn(ElementType.ARRAYREF);
        final Operand arrayElement = Mockito.mock(Operand.class);
        Mockito.when(arrayElement.isLiteral()).thenReturn(false);
        Mockito.when(arrayElement.getType()).thenReturn(arrayType);
        Mockito.when(arrayElement.getName()).thenReturn("OperandElement");

        assertEquals("ldc LiteralElement\n", JasminUtils.buildLoadInstructions(literalElement, method));
        assertEquals("aload 7\n", JasminUtils.buildLoadInstructions(objectElement, method));
        assertEquals("iload 7\n", JasminUtils.buildLoadInstructions(intElement, method));
        assertEquals("iaload 7\n", JasminUtils.buildLoadInstructions(arrayElement, method));
    }
}
