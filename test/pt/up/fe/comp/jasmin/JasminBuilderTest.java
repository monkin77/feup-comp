package pt.up.fe.comp.jasmin;

import static org.junit.Assert.assertEquals;
import static pt.up.fe.comp.jasmin.JasminConstants.DEFAULT_SUPERCLASS;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.specs.comp.ollir.ClassUnit;

import java.util.ArrayList;

public class JasminBuilderTest {
    private JasminBuilder jasminBuilder;
    private ClassUnit classUnit;
    private static MockedStatic<JasminUtils> mockedUtils;

    @BeforeClass
    public static void setupStatic() {
        mockedUtils = Mockito.mockStatic(JasminUtils.class);
        mockedUtils.when(() -> JasminUtils.getAccessModifier(Mockito.any())).thenReturn("public");
        mockedUtils.when(() -> JasminUtils.getFullClassName(Mockito.any(), Mockito.any())).thenReturn("ParentClass");
    }

    @AfterClass
    public static void teardown() {
        mockedUtils.close();
    }

    @Before
    public void setup() {
        classUnit = Mockito.mock(ClassUnit.class);
        Mockito.when(classUnit.getClassName()).thenReturn("MyClass");
        Mockito.when(classUnit.getFields()).thenReturn(new ArrayList<>());
        Mockito.when(classUnit.getMethods()).thenReturn(new ArrayList<>());

        jasminBuilder = new JasminBuilder(classUnit);
    }

    @Test
    public void defaultSuperClass() {
        assertEquals(".class public MyClass\n" +
                ".super " + DEFAULT_SUPERCLASS + "\n", jasminBuilder.compile());
    }

    @Test
    public void customSuperClass() {
        Mockito.when(classUnit.getSuperClass()).thenReturn("ParentClass");
        assertEquals(".class public MyClass\n" +
                ".super ParentClass\n", jasminBuilder.compile());
    }
}
