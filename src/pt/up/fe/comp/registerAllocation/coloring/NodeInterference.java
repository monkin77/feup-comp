package pt.up.fe.comp.registerAllocation.coloring;

import java.util.ArrayList;

public class NodeInterference {
    String value;
    ArrayList<NodeInterference> edges;
    int register;

    public NodeInterference(String value) {
        this.value = value;
        this.register = -1;
        this.edges = new ArrayList<>();
    }

    public void addEdge(NodeInterference nodeInterference) {
        this.edges.add(nodeInterference);
    }

    public void removeEdge(NodeInterference nodeInterference) {
        this.edges.remove(nodeInterference);
    }

    public String getValue() {
        return this.value;
    }

    public ArrayList<NodeInterference> getEdges() {
        return this.edges;
    }

    public int getRegister() {
        return this.register;
    }

    public void setRegister(int register) {
        this.register = register;
    }
}
