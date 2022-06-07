package pt.up.fe.comp.jasmin.instruction;

import pt.up.fe.comp.jasmin.MethodsBuilder;

public class InstructionList {
    public static String aload(int reg) {
        MethodsBuilder.updateStackLimit(1);
        return "aload" + (reg <= 3 ? "_" : " ") + reg;
    }

    public static String iload(int reg) {
        MethodsBuilder.updateStackLimit(1);
        return "iload" + (reg <= 3 ? "_" : " ") + reg;
    }

    public static String iaload() {
        MethodsBuilder.updateStackLimit(-1);
        return "iaload";
    }

    public static String aaload() {
        MethodsBuilder.updateStackLimit(-1);
        return "aaload";
    }

    public static String astore(int reg) {
        MethodsBuilder.updateStackLimit(-1);
        return "astore" + (reg <= 3 ? "_" : " ") + reg;
    }

    public static String istore(int reg) {
        MethodsBuilder.updateStackLimit(-1);
        return "istore" + (reg <= 3 ? "_" : " ") + reg;
    }

    public static String iastore() {
        MethodsBuilder.updateStackLimit(-3);
        return "iastore";
    }

    public static String aastore() {
        MethodsBuilder.updateStackLimit(-3);
        return "aastore";
    }

    public static String loadIntConstant(int value) {
        MethodsBuilder.updateStackLimit(1);
        if (value == -1) return "iconst_m1";
        else if (value >= 0 && value <= 5) return "iconst_" + value;
        else if (value >= -128 && value <= 127) return "bipush " + value;
        else if (value >= -32768 && value <= 32767) return "sipush " + value;
        else return ldc(Integer.toString(value));
    }

    public static String ldc(String value) {
        MethodsBuilder.updateStackLimit(1);
        return "ldc " + value;
    }

    public static String arraylength() {
        return "arraylength";
    }

    public static String newarray(String type) {
        return "newarray " + type;
    }

    public static String newInstruction(String type) {
        MethodsBuilder.updateStackLimit(1);
        return "new " + type;
    }

    public static String ifne(String label) {
        MethodsBuilder.updateStackLimit(-1);
        return "ifne " + label;
    }

    public static String getfield(String className, String fieldName, String fieldType) {
        return "getfield " + className + "/" + fieldName + " " + fieldType;
    }

    public static String putfield(String className, String fieldName, String fieldType) {
        MethodsBuilder.updateStackLimit(-2);
        return "putfield " + className + "/" + fieldName + " " + fieldType;
    }

    public static String gotoInstruction(String label) {
        return "goto " + label;
    }

    public static String if_icmpeq(String label) {
        MethodsBuilder.updateStackLimit(-2);
        return "if_icmpeq " + label;
    }

    public static String if_icmpge(String label) {
        MethodsBuilder.updateStackLimit(-2);
        return "if_icmpge " + label;
    }

    public static String if_icmpgt(String label) {
        MethodsBuilder.updateStackLimit(-2);
        return "if_icmpgt " + label;
    }

    public static String if_icmple(String label) {
        MethodsBuilder.updateStackLimit(-2);
        return "if_icmple " + label;
    }

    public static String if_icmplt(String label) {
        MethodsBuilder.updateStackLimit(-2);
        return "if_icmplt " + label;
    }

    public static String if_icmpne(String label) {
        MethodsBuilder.updateStackLimit(-2);
        return "if_icmpne " + label;
    }

    public static String iadd() {
        MethodsBuilder.updateStackLimit(-1);
        return "iadd";
    }

    public static String isub() {
        MethodsBuilder.updateStackLimit(-1);
        return "isub";
    }

    public static String imul() {
        MethodsBuilder.updateStackLimit(-1);
        return "imul";
    }

    public static String idiv() {
        MethodsBuilder.updateStackLimit(-1);
        return "idiv";
    }

    public static String ixor() {
        MethodsBuilder.updateStackLimit(-1);
        return "ixor";
    }

    public static String ireturn() {
        MethodsBuilder.updateStackLimit(-1);
        return "ireturn";
    }

    public static String areturn() {
        MethodsBuilder.updateStackLimit(-1);
        return "areturn";
    }

    public static String returnInstruction() {
        return "return";
    }
}
