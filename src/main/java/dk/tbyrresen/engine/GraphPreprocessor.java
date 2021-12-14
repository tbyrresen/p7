package dk.tbyrresen.engine;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class GraphPreprocessor {
    private GraphPreprocessor() {
    }

    public static<T> Graph<T> getPreProcessedGraph(Graph<T> graph) {
        var nodesToKeep = findDegreeThreeNodes(graph);
        var edgesToKeep = findEdgesBetweenDegreeThreeNodes(graph, nodesToKeep);
        addDegreeTwoEndpointEdges(graph, nodesToKeep, edgesToKeep);
        // remove parallel edges since we could end up introducing new ones again
        edgesToKeep.removeIf(e -> e.getSource().equals(e.getTarget()));
        return new StandardGraph<>(nodesToKeep, edgesToKeep);
    }

    private static<T> Set<T> findDegreeThreeNodes(Graph<T> graph) {
        HashSet<T> degreeThreeNodes = new HashSet<>();
        for (var node : graph.getNodes()) {
            if (graph.getAdjacentNodes(node).size() >= 3) {
                degreeThreeNodes.add(node);
            }
        }
        return degreeThreeNodes;
    }

    private static<T> Set<Edge<T>> findEdgesBetweenDegreeThreeNodes(Graph<T> graph, Set<T> nodesToKeep) {
        HashSet<Edge<T>> edgesBetweenDegreeThreeNodes = new HashSet<>();
        for (var edge : graph.getEdges()) {
            if (nodesToKeep.contains(edge.getSource()) && nodesToKeep.contains(edge.getTarget())) {
                edgesBetweenDegreeThreeNodes.add(edge);
            }
        }
        return edgesBetweenDegreeThreeNodes;
    }

    private static<T> void addDegreeTwoEndpointEdges(Graph<T> graph, Set<T> nodesToKeep, Set<Edge<T>> edgesToKeep) {
        HashSet<T> visited = new HashSet<>(nodesToKeep);
        for (var node : nodesToKeep) {
            for (var adjacent : graph.getAdjacentNodes(node)) {
                if (!visited.contains(adjacent)) {
                    Optional<T> endPoint = getEndpointStartingAt(graph, nodesToKeep, adjacent, node, visited);
                    endPoint.ifPresent(e -> edgesToKeep.add(new StandardEdge<>(node, e)));
                }
            }
        }
    }

    // Visits all nodes in a degree 2 path starting at startingNode having originated at some
    // origin part of nodesToKeep. Assumes that startingNode is itself a valid degree 2 node.
    private static<T> Optional<T> getEndpointStartingAt(Graph<T> graph, Set<T> nodesToKeep, T startingNode, T origin, Set<T> visited) {
        T current = startingNode;
        T prev = origin;
        visited.add(current);
        boolean foundNextNode;
        while (true) {
            foundNextNode = false;
            for (var adjacent : graph.getAdjacentNodes(current)) {
                if (!adjacent.equals(prev)) {
                    foundNextNode = true;
                    prev = current;
                    current = adjacent;
                    visited.add(current);
                    // An endpoint we can simply discard since it goes nowhere
                    if (graph.getAdjacentNodes(current).size() == 1) {
                        return Optional.empty();
                    // A node to which we need to create an edge
                    } else if (nodesToKeep.contains(current)) {
                        return Optional.of(current);
                    }
                    break;
                }
            }
            if (!foundNextNode) {
                // This happens exactly when we cycle back to the origin meaning that we don't need to add any edges
                return Optional.empty();
            }
        }
    }
}
