package pt.up.fe.comp.jasmin;

import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.Field;

import java.util.ArrayList;

public class FieldsBuilder extends AbstractBuilder {
    public FieldsBuilder(final ClassUnit classUnit) {
        super(classUnit);
    }

    @Override
    public String compile() {
        final ArrayList<Field> fields = classUnit.getFields();
        for (final Field field : fields) {
            final String accessModifier = JasminUtils.getAccessModifier(field.getFieldAccessModifier());
            final String fieldType = JasminUtils.getTypeName(field.getFieldType(), classUnit, true);

            String fieldName = field.getFieldName();
            if (fieldName.equals("field")) fieldName = "'field'";

            builder.append(".field ").append(accessModifier).append(" ");

            if (field.isStaticField()) builder.append("static ");
            if (field.isFinalField()) builder.append("final ");

            builder.append(fieldName).append(" ").append(fieldType).append("\n");
        }

        return builder.toString();
    }
}
