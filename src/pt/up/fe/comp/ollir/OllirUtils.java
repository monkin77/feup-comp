package pt.up.fe.comp.ollir;

import pt.up.fe.comp.jmm.analysis.table.Type;

public class OllirUtils {
    public static String convertType(Type symbolType) {
        StringBuilder stringBuilder = new StringBuilder();

        if (symbolType.isArray()) {
            stringBuilder.append("array.");
        }

        String javaType = symbolType.getName();
        switch (javaType) {
            case "int":
                stringBuilder.append("i32");
                break;
            case "boolean":
                stringBuilder.append("bool");
                break;
            case "string":
                stringBuilder.append("String");
                break;
            case "void":
                stringBuilder.append("V");
                break;
            default:
                stringBuilder.append(javaType); // Custom class
        }

        return stringBuilder.toString();
    }
}
