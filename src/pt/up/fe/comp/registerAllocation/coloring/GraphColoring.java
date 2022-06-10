package pt.up.fe.comp.registerAllocation.coloring;

import java.util.ArrayList;

public class GraphColoring {
    ArrayList<NodeInterference> stackVisited;
    InterferenceGraph interferenceGraph;
    int k;
    int allocated = 0;

    public GraphColoring(int k, InterferenceGraph interferenceGraph) {
        this.stackVisited = new ArrayList<>();
        this.interferenceGraph = interferenceGraph;
        this.k = k;
    }

    /**
     * Builds the stack with the nodes
     * @return true if possible to build to color the graph with k colors. False otherwise.
     */
    public boolean buildStack() {
        ArrayList<NodeInterference> nodeList = this.interferenceGraph.nodeList;
        boolean removedNode;

        while (this.stackVisited.size() != nodeList.size()) {
            removedNode = false;
            for (NodeInterference node : nodeList) {
                if (this.stackVisited.contains(node))continue;
                if (this.countRemainingEdges(node) <= k) {
                    this.stackVisited.add(node);
                    removedNode = true;
                }
            }
            if (!removedNode) return false;
        }

        return true;
    }

    /**
     * Calculates the number of edges of a node that are not in the stack (still interfering)
     * @param node
     * @return number of collisions
     */
    private int countRemainingEdges(NodeInterference node) {
        int edgesNotInStack = 0;
        for (NodeInterference child : node.getEdges())
            if (!this.stackVisited.contains(child)) edgesNotInStack++;
        return edgesNotInStack;
    }
}
