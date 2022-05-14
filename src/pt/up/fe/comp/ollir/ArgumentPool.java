package pt.up.fe.comp.ollir;

public class ArgumentPool {
    private final String type;
    private final boolean isNotTerminal;
    private final String id;

    public ArgumentPool(String type, boolean isNotTerminal) {
        this(type, isNotTerminal, null);
    }

    public ArgumentPool(String id) {
        this(null, false, id);
    }

    public ArgumentPool() { this(null, false, null); }

    private ArgumentPool(String type, boolean isNotTerminal, String id) {
        this.type = type;
        this.isNotTerminal = isNotTerminal;
        this.id = id;
    }


    public String getType() {
        return type;
    }

    public boolean getIsNotTerminal() {
        return isNotTerminal;
    }

    public String getId() {
        return id;
    }
}
