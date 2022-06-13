package pt.up.fe.comp.registerAllocation.coloring;

import java.util.ArrayList;

public class GraphColoring {
    ArrayList<NodeInterference> stackVisited;
    InterferenceGraph interferenceGraph;
    int k;
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
                if (this.stackVisited.contains(node)) continue;
                if (this.countRemainingEdges(node) < this.k) {
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

    /**
     * Colors the Graph, iterating the stack and assigning a color (register) to each node
     * @return true upon success. false otherwise.
     */
    public boolean coloring() {
        for (int i = this.stackVisited.size() - 1; i >= 0; i--) {
            NodeInterference node = this.stackVisited.get(i);
            int register = this.getAvailableColor(node);
            if (register == -1) return false;
            node.setRegister(register);
            this.stackVisited.remove(node);
        }
        return true;
    }

    /**
     * Gets the color for a given node
     * @return Register Number (color)
     */
    private int getAvailableColor(NodeInterference node) {
        ArrayList<Integer> usedColors = new ArrayList<>();

        // Add used colors from its edges
        for (NodeInterference edge : node.getEdges()) {
            if (edge.getRegister() != -1)   // -1 represents no colors assigned
                usedColors.add(edge.getRegister());
        }

        // Find an available color
        for (int i = 0; i < this.k; i++) {
            if (!usedColors.contains(i))
                return i;
        }

        return -1;
    }

    /**
     * Get the minimum number of JVM local variables required,
     * start by counting from k
     */
    public int getMinLocalVar() {
        while (true) {
            this.stackVisited.clear();
            if (this.buildStack() && this.coloring())
                return this.k;
            this.k++;
        }
    }
}
