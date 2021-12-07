package dk.tbyrresen.engine;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.MutablePair;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class GraphSeparator<T> {
    private static final int NUM_RANDOM_FLOWCUTTER_RUNS = 10;
    private final Graph<T> graph;
    private final Separator<T> separator;

    public GraphSeparator(Graph<T> graph, double epsilon) {
        this.graph = graph;
        var edgeCuts = computeCutSets(epsilon);
        var optimalCut = findOptimalCut(edgeCuts);
        separator = findSeparator(optimalCut);
    }

    private Set<EdgeCut<T>> computeCutSets(double epsilon) {
        Set<EdgeCut<T>> edgeCuts = new HashSet<>();
        Set<ImmutablePair<T, T>> randomPairs = new HashSet<>();
        for (int i = 0; i < NUM_RANDOM_FLOWCUTTER_RUNS; i++) {
            randomPairs.add(getRandomSourceAndTarget(graph));
        }
        randomPairs.parallelStream().forEach(p -> {
            var flowCutter = new FlowCutter<>(graph, p.left, p.right, epsilon);
            edgeCuts.addAll(flowCutter.getCuts());
        });
        return edgeCuts;
    }

    private ImmutablePair<T, T> getRandomSourceAndTarget(Graph<T> graph) {
        var random = new SecureRandom();
        var graphNodes = graph.getNodes();
        var sourceIdx = random.nextInt(graphNodes.size());
        var targetIdx = random.nextInt(graphNodes.size());
        if (targetIdx == sourceIdx) {
            targetIdx = (targetIdx + 1) % graphNodes.size(); // Ensure distinct source and target nodes
        }
        var nodesList = new ArrayList<>(graphNodes);
        return new ImmutablePair<>(nodesList.get(sourceIdx), nodesList.get(targetIdx));
    }

    private EdgeCut<T> findOptimalCut(Set<EdgeCut<T>> cuts) {
        var optimalUsingMaxImbalance = cuts
                .stream()
                .filter(c -> c.getImbalance() <= DissectionConstants.OPTIMAL_CUT_MAX_IMBALANCE)
                .min(Comparator.comparing(EdgeCut::getExpansionSize));

        // TODO should we order these by imbalance? at least check if this ever happens on real world graphs
        return optimalUsingMaxImbalance.orElseGet(() -> cuts
                .stream()
                .min(Comparator.comparing(EdgeCut::getExpansionSize))
                .orElseThrow(() -> new IllegalStateException("No cut to choose as optimal one")));
    }

    private Separator<T> findSeparator(EdgeCut<T> edgeCut) {
        var nodesInCut = edgeCut.getNodesInCut();
        var nodesNotInCut = graph.getNodes()
                .stream()
                .filter(n -> !nodesInCut.contains(n))
                .collect(Collectors.toSet());
        var separatorNodes = new HashSet<T>();
        for (var edge : edgeCut.getCutEdges()) {
            if (!(separatorNodes.contains(edge.getSource()) || separatorNodes.contains(edge.getTarget()))) {
                if (nodesInCut.size() <= nodesNotInCut.size()) {
                    var separatorNode = getSeparatorNode(edge, edgeCut, false);
                    separatorNodes.add(separatorNode);
                    nodesNotInCut.remove(separatorNode);
                } else {
                    var separatorNode = getSeparatorNode(edge, edgeCut, true);
                    separatorNodes.add(separatorNode);
                    nodesInCut.remove(separatorNode);
                }
            }
        }
        var separatingEdges = findSeparatingEdges(separatorNodes);
        return new Separator<>(separatorNodes, nodesInCut, nodesNotInCut, separatingEdges);
    }

    private T getSeparatorNode(Edge<T> edge, EdgeCut<T> edgeCut, boolean extractFromCut) {
        if (edgeCut.getNodesInCut().contains(edge.getSource())) {
            if (extractFromCut) {
                return edge.getSource();
            }
            return edge.getTarget();
        } else {
            if (extractFromCut) {
                return edge.getTarget();
            }
            return edge.getSource();
        }
    }

    // returns all the edges that goes from the separator to the separated subgraphs
    private Set<Edge<T>> findSeparatingEdges(Set<T> separatorNodes) {
        return graph.getEdges()
                .stream()
                .filter(e -> (separatorNodes.contains(e.getSource()) && !separatorNodes.contains(e.getTarget()))
                          || (!separatorNodes.contains(e.getSource()) && separatorNodes.contains(e.getTarget())))
                .collect(Collectors.toSet());
    }

    // TODO figure out if we can do this immutable
    public MutablePair<Set<Graph<T>>, Set<Graph<T>>> separate() {
        Set<Edge<T>> leftSeparatedEdges = GraphUtils.extractSubGraphEdges(graph, separator.getLeftSeparatedNodes());
        Set<Edge<T>> rightSeparatedEdges = GraphUtils.extractSubGraphEdges(graph, separator.getRightSeparatedNodes());
        var leftConnectedComponents = GraphUtils.findConnectedComponents(
                new StandardGraph<>(separator.getLeftSeparatedNodes(), leftSeparatedEdges));
        var rightConnectedComponents = GraphUtils.findConnectedComponents(
                new StandardGraph<>(separator.getRightSeparatedNodes(), rightSeparatedEdges));
        return MutablePair.of(leftConnectedComponents, rightConnectedComponents);
    }

    public Separator<T> getSeparator() {
        return separator;
    }
}