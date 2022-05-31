package pt.up.fe.comp.jasmin;

import static org.junit.Assert.assertEquals;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.Element;
import org.specs.comp.ollir.Method;

import java.util.ArrayList;

public class MethodsBuilderTest {
    private MethodsBuilder methodsBuilder;
    private ArrayList<Method> methods;
    private ArrayList<Element> params;
    private Method method;
    private static MockedStatic<JasminUtils> mockedUtils;

    @BeforeClass
    public static void setupStatic() {
        mockedUtils = Mockito.mockStatic(JasminUtils.class);
        mockedUtils.when(() -> JasminUtils.getAccessModifier(Mockito.any())).thenReturn("public");
        mockedUtils.when(() -> JasminUtils.getTypeName(Mockito.any(), Mockito.any())).thenReturn("T");
        mockedUtils.when(() -> JasminUtils.getTypeName(Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn("T");
    }

    @AfterClass
    public static void teardown() {
        mockedUtils.close();
    }

    @Before
    public void setup() {
        params = new ArrayList<>();

        method = Mockito.mock(Method.class);
        Mockito.when(method.isStaticMethod()).thenReturn(false);
        Mockito.when(method.isFinalMethod()).thenReturn(false);
        Mockito.when(method.isConstructMethod()).thenReturn(false);
        Mockito.when(method.getMethodName()).thenReturn("methodName");
        Mockito.when(method.getParams()).thenReturn(params);
        Mockito.when(method.getInstructions()).thenReturn(new ArrayList<>());

        methods = new ArrayList<>();
        methods.add(method);

        ClassUnit classUnit = Mockito.mock(ClassUnit.class);
        Mockito.when(classUnit.getClassName()).thenReturn("MyClassUnit");
        Mockito.when(classUnit.getMethods()).thenReturn(methods);

        methodsBuilder = new MethodsBuilder(classUnit);
    }

    @Test
    public void methodScaffold() {
        assertEquals("""
                .method public methodName()T
                .limit stack 0
                .limit locals 1
                    return
                .end method

                """, methodsBuilder.compile());
    }

    @Test
    public void staticMethod() {
        Mockito.when(method.isStaticMethod()).thenReturn(true);
        assertEquals("""
                .method public static methodName()T
                .limit stack 0
                .limit locals 0
                    return
                .end method

                """, methodsBuilder.compile());
    }

    @Test
    public void finalMethod() {
        Mockito.when(method.isFinalMethod()).thenReturn(true);
        assertEquals("""
                .method public final methodName()T
                .limit stack 0
                .limit locals 1
                    return
                .end method

                """, methodsBuilder.compile());
    }

    @Test
    public void staticFinalMethod() {
        Mockito.when(method.isStaticMethod()).thenReturn(true);
        Mockito.when(method.isFinalMethod()).thenReturn(true);
        assertEquals("""
                .method public static final methodName()T
                .limit stack 0
                .limit locals 0
                    return
                .end method

                """, methodsBuilder.compile());
    }

    @Test
    public void constructMethod() {
        Mockito.when(method.isConstructMethod()).thenReturn(true);
        assertEquals("""
                .method public <init>()T
                .limit stack 0
                .limit locals 1
                    return
                .end method

                """, methodsBuilder.compile());
    }

    @Test
    public void methodWithParameters() {
        final Element mockedElement = Mockito.mock(Element.class);
        params.add(mockedElement); params.add(mockedElement);
        assertEquals("""
                .method public methodName(TT)T
                .limit stack 0
                .limit locals 1
                    return
                .end method

                """, methodsBuilder.compile());
    }

    @Test
    public void multipleMethods() {
        final Method newMethod = Mockito.mock(Method.class);
        Mockito.when(newMethod.isStaticMethod()).thenReturn(false);
        Mockito.when(newMethod.isFinalMethod()).thenReturn(true);
        Mockito.when(newMethod.isConstructMethod()).thenReturn(false);
        Mockito.when(newMethod.getMethodName()).thenReturn("newMethodName");
        Mockito.when(newMethod.getParams()).thenReturn(params);
        Mockito.when(newMethod.getInstructions()).thenReturn(new ArrayList<>());

        methods.add(newMethod);

        assertEquals("""
                .method public methodName()T
                .limit stack 0
                .limit locals 1
                    return
                .end method

                .method public final newMethodName()T
                .limit stack 0
                .limit locals 1
                    return
                .end method

                """, methodsBuilder.compile());
    }
}
