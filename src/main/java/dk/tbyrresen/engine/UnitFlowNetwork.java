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
    private final Map<T, Set<MultiFlowEdge<T>>> outEdges = new HashMap<>();  // Used semi undirected since we need to look from both directed in edmonds karp?

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
            var multiFlowEdge = new MultiFlowEdge<>(edge.getSource(), edge.getTarget());
            outEdges.get(edge.getSource()).add(multiFlowEdge);
            outEdges.get(edge.getTarget()).add(multiFlowEdge);
        }
    }

    public T getOriginalSource() {
        return originalSource;
    }

    public T getOriginalTarget() {
        return originalTarget;
    }

    // This seems a bit odd, but we only need to return the first (or second) edge due
    // to how the later steps are implemented.
    public Set<MultiFlowEdge<T>> getOutEdges(T node) {
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
                .map(MultiFlowEdge::getFirst)
                .flatMap(e -> Stream.of(e.getSource(), e.getTarget()))
                .filter(n -> !node.equals(n))
                .collect(Collectors.toSet());
    }

    @Override
    public void addEdge(Edge<T> edge) {
        throw new UnsupportedOperationException(); // TODO this is dumb. Should probably make flow network inherent an immutable graph type
    }

    @Override
    public Set<Edge<T>> getEdges() {
        return outEdges.values()
                .stream()
                .flatMap(Set::stream)
                .flatMap(e -> Stream.of(e.getFirst(), e.getSecond()))
                .collect(Collectors.toSet());
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
