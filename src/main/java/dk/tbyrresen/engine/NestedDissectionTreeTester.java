package dk.tbyrresen.engine;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class NestedDissectionTreeTester {
    private final Graph<Node> graph;
    private final NestedDissectionTree<Node> ndTree;

    public NestedDissectionTreeTester(Graph<Node> graph, NestedDissectionTree<Node> ndTree) {
        this.graph = graph;
        this.ndTree = ndTree;
    }

    public void insertNewNodesAndPrintStatistics(int numRandomNodes) {
        var edgesToInsert = getNewEdgesWithNewNodes(numRandomNodes);
        var highestRecomputedNodeDepth = Integer.MAX_VALUE; // high as is higher up in the tree i.e. lower depth value
        var peakNumDirtyNodes = 0;
        for (var edge : edgesToInsert) {
            var recomputation = ndTree.addEdge(edge);
            if (recomputation.isPresent()) {
                if (ndTree.getNumDirtyNodes() > peakNumDirtyNodes) {
                    peakNumDirtyNodes = ndTree.getNumDirtyNodes();
                }
                if (recomputation.get().getDepth() < highestRecomputedNodeDepth) {
                    highestRecomputedNodeDepth = recomputation.get().getDepth();
                }
            }
        }
        var recomputations = ndTree.getRecomputations();
        var numRecomputations = recomputations.size();
        System.out.println("Result of adding " + numRandomNodes + " new nodes nodes");
        System.out.println("Number of recomputations: " + numRecomputations);
        if (numRecomputations > 0) {
            System.out.printf("Average recomputation depth: %.2f (SD: %.2f)%n", getAverageDepth(recomputations), getDepthStandardDeviation(recomputations));
            System.out.printf("Average recomputation time: %.2f (SD: %.2f)%n", getAverageTime(recomputations), getTimeStandardDeviation(recomputations));
            System.out.println("Highest recomputed node depth: " + highestRecomputedNodeDepth);
        }
        System.out.println("Peak number of dirty nodes: " + peakNumDirtyNodes);
    }

    public void insertNewEdgesAndPrintStatistics(int numRandomEdges, int maxHopDistance) {
        var edgesToInsert = getNewEdgesBetweenExistingNodes(numRandomEdges, maxHopDistance);
        var highestRecomputedNodeDepth = Integer.MAX_VALUE; // high as is higher up in the tree i.e. lower depth value
        for (var edge : edgesToInsert) {
            var recomputation = ndTree.addEdge(edge);
            if (recomputation.isPresent()) {
                if (recomputation.get().getDepth() < highestRecomputedNodeDepth) {
                    highestRecomputedNodeDepth = recomputation.get().getDepth();
                }
            }
        }
        var recomputations = ndTree.getRecomputations();
        var numRecomputations = recomputations.size();
        System.out.println("Result of adding " + numRandomEdges + " new edges between existing nodes with max hop distance of " + maxHopDistance);
        System.out.println("Number of recomputations: " + numRecomputations);
        if (numRecomputations > 0) {
            System.out.printf("Average recomputation depth: %.2f (SD: %.2f)%n", getAverageDepth(recomputations), getDepthStandardDeviation(recomputations));
            System.out.printf("Average recomputation time: %.2f (SD: %.2f)%n", getAverageTime(recomputations), getTimeStandardDeviation(recomputations));
            System.out.println("Highest recomputed node depth: " + highestRecomputedNodeDepth);
        }
    }

    private Set<Edge<Node>> getNewEdgesWithNewNodes(int numRandomNodes) {
        Set<Edge<Node>> edgesWithNewNodeToInsert = new HashSet<>();
        for (int i = 0; i < numRandomNodes; i++) {
            var nodeToInsert = new Node(i); // OSM nodes have really high id ranges so this won't create conflicts
            var randomNodeInGraph = getRandomNodeFrom(graph.getNodes());
            edgesWithNewNodeToInsert.add(new StandardEdge<>(nodeToInsert, randomNodeInGraph));
        }
        return edgesWithNewNodeToInsert;
    }

    private Set<Edge<Node>> getNewEdgesBetweenExistingNodes(int numRandomEdges, int maxHopDistance) {
        Set<Edge<Node>> edgesBetweenExistingNodesToInsert = new HashSet<>();
        for (int i = 0; i < numRandomEdges; i++) {
            var first = getRandomNodeFrom(graph.getNodes());
            var ndNodeContainingFirst = ndTree.findDissectionNodeByGraphNode(first);
            var nodesAtMaxDistance = getNodesAtMaxHopDistanceFrom(ndNodeContainingFirst.get(), maxHopDistance);
            var second = getRandomNodeFrom(nodesAtMaxDistance);
            while (first.equals(second)) {
                second = getRandomNodeFrom(nodesAtMaxDistance); // avoid creating self loops
            }
            edgesBetweenExistingNodesToInsert.add(new StandardEdge<>(first, second));
        }
        return edgesBetweenExistingNodesToInsert;
    }

    private Node getRandomNodeFrom(Set<Node> nodes) {
        var random = new SecureRandom();
        var idx = random.nextInt(nodes.size());
        return new ArrayList<>(nodes).get(idx);
    }

    private Set<Node> getNodesAtMaxHopDistanceFrom(NestedDissectionTreeNode<Node> ndNode, int hopDistance) {
        Set<Node> nodes = new HashSet<>();
        HashSet<NestedDissectionTreeNode<Node>> visited = new HashSet<>();
        Queue<Pair<NestedDissectionTreeNode<Node>, Integer>> queue = new LinkedList<>(Collections.singleton(ImmutablePair.of(ndNode, 0)));
        while (!queue.isEmpty()) {
            var current = queue.remove();
            visited.add(current.getLeft());
            nodes.addAll(current.getLeft().getDissectionNodes());
            if (current.getRight() < hopDistance) {
                for (var child : current.getLeft().getChildren()) { // add children
                    if (!visited.contains(child)) {
                        queue.add(ImmutablePair.of(child, current.getRight() + 1));
                    }
                }
                if (current.getLeft().getParent() != null && !visited.contains(current.getLeft().getParent())) {
                    queue.add(ImmutablePair.of(current.getLeft().getParent(), current.getRight() + 1)); // add parent
                }
            }
        }
        return nodes;
    }

    private double getAverageDepth(List<NestedDissectionTreeRecomputation> recomputations) {
        return recomputations
                .stream()
                .mapToDouble(NestedDissectionTreeRecomputation::getDepth)
                .average()
                .getAsDouble();
    }

    private double getDepthStandardDeviation(List<NestedDissectionTreeRecomputation> recomputations) {
        double avg = getAverageDepth(recomputations);
        double varianceSum = recomputations
                .stream()
                .mapToDouble(r -> Math.pow(r.getDepth() - avg, 2))
                .sum();
        return Math.sqrt(varianceSum / recomputations.size());
    }

    private double getAverageTime(List<NestedDissectionTreeRecomputation> recomputations) {
        return recomputations
                .stream()
                .mapToDouble(NestedDissectionTreeRecomputation::getRecomputationTimeMs)
                .average()
                .getAsDouble();
    }

    private double getTimeStandardDeviation(List<NestedDissectionTreeRecomputation> recomputations) {
        double avg = getAverageTime(recomputations);
        double varianceSum = recomputations
                .stream()
                .mapToDouble(r -> Math.pow(r.getRecomputationTimeMs() - avg, 2))
                .sum();
        return Math.sqrt(varianceSum / recomputations.size());
    }
}

