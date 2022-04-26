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

    public EntityTypes getEntity() {
        return entity;
    }
}
