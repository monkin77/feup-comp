package pt.up.fe.comp.visitors;

import pt.up.fe.specs.util.SpecsEnums;

public enum CalculatorOps {
    ADD("+"),
    SUB("-"),
    MUL("*"),
    DIV("/"),
    NEG("-"),
    EQ("=");

    private final String code;

    CalculatorOps(String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return code;
    }

    static CalculatorOps fromName(String name) {
        return SpecsEnums.fromName(CalculatorOps.class, name);
    }
}
