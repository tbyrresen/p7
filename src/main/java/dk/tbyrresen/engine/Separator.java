package dk.tbyrresen.engine;

import java.util.Set;

// todo this is by definition not a separator? - could just call it a dissection?
public class Separator<T> {
    private final Set<T> separatorNodes;
    private final Set<T> leftSeparatedNodes;
    private final Set<T> rightSeparatedNodes;
    private final Set<Edge<T>> separatingEdges; // all edges separating the separator nodes from left and right
    private final double imbalance;

    public Separator(
            Set<T> separatorNodes,
            Set<T> leftSeparatedNodes,
            Set<T> rightSeparatedNodes,
            Set<Edge<T>> separatingEdges) {
        this.separatorNodes = separatorNodes;
        this.leftSeparatedNodes = leftSeparatedNodes;
        this.rightSeparatedNodes = rightSeparatedNodes;
        this.separatingEdges = separatingEdges;
        imbalance = computeImbalance();
    }

    private double computeImbalance() {
        var totalSize = separatorNodes.size() + leftSeparatedNodes.size() + rightSeparatedNodes.size();
        var leftSize = leftSeparatedNodes.size();
        var rightSize = rightSeparatedNodes.size();
        return (((double) Math.max(leftSize, rightSize) * 2) / totalSize) - 1;
    }

    public Set<T> getSeparatorNodes() {
        return separatorNodes;
    }

    public Set<T> getLeftSeparatedNodes() {
        return leftSeparatedNodes;
    }

    public Set<T> getRightSeparatedNodes() {
        return rightSeparatedNodes;
    }

    public Set<Edge<T>> getSeparatingEdges() {
        return separatingEdges;
    }

    public double getImbalance() {
        return imbalance;
    }
}
