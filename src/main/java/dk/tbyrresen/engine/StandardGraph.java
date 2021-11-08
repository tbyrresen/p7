package dk.tbyrresen.engine;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class StandardGraph<T> implements Graph<T> {
    private final Set<T> nodes;
    private final Set<Edge<T>> edges;
    private final Map<T, Set<T>> adjacentNodes;
    private final boolean allowsSelfLoops;
    private final boolean allowsParallelEdges;

    public StandardGraph(Set<T> nodes, Set<Edge<T>> edges) {
        this.nodes = nodes;
        this.edges = edges;
        adjacentNodes = findAdjacentNodes(nodes, edges);
        allowsSelfLoops = false;
        allowsParallelEdges = false;
    }

    private Map<T, Set<T>> findAdjacentNodes(Set<T> nodes, Set<Edge<T>> edges) {
        Map<T, Set<T>> adjacentNodesMap = new HashMap<>();
        nodes.forEach(n -> adjacentNodesMap.put(n, new HashSet<>()));
        edges.forEach(e -> {
            requireContainsNode(e.getSource());
            requireContainsNode(e.getTarget());
            adjacentNodesMap.get(e.getSource()).add(e.getTarget());
            adjacentNodesMap.get(e.getTarget()).add(e.getSource());
        });
        return adjacentNodesMap;
    }

    @Override
    public Set<T> getNodes() {
        return nodes;
    }

    @Override
    public Set<T> getAdjacentNodes(T node) {
        if (!nodes.contains(node)) {
            throw new IllegalArgumentException(String.format("Graph does not contain node %s", node));
        }
        return adjacentNodes.get(node);
    }

    @Override
    public Set<Edge<T>> getEdges() {
        return edges;
    }

    @Override
    public boolean allowsSelfLoops() {
        return allowsSelfLoops;
    }

    @Override
    public boolean allowsParallelEdges() {
        return allowsParallelEdges;
    }

    private void requireContainsNode(T node) {
        if (!nodes.contains(node)) {
            throw new IllegalArgumentException(String.format("Graph does not contain node %s", node));
        }
    }
}
