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
        assertEquals(".method public methodName()T\n" +
                ".limit stack 99\n" +
                ".limit locals 99\n" +
                "    return\n" +
                ".end method\n\n", methodsBuilder.compile());
    }

    @Test
    public void staticMethod() {
        Mockito.when(method.isStaticMethod()).thenReturn(true);
        assertEquals(".method public static methodName()T\n" +
                ".limit stack 99\n" +
                ".limit locals 99\n" +
                "    return\n" +
                ".end method\n\n", methodsBuilder.compile());
    }

    @Test
    public void finalMethod() {
        Mockito.when(method.isFinalMethod()).thenReturn(true);
        assertEquals(".method public final methodName()T\n" +
                ".limit stack 99\n" +
                ".limit locals 99\n" +
                "    return\n" +
                ".end method\n\n", methodsBuilder.compile());
    }

    @Test
    public void staticFinalMethod() {
        Mockito.when(method.isStaticMethod()).thenReturn(true);
        Mockito.when(method.isFinalMethod()).thenReturn(true);
        assertEquals(".method public static final methodName()T\n" +
                ".limit stack 99\n" +
                ".limit locals 99\n" +
                "    return\n" +
                ".end method\n\n", methodsBuilder.compile());
    }

    @Test
    public void constructMethod() {
        Mockito.when(method.isConstructMethod()).thenReturn(true);
        assertEquals(".method public <init>()T\n" +
                ".limit stack 99\n" +
                ".limit locals 99\n" +
                "    return\n" +
                ".end method\n\n", methodsBuilder.compile());
    }

    @Test
    public void methodWithParameters() {
        final Element mockedElement = Mockito.mock(Element.class);
        params.add(mockedElement); params.add(mockedElement);
        assertEquals(".method public methodName(TT)T\n" +
                ".limit stack 99\n" +
                ".limit locals 99\n" +
                "    return\n" +
                ".end method\n\n", methodsBuilder.compile());
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

        assertEquals(".method public methodName()T\n" +
                ".limit stack 99\n" +
                ".limit locals 99\n" +
                "    return\n" +
                ".end method\n\n" +
                ".method public final newMethodName()T\n" +
                ".limit stack 99\n" +
                ".limit locals 99\n" +
                "    return\n" +
                ".end method\n\n", methodsBuilder.compile());
    }
}
