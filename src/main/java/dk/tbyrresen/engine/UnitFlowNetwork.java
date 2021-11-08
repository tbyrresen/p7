package dk.tbyrresen.engine;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UnitFlowNetwork<T> implements Graph<T> {
    private final T originalSource;
    private final T originalTarget;
    private Set<T> sourceNodes = new HashSet<>();
    private Set<T> targetNodes = new HashSet<>();
    private final Map<T, Set<UnitFlowEdge<T>>> outEdges = new HashMap<>();  // Used semi undirected since we need to look from both directed in edmonds karp?

    // TODO Should we allow/disallow selfloops and/or paralleledges?
    public UnitFlowNetwork(Graph<T> graph, T source, T target) {
        if (source.equals(target)) {
            throw new IllegalArgumentException("Source and target must be distinct");
        }
        originalSource = source;
        originalTarget = target;
        sourceNodes.add(source);
        targetNodes.add(target);
        addNodes(graph.getNodes());
        addEdgesAsUnitFlowEdges(graph.getEdges());
    }

    private void addNodes(Set<T> nodes) {
        nodes.forEach(n -> outEdges.put(n, new HashSet<>()));
    }

    private void addEdgesAsUnitFlowEdges(Set<Edge<T>> edges) {
        for (var edge : edges) {
            var unitFlowEdge = new UnitFlowEdge<>(edge.getSource(), edge.getTarget());
            outEdges.get(edge.getSource()).add(unitFlowEdge);
            outEdges.get(edge.getTarget()).add(unitFlowEdge);
        }
    }

    public T getOriginalSource() {
        return originalSource;
    }

    public T getOriginalTarget() {
        return originalTarget;
    }

    public Set<UnitFlowEdge<T>> getOutEdges(T node) {
        requireContainsNode(node);
        return outEdges.get(node);
    }

    public boolean isSourceNode(T node) {
        requireContainsNode(node);
        return sourceNodes.contains(node);
    }

    public boolean isTargetNode(T node) {
        requireContainsNode(node);
        return targetNodes.contains(node);
    }

    public Set<T> getSourceNodes() {
        return sourceNodes;
    }

    public void setSourceNodes(Set<T> sourceNodes) {
        requireContainsNodes(sourceNodes);
        this.sourceNodes = sourceNodes;
    }

    public Set<T> getTargetNodes() {
        return targetNodes;
    }

    public void setTargetNodes(Set<T> targetNodes) {
        requireContainsNodes(targetNodes);
        this.targetNodes = targetNodes;
    }

    public void addToSource(T node) {
        requireContainsNode(node);
        sourceNodes.add(node);
    }

    public void addToTarget(T node) {
        requireContainsNode(node);
        targetNodes.add(node);
    }

    // reset flow for all edges in network
    public void resetFlow() {
        outEdges.values().stream().flatMap(Set::stream).forEach(UnitFlowEdge::resetFlow);
    }

    @Override
    public Set<T> getNodes() {
        return outEdges.keySet();
    }

    // must consider both the source and target of edges and filter out the node corresponding to the argument
    @Override
    public Set<T> getAdjacentNodes(T node) {
        requireContainsNode(node);
        return outEdges.get(node)
                .stream()
                .flatMap(e -> Stream.of(e.getSource(), e.getTarget()))
                .filter(n -> !node.equals(n))
                .collect(Collectors.toSet());
    }

    @Override
    public Set<Edge<T>> getEdges() {
        return outEdges.values().stream().flatMap(Set::stream).collect(Collectors.toSet());
    }

    @Override
    public boolean allowsSelfLoops() {
        return false;
    }

    @Override
    public boolean allowsParallelEdges() {
        return false;
    }

    private void requireContainsNode(T node) {
        if (!outEdges.containsKey(node)) {
            throw new IllegalArgumentException(String.format("Flow network does not contain node %s", node));
        }
    }

    private void requireContainsNodes(Set<T> nodes) {
        if (!outEdges.keySet().containsAll(nodes)) {
            throw new IllegalArgumentException(String.format("Flow network does not contain all nodes in %s", nodes));
        }
    }
}
