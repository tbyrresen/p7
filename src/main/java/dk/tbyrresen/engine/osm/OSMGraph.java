package dk.tbyrresen.engine.osm;

import dk.tbyrresen.engine.Edge;
import dk.tbyrresen.engine.Node;
import dk.tbyrresen.engine.StandardEdge;
import dk.tbyrresen.engine.StandardGraph;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;

public class OSMGraph extends StandardGraph<Node> {
    private final Set<OSMWay> osmWays = new HashSet<>();

    // This is a really messed up way to make OSMGraph extend StandardGraph, but its good enough for our needs
    public OSMGraph(String fileName) {
        super();
        OSMHandler.parseFromXML(this, fileName); // populates osmWays
        var nodesAndEdges = buildNodesAndEdgesFromOsmWays();
        nodes = nodesAndEdges.getLeft();
        edges = nodesAndEdges.getRight();
        System.out.println("osm nodes: " + nodes.size());
        System.out.println("osm edges: " + edges.size());
        adjacentNodes = findAdjacentNodes(nodes, edges);
        allowsSelfLoops = false;
        allowsParallelEdges = false;
    }

    private Pair<Set<Node>, Set<Edge<Node>>> buildNodesAndEdgesFromOsmWays() {
        Set<Node> graphNodes = new HashSet<>();
        Set<Edge<Node>> graphEdges = new HashSet<>();
        for (var way : osmWays) {
            List<Node> wayNodes = new ArrayList<>();
            Set<Edge<Node>> wayEdges = new HashSet<>();
            Queue<Long> nodeRefs = way.getNodeRefs();
            while (!nodeRefs.isEmpty()) {
                wayNodes.add(new Node(nodeRefs.remove()));
            }
            for (int i = 0; i < wayNodes.size() - 1; i++) {
                wayEdges.add(new StandardEdge<>(wayNodes.get(i), wayNodes.get(i + 1)));
            }
            graphNodes.addAll(wayNodes);
            graphEdges.addAll(wayEdges);
        }
        removeSelfLoops(graphEdges);
        // parallel edges are removed by using set to contains nodes and by having an appropriate equals method
        return ImmutablePair.of(graphNodes, graphEdges);
    }

    private void removeSelfLoops(Set<Edge<Node>> edges) {
        edges.removeIf(e -> e.getSource().equals(e.getTarget()));
    }

    public void addWay(OSMWay way) {
        osmWays.add(way);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        OSMGraph osmGraph = (OSMGraph) o;
        return osmWays.equals(osmGraph.osmWays);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), osmWays);
    }
}
