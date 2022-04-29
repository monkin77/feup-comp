package pt.up.fe.comp;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;

public class MySymbol extends Symbol {
    private final EntityTypes entity;

    /**
     *
     * @param type built-in type if exists
     * @param name name of the symbol
     * @param entity type of entity (variable, function, class...)
     */
    public MySymbol(Type type, String name, EntityTypes entity) {
        super(type, name);
        this.entity = entity;
    }

    @Override
    public String toString() {
        return "MySymbol [type=" + super.getType() + ", name=" + super.getName() + ", entityType=" + this.entity.toString() + "]";
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.getName() == null) ? 0 : this.getName().hashCode());
        result = prime * result + ((this.getType() == null) ? 0 : this.getType().hashCode());
        result = prime * result + ((this.getEntity() == null) ? 0 : this.getEntity().hashCode());
        return result;
    }

    public EntityTypes getEntity() {
        return entity;
    }
}
