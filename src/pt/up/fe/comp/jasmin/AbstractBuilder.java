package pt.up.fe.comp.jasmin;

import org.specs.comp.ollir.ClassUnit;

public abstract class AbstractBuilder {
    protected final ClassUnit classUnit;
    protected final StringBuilder builder;

    public AbstractBuilder(final ClassUnit classUnit) {
        this.classUnit = classUnit;
        this.builder = new StringBuilder();
    }

    public abstract String compile();
}
