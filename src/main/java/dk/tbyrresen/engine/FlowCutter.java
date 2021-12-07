package dk.tbyrresen.engine;

import org.apache.commons.collections4.SetUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

public class FlowCutter<T> {
    private enum CutSide {
        SOURCE,
        TARGET
    }

    private final UnitFlowNetwork<T> unitFlowNetwork;
    private final double epsilon;
    private final Map<T, Integer> hopDistancesToSource;
    private final Map<T, Integer> hopDistancesToTarget;
    private final Set<EdgeCut<T>> cuts = new HashSet<>();

    public FlowCutter(Graph<T> graph, T source, T target, double epsilon) {
        if (epsilon < 0.0 || epsilon > 1.0) {
            throw new IllegalArgumentException(String.format(
                    "Epsilon of %s is not in valid range of [0.0, 1.0]", epsilon));
        }
        this.epsilon = epsilon;
        unitFlowNetwork = new UnitFlowNetwork<>(graph, source, target);
        hopDistancesToSource = computeHopDistancesTo(unitFlowNetwork.getOriginalSource());
        hopDistancesToTarget = computeHopDistancesTo(unitFlowNetwork.getOriginalTarget());
        computeCutSets();
    }

    private Map<T, Integer> computeHopDistancesTo(T node) {
        Map<T, Integer> hopDistancesToNode = new HashMap<>(Collections.singletonMap(node, 0));
        Set<T> visited = new HashSet<>(Collections.singleton(node));
        Queue<T> queue = new LinkedList<>(visited);
        while (!queue.isEmpty()) {
            var currentNode = queue.remove();
            var neighbors = unitFlowNetwork.getAdjacentNodes(currentNode);
            for (var neighbor : neighbors) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                    hopDistancesToNode.put(neighbor, hopDistancesToNode.get(currentNode) + 1);
                }
            }
        }
        return hopDistancesToNode;
    }

    private void computeCutSets() {
        var foundEpsilonBalancedBipartition = false;
        while (!foundEpsilonBalancedBipartition && !isIntersectingSourceAndTarget()) {
            var edmondsKarp = new EdmondsKarp<>(unitFlowNetwork);
            if (edmondsKarp.getSourceReachableNodes().size() <= edmondsKarp.getTargetReachableNodes().size()) {
                unitFlowNetwork.setSourceNodes(edmondsKarp.getSourceReachableNodes());
                var sourceSideCut = findCutFor(edmondsKarp.getSourceReachableNodes());
                cuts.add(sourceSideCut);
                foundEpsilonBalancedBipartition = isEpsilonBalancedBipartition(edmondsKarp.getSourceReachableNodes());
                var piercingNode = findPiercingNode(sourceSideCut, edmondsKarp.getTargetReachableNodes(), CutSide.SOURCE);
                unitFlowNetwork.addToSource(piercingNode);
            } else {
                unitFlowNetwork.setTargetNodes(edmondsKarp.getTargetReachableNodes());
                var targetSideCut = findCutFor(edmondsKarp.getTargetReachableNodes());
                cuts.add(targetSideCut);
                foundEpsilonBalancedBipartition = isEpsilonBalancedBipartition(edmondsKarp.getTargetReachableNodes());
                var piercingNode = findPiercingNode(targetSideCut, edmondsKarp.getSourceReachableNodes(), CutSide.TARGET);
                unitFlowNetwork.addToTarget(piercingNode);
            }
        }
        cuts.removeIf(this::isDominatedCut); // Remove dominated cuts to ensure pareto optimal cuts
    }

    private boolean isEpsilonBalancedBipartition(Set<T> sourceOrTargetSet) {
        var numNodes = unitFlowNetwork.getNodes().size();
        var partitionSize = sourceOrTargetSet.size();
        return Math.max(numNodes - partitionSize, partitionSize) <= Math.ceil(((1 + epsilon) * numNodes) / 2.0);
    }

    private boolean isIntersectingSourceAndTarget() {
        return !SetUtils.intersection(unitFlowNetwork.getSourceNodes(), unitFlowNetwork.getTargetNodes()).isEmpty();
    }

    private EdgeCut<T> findCutFor(Set<T> reachableNodes) {
        var cut = reachableNodes
                .stream()
                .map(unitFlowNetwork::getOutEdges)
                .flatMap(Set::stream)
                .map(MultiFlowEdge::getFirst)
                .filter(e -> isCutEdge(reachableNodes, e))
                .collect(Collectors.toSet());

        return new EdgeCut<>(unitFlowNetwork, cut, new HashSet<>(reachableNodes));
    }

    private boolean isCutEdge(Set<T> reachableNodes, UnitFlowEdge<T> edge) {
        return (reachableNodes.contains(edge.getSource()) && !reachableNodes.contains(edge.getTarget())
                || (reachableNodes.contains(edge.getTarget()) && !reachableNodes.contains(edge.getSource())));
    }

    private T findPiercingNode(EdgeCut<T> cut, Set<T> oppositeSideReachableNodes, CutSide cutSide) {
        return findCandidatePiercingNodes(cut, oppositeSideReachableNodes)
                .stream()
                .max((n1, n2) -> distanceHeuristic(n1, n2, cutSide))
                .orElseThrow(() -> new IllegalStateException("No piercing node found during FlowCutter execution"));
    }

    private Set<T> findCandidatePiercingNodes(EdgeCut<T> sourceOrTargetSideCut,
                                              Set<T> oppositeSideReachableNodes) {
        Set<T> allCandidates = sourceOrTargetSideCut.getCutEdges()
                .stream()
                .map(e -> getEndPointNotInCut(sourceOrTargetSideCut, e))
                .collect(Collectors.toSet());

        Set<T> nonAugmentingCandidates = allCandidates
                .stream()
                .filter(n -> !oppositeSideReachableNodes.contains(n))
                .collect(Collectors.toSet());

        if (!nonAugmentingCandidates.isEmpty()) {
            return nonAugmentingCandidates;
        }
        return allCandidates;
    }

    private T getEndPointNotInCut(EdgeCut<T> edgeCut, Edge<T> edge) {
        if (edgeCut.getNodesInCut().contains(edge.getSource())) {
            return edge.getTarget();
        }
        return edge.getSource();
    }

    // returns pareto optimal cuts
    public Set<EdgeCut<T>> getCuts() {
        return cuts;
    }

    private int distanceHeuristic(T firstNode, T secondNode, CutSide cutSide) {
        if (CutSide.SOURCE == cutSide) {
            return Integer.compare(hopDistancesToTarget.get(firstNode) - hopDistancesToSource.get(firstNode),
                                   hopDistancesToTarget.get(secondNode) - hopDistancesToSource.get(secondNode));
        }
        return Integer.compare(hopDistancesToSource.get(firstNode) - hopDistancesToTarget.get(firstNode),
                               hopDistancesToSource.get(secondNode) - hopDistancesToTarget.get(secondNode)
        );
    }

    private boolean isDominatedCut(EdgeCut<T> candidateCut) {
        for (var otherCut : cuts) {
            int imbalanceCompare = Double.compare(candidateCut.getImbalance(), otherCut.getImbalance());
            if ((otherCut.getCutEdges().size() < candidateCut.getCutEdges().size() && imbalanceCompare >= 0)
                    || (otherCut.getCutEdges().size() <= candidateCut.getCutEdges().size() && imbalanceCompare > 0)) {
                return true;
            }
        }
        return false;
    }
}
