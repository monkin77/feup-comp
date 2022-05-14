package pt.up.fe.comp.ollir;

public class ArgumentPool {
    private final String type;
    private final Boolean isNotTerminal;
    private final String id;

    public ArgumentPool(String type, Boolean isNotTerminal) {
        this(type, isNotTerminal, null);
    }

    public ArgumentPool(String id) {
        this(null, null, id);
    }

    private ArgumentPool(String type, Boolean isNotTerminal, String id) {
        this.type = type;
        this.isNotTerminal = isNotTerminal;
        this.id = id;
    }


    public String getType() {
        return type;
    }

    public Boolean getIsNotTerminal() {
        return isNotTerminal;
    }

    public String getId() {
        return id;
    }
}
