package dk.tbyrresen.engine;

import org.apache.commons.lang3.tuple.ImmutablePair;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class GraphSeparator<T> {
    private static final double OPTIMAL_CUT_MAX_IMBALANCE = 0.6;
    private static final int NUM_RANDOM_FLOWCUTTER_RUNS = 20;
    private final Graph<T> graph;
    private final Set<T> separator;

    public GraphSeparator(Graph<T> graph, double epsilon) {
        this.graph = graph;
        var edgeCuts = computeCutSets(epsilon);
        var optimalCut = findOptimalCut(edgeCuts);
        separator = findSeparatorNodes(optimalCut);
    }

    private Set<EdgeCut<T>> computeCutSets(double epsilon) {
        Set<EdgeCut<T>> edgeCuts = new HashSet<>();
        var runsCompleted = 0;
        while (runsCompleted++ < NUM_RANDOM_FLOWCUTTER_RUNS) {
            var randomSourceAndTarget = getRandomSourceAndTarget(graph);
            var flowCutter = new FlowCutter<>(graph, randomSourceAndTarget.left, randomSourceAndTarget.right, epsilon);
            edgeCuts.addAll(flowCutter.getCuts());
        }
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
        return cuts
                .stream()
                .filter(c -> c.getImbalance() <= OPTIMAL_CUT_MAX_IMBALANCE)
                .min(Comparator.comparing(EdgeCut::getExpansionSize))
                .orElseThrow(() -> new IllegalStateException(
                        String.format("No suitable cut to choose as optimal cut using max cut imbalance heuristic of: %s",
                                      OPTIMAL_CUT_MAX_IMBALANCE)));
    }

    // Note that we do not need to compute this as an actual subgraph containing edges since we
    // only need the separator nodes for performing a subsequent split of the graph. This keeps it more efficient.
    private Set<T> findSeparatorNodes(EdgeCut<T> edgeCut) {
        var numNodesInCut = edgeCut.getNodesInCut().size();
        var numNodesNotInCut = graph.getNodes().size() - numNodesInCut;
        var separatorNodes = new HashSet<T>();
        for (var edge : edgeCut.getCutEdges()) {
            if (!(separatorNodes.contains(edge.getSource()) || separatorNodes.contains(edge.getTarget()))) {
                if (numNodesInCut <= numNodesNotInCut) {
                    separatorNodes.add(getSeparatorNode(edge, edgeCut, false));
                    numNodesInCut--;
                } else {
                    separatorNodes.add(getSeparatorNode(edge, edgeCut, true));
                    numNodesNotInCut--;
                }
            }
        }
        return separatorNodes;
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

    public Set<Graph<T>> separate() {
        var separatedNodes = graph.getNodes()
                .stream()
                .filter(n -> !separator.contains(n))
                .collect(Collectors.toSet());

        var separatedNodesEdges = graph.getEdges()
                .stream()
                .filter(e -> separatedNodes.contains(e.getSource()) && separatedNodes.contains(e.getTarget()))
                .collect(Collectors.toSet());

        return GraphUtils.findConnectedComponents(new StandardGraph<>(separatedNodes, separatedNodesEdges));
    }

    public Set<T> getSeparator() {
        return separator;
    }
}