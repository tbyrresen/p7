package dk.tbyrresen.engine;

import java.util.Objects;
import java.util.Set;

public class EdgeCut<T> {
    private final Graph<T> parentGraph;
    private final Set<UnitFlowEdge<T>> cutEdges;
    private final Set<T> nodesInCut;
    private final double imbalance;
    private final double expansionSize;

    public EdgeCut(Graph<T> parentGraph, Set<UnitFlowEdge<T>> cut, Set<T> nodesInCut) {
        this.parentGraph = parentGraph;
        this.cutEdges = cut;
        this.nodesInCut = nodesInCut;
        var numNodesInGraph = parentGraph.getNodes().size();
        var numNodesInCut = nodesInCut.size();
        this.imbalance = computeImbalance(numNodesInGraph, numNodesInCut);
        expansionSize = (double) cut.size() / Math.min(numNodesInGraph, numNodesInCut);
    }

    private double computeImbalance(int numNodesInGraph, int numNodesInCut) {
        // Epsilon balance is defined as Max(V1, V2) <= ceil((1 + epsilon) * N / 2)
        // where N is the number of nodes in the graph, V2 is the number of reachable nodes and V1 is N - V2
        // Finding the epsilon imbalance for an edge cut thus corresponds to (Max(V1, V2) * 2 / N) - 1.
        return (((double) Math.max(numNodesInCut, numNodesInGraph - numNodesInCut) * 2) / numNodesInGraph) - 1;
    }

    public Set<UnitFlowEdge<T>> getCutEdges() {
        return cutEdges;
    }

    public double getImbalance() {
        return imbalance;
    }

    public double getExpansionSize() {
        return expansionSize;
    }

    public Set<T> getNodesInCut() {
        return nodesInCut;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EdgeCut<?> edgeCut = (EdgeCut<?>) o;
        return Double.compare(edgeCut.imbalance, imbalance) == 0 &&
               Double.compare(edgeCut.expansionSize, expansionSize) == 0 &&
               parentGraph.equals(edgeCut.parentGraph) && cutEdges.equals(edgeCut.cutEdges) &&
               nodesInCut.equals(edgeCut.nodesInCut);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parentGraph, cutEdges, nodesInCut, imbalance, expansionSize);
    }
}
