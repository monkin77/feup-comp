package pt.up.fe.comp.jasmin;

import static org.junit.Assert.assertEquals;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.Field;

import java.util.ArrayList;
import java.util.Collections;

public class FieldsBuilderTest {
    private Field field;
    private FieldsBuilder fieldsBuilder;
    private ArrayList<Field> fields;
    private static MockedStatic<JasminUtils> mockedUtils;

    @BeforeClass
    public static void setupStatic() {
        mockedUtils = Mockito.mockStatic(JasminUtils.class);
        mockedUtils.when(() -> JasminUtils.getAccessModifier(Mockito.any())).thenReturn("public");
        mockedUtils.when(() -> JasminUtils.getTypeName(Mockito.any(), Mockito.any())).thenReturn("fieldType");
    }

    @AfterClass
    public static void teardown() {
        mockedUtils.close();
    }

    @Before
    public void setup() {
        field = Mockito.mock(Field.class);
        Mockito.when(field.getFieldName()).thenReturn("fieldName");
        Mockito.when(field.isFinalField()).thenReturn(false);
        Mockito.when(field.isStaticField()).thenReturn(false);

        fields = new ArrayList<>(Collections.singletonList(field));

        ClassUnit classUnit = Mockito.mock(ClassUnit.class);
        Mockito.when(classUnit.getClassName()).thenReturn("MyClassUnit");
        Mockito.when(classUnit.getFields()).thenReturn(fields);

        fieldsBuilder = new FieldsBuilder(classUnit);
    }

    @Test
    public void fieldNameIsField() {
        Mockito.when(field.getFieldName()).thenReturn("field");
        assertEquals(".field public 'field' fieldType\n", fieldsBuilder.compile());
    }

    @Test
    public void staticField() {
        Mockito.when(field.isStaticField()).thenReturn(true);
        assertEquals(".field public static fieldName fieldType\n", fieldsBuilder.compile());
    }

    @Test
    public void finalField() {
        Mockito.when(field.isFinalField()).thenReturn(true);
        assertEquals(".field public final fieldName fieldType\n", fieldsBuilder.compile());
    }

    @Test
    public void finalStaticField() {
        Mockito.when(field.isStaticField()).thenReturn(true);
        Mockito.when(field.isFinalField()).thenReturn(true);
        assertEquals(".field public static final fieldName fieldType\n", fieldsBuilder.compile());
    }

    @Test
    public void multipleFields() {
        final Field newField = Mockito.mock(Field.class);
        Mockito.when(newField.getFieldName()).thenReturn("newFieldName");
        Mockito.when(newField.isFinalField()).thenReturn(true);
        Mockito.when(newField.isStaticField()).thenReturn(false);
        fields.add(newField);

        assertEquals(".field public fieldName fieldType\n" +
                ".field public final newFieldName fieldType\n", fieldsBuilder.compile());
    }
}
