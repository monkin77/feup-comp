package pt.up.fe.comp.registerAllocation.coloring;

import java.util.ArrayList;
import java.util.HashMap;

public class InterferenceGraph {
    HashMap<String, ArrayList<String>> interferenceRelations;
    ArrayList<NodeInterference> nodeList;

    public InterferenceGraph(HashMap<String, ArrayList<String>> interference) {
        this.interferenceRelations = interference;
        this.nodeList = new ArrayList<>();
        this.buildNodes();
        this.buildEdges();
    }

    private void buildNodes() {
        for (String value : this.interferenceRelations.keySet())
            this.nodeList.add(new NodeInterference(value));
    }

    private void buildEdges() {
        for (NodeInterference node : this.nodeList)
            for (String value : this.interferenceRelations.get(node.getValue()))
                node.addEdge(this.getNode(value));
    }

    /**
     * Gets the NodeInterference of a certain node identified by its value
     * @param value value of the NodeInterference
     */
    private NodeInterference getNode(String value) {
        for (NodeInterference node : this.nodeList)
            if (node.getValue().equals(value))
                return node;
        return null;
    }

    public ArrayList<NodeInterference> getNodeList() {
        return nodeList;
    }
}
