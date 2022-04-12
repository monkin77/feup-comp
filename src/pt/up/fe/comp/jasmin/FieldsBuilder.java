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

            builder.append(".field ").append(accessModifier).append(" ").append(field.getFieldName())
                    .append(" ").append(JasminUtils.getTypeName(field.getFieldType(), classUnit)).append("\n");
        }

        return builder.toString();
    }
}
