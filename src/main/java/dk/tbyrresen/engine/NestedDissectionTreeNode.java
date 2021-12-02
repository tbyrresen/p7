package dk.tbyrresen.engine;

import org.springframework.lang.Nullable;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

// We are always building from lower nodes to higher nodes in the tree
public class NestedDissectionTreeNode<T> {
    private final Set<T> dissectionNodes;   // Separator nodes if non leaf, otherwise a tree or clique
    private final Set<Edge<T>> dissectionEdges;
    @Nullable private NestedDissectionTreeNode<T> parent;
    @Nullable private SeparationSide separationSide;
    private final Set<NestedDissectionTreeNode<T>> children = new HashSet<>();
    private Set<Edge<T>> edgesToChildren = new HashSet<>();
    private int leftTreeSize = 0;
    private int rightTreeSize = 0;
    private int numDirtyNodes = 0;
    private final int depth;

    public NestedDissectionTreeNode(Set<T> dissectionNodes,
                                    Set<Edge<T>> dissectionEdges,
                                    int depth) {
        this.dissectionNodes = dissectionNodes;
        this.dissectionEdges = dissectionEdges;
        this.depth = depth;
    }

    public NestedDissectionTreeNode(Set<T> dissectionNodes,
                                    Set<Edge<T>> dissectionEdges,
                                    Set<Edge<T>> edgesToChildren,
                                    int leftTreeSize,
                                    int rightTreeSize,
                                    int depth) {
        this(dissectionNodes, dissectionEdges, depth);
        this.leftTreeSize = leftTreeSize;
        this.rightTreeSize = rightTreeSize;
        this.edgesToChildren = edgesToChildren;
    }

    public NestedDissectionTreeNode(Set<T> dissectionNodes,
                                    Set<Edge<T>> dissectionEdges,
                                    NestedDissectionTreeNode<T> parent,
                                    SeparationSide separationSide,
                                    int depth) {
        this(dissectionNodes, dissectionEdges, depth);
        this.parent = parent;
        this.separationSide = separationSide;
    }

    public NestedDissectionTreeNode(Set<T> dissectionNodes,
                                    Set<Edge<T>> dissectionEdges,
                                    NestedDissectionTreeNode<T> parent,
                                    Set<Edge<T>> edgesToChildren,
                                    SeparationSide separationSide,
                                    int leftTreeSize,
                                    int rightTreeSize,
                                    int depth) {
        this(dissectionNodes, dissectionEdges, edgesToChildren, leftTreeSize, rightTreeSize, depth);
        this.parent = parent;
        this.separationSide = separationSide;
    }

    @Nullable
    public NestedDissectionTreeNode<T> getParent() {
        return parent;
    }

    public void addChild(NestedDissectionTreeNode<T> node) {
        children.add(node);
    }

    public Set<NestedDissectionTreeNode<T>> getChildren() {
        return children;
    }

    public Set<T> getDissectionNodes() {
        return dissectionNodes;
    }

    public void addDissectionNode(T node) {
        dissectionNodes.add(node);
        numDirtyNodes++;
    }

    public void addDissectionEdge(Edge<T> edge) {
        dissectionEdges.add(edge);
    }

    public Set<Edge<T>> getDissectionEdges() {
        return dissectionEdges;
    }

    public int getDepth() {
        return depth;
    }

    public void addEdgeToChildren(Edge<T> edge) {
        edgesToChildren.add(edge);
    }

    public Set<Edge<T>> getEdgesToChildren() {
        return edgesToChildren;
    }

    @Nullable
    public SeparationSide getSeparationSide() {
        return separationSide;
    }

    public boolean isBalanced() {
        var totalSize = dissectionNodes.size() + leftTreeSize + rightTreeSize;
        return ((((double) Math.max(leftTreeSize, rightTreeSize) * 2) / totalSize) - 1) <=
               DissectionConstants.OPTIMAL_CUT_MAX_IMBALANCE;
    }

    public void removeChild(NestedDissectionTreeNode<T> child) {
        children.remove(child);
    }

    public void incrementTreeSize(@Nullable SeparationSide separationSide) {
        if (separationSide == null) {
            throw new NullPointerException("Cannot increment tree size when separation side is null");
        }
        if (separationSide == SeparationSide.LEFT) {
            leftTreeSize++;
        } else {
            rightTreeSize++;
        }
    }

    public int getNumDirtyNodes() {
        return numDirtyNodes;
    }

    // TODO this equals method should be enough.. is it proper?
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NestedDissectionTreeNode<?> that = (NestedDissectionTreeNode<?>) o;
        return dissectionNodes.equals(that.dissectionNodes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dissectionNodes);
    }
}
