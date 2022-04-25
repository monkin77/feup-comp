package pt.up.fe.comp.entity;

import pt.up.fe.comp.jmm.analysis.table.Symbol;

public class Entity {
    private final Symbol symbol;
    private final EntityTypes type;

    public Entity(Symbol symbol, EntityTypes type) {
        this.symbol = symbol;
        this.type = type;
    }

    public Symbol getSymbol() {
        return symbol;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return symbol.hashCode();
    }
}
