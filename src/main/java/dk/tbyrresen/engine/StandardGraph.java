package dk.tbyrresen.engine;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    // Copy constructor
    public StandardGraph(Graph<T> graph) {
        nodes = new HashSet<>(graph.getNodes());
        edges = new HashSet<>(graph.getEdges());
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
    public void addEdge(Edge<T> edge) {
        edges.add(edge);
        nodes.add(edge.getSource());    // TODO is this proper behaviour?
        nodes.add(edge.getTarget());
        if (adjacentNodes.containsKey(edge.getSource())) {
            adjacentNodes.get(edge.getSource()).add(edge.getTarget());
        } else {
            adjacentNodes.put(edge.getSource(), new HashSet<>(List.of(edge.getTarget())));
        }
        if (adjacentNodes.containsKey(edge.getTarget())) {
            adjacentNodes.get(edge.getTarget()).add(edge.getSource());
        } else {
            adjacentNodes.put(edge.getTarget(), new HashSet<>(List.of(edge.getSource())));
        }
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        StandardGraph<?> that = (StandardGraph<?>) o;
        return allowsSelfLoops == that.allowsSelfLoops
               && allowsParallelEdges == that.allowsParallelEdges
               && nodes.equals(that.nodes)
               && edges.equals(that.edges);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodes, edges, allowsSelfLoops, allowsParallelEdges);
    }
}
