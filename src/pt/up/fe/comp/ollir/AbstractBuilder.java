package pt.up.fe.comp.ollir;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;

public abstract class AbstractBuilder {
    protected final SymbolTable symbolTable;
    protected final StringBuilder builder;

    public AbstractBuilder(final SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        this.builder = new StringBuilder();
    }

    public abstract String compile();
}
