package pt.up.fe.comp.ollir;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;

public abstract class AbstractBuilder {
    protected final JmmSemanticsResult semanticsResult;
    protected final StringBuilder builder;

    public AbstractBuilder(final JmmSemanticsResult semanticsResult) {
        this.semanticsResult = semanticsResult;
        this.builder = new StringBuilder();
    }

    public abstract String compile();
}
