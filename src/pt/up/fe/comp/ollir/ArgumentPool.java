package pt.up.fe.comp.ollir;

public class ArgumentPool {
    private final String type;
    private final boolean isNotTerminal;
    private final String id;
    private String returnType;
    private String assignmentType;
    private boolean isTarget;

    public ArgumentPool(String type, boolean isNotTerminal) {
        this(type, isNotTerminal, null, null);
    }

    public ArgumentPool(String id) {
        this(null, false, id, null);
    }

    public ArgumentPool() {
        this(null, false, null, null);
    }

    private ArgumentPool(String type, boolean isNotTerminal, String id, String returnType) {
        this.type = type;
        this.isNotTerminal = isNotTerminal;
        this.id = id;
        this.returnType = returnType;
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

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public void setAssignmentType(String assignmentType) {
        this.assignmentType = assignmentType;
    }

    public String getAssignmentType() {
        return assignmentType;
    }

    public boolean isTarget() {
        return isTarget;
    }

    public void setTarget(boolean target) {
        isTarget = target;
    }
}
