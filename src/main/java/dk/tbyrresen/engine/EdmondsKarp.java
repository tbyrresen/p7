package dk.tbyrresen.engine;

import org.springframework.lang.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

// TODO various optimization steps have left this class with many speficic methods that really needs generalization
public class EdmondsKarp<T> {
    private final UnitFlowNetwork<T> unitFlowNetwork;
    private final Map<T, MultiFlowEdge<T>> edgeTo = new HashMap<>();
    private final Map<T, MultiFlowEdge<T>> edgeFrom = new HashMap<>();
    private final Set<T> sourceReachableNodes = new HashSet<>();
    private final Set<T> targetReachableNodes = new HashSet<>();
    // Use a single target node for augmenting the flow to avoid having to create
    // super nodes when growing the source and target sets in the flow network
    @Nullable private T augmentingPathTarget;
    @Nullable private T augmentingPathStart;
    private int maxFlow = 0; // TODO do we need to keep track of this?

    public EdmondsKarp(UnitFlowNetwork<T> unitFlowNetwork) {
        this.unitFlowNetwork = unitFlowNetwork;
    }

    public void updateFlow() {
        while (hasAugmentingPath()) {
            var currentNode = augmentingPathTarget;
            while (!unitFlowNetwork.isSourceNode(currentNode)) {
                edgeTo.get(currentNode).flowTo(currentNode);
                currentNode = edgeTo.get(currentNode).getOppositeOf(currentNode);
            }
            maxFlow++; // Minimum possible flow increment is always 1 in a unit capacity flow network
        }
        updateTargetReachable(); // Source reachable nodes are computed when exhausting augmenting paths
    }

    // Note that the very last trip when result is false will result precisely in the source reachable set
    private boolean hasAugmentingPath() {
        edgeTo.clear();
        sourceReachableNodes.clear();
        sourceReachableNodes.addAll(unitFlowNetwork.getSourceNodes());
        Queue<T> queue = new LinkedList<>(sourceReachableNodes);
        while (!queue.isEmpty()) {
            var currentNode = queue.remove();
            for (var multiFlowEdge : unitFlowNetwork.getOutEdges(currentNode)) {
                var oppositeNode = multiFlowEdge.getOppositeOf(currentNode);
                if (multiFlowEdge.canFlowTo(oppositeNode) && !sourceReachableNodes.contains(oppositeNode)) {
                    sourceReachableNodes.add(oppositeNode);
                    edgeTo.put(oppositeNode, multiFlowEdge);
                    if (unitFlowNetwork.isTargetNode(oppositeNode)) {
                        augmentingPathTarget = oppositeNode;
                        return true;
                    }
                    queue.add(oppositeNode);
                }
            }
        }
        return false;
    }

    public void updateFlowFrom(T node) {
        while (hasAugmentingPathFrom(node)) {
            var currentNode = augmentingPathTarget;
            while (!unitFlowNetwork.isSourceNode(currentNode)) {
                edgeTo.get(currentNode).flowTo(currentNode);
                currentNode = edgeTo.get(currentNode).getOppositeOf(currentNode);
            }
            maxFlow++; // Minimum possible flow increment is always 1 in a unit capacity flow network
        }
        updateTargetReachable(); // Source reachable nodes are computed when exhausting augmenting paths
    }

    private boolean hasAugmentingPathFrom(T node) {
        edgeTo.clear();
        sourceReachableNodes.clear();
        sourceReachableNodes.addAll(unitFlowNetwork.getSourceNodes());
        Queue<T> queue = new LinkedList<>(Collections.singletonList(node));
        while (!queue.isEmpty()) {
            var currentNode = queue.remove();
            for (var multiFlowEdge : unitFlowNetwork.getOutEdges(currentNode)) {
                var oppositeNode = multiFlowEdge.getOppositeOf(currentNode);
                if (multiFlowEdge.canFlowTo(oppositeNode) && !sourceReachableNodes.contains(oppositeNode)) {
                    sourceReachableNodes.add(oppositeNode);
                    edgeTo.put(oppositeNode, multiFlowEdge);
                    if (unitFlowNetwork.isTargetNode(oppositeNode)) {
                        augmentingPathTarget = oppositeNode;
                        return true;
                    }
                    queue.add(oppositeNode);
                }
            }
        }
        return false;
    }

    public void updateFlowTo(T node) {
        while (hasAugmentingPathTo(node)) {
            var currentNode = augmentingPathStart;
            while (!unitFlowNetwork.isTargetNode(currentNode)) {
                var oppositeNode = edgeFrom.get(currentNode).getOppositeOf(currentNode);
                edgeFrom.get(currentNode).flowTo(oppositeNode);
                currentNode = oppositeNode;
            }
            maxFlow++; // Minimum possible flow increment is always 1 in a unit capacity flow network
        }
        updateSourceReachable();
    }

    private boolean hasAugmentingPathTo(T node) {
        edgeFrom.clear();
        targetReachableNodes.clear();
        targetReachableNodes.addAll(unitFlowNetwork.getTargetNodes());
        Queue<T> queue = new LinkedList<>(Collections.singletonList(node));
        while (!queue.isEmpty()) {
            var currentNode = queue.remove();
            for (var multiFlowEdge : unitFlowNetwork.getOutEdges(currentNode)) {
                var oppositeNode = multiFlowEdge.getOppositeOf(currentNode);
                if (multiFlowEdge.canFlowTo(currentNode) && !targetReachableNodes.contains(oppositeNode)) {
                    targetReachableNodes.add(oppositeNode);
                    edgeFrom.put(oppositeNode, multiFlowEdge);
                    if (unitFlowNetwork.isSourceNode(oppositeNode)) {
                        augmentingPathStart = oppositeNode;
                        return true;
                    }
                    queue.add(oppositeNode);
                }
            }
        }
        return false;
    }

    public void updateSourceReachableFrom(T node) {
        sourceReachableNodes.add(node);
        Queue<T> queue = new LinkedList<>(Collections.singletonList(node));
        while (!queue.isEmpty()) {
            var currentNode = queue.remove();
            for (var multiFlowEdge : unitFlowNetwork.getOutEdges(currentNode)) {
                var oppositeNode = multiFlowEdge.getOppositeOf(currentNode);
                if (multiFlowEdge.canFlowTo(oppositeNode) && !sourceReachableNodes.contains(oppositeNode)) {
                    sourceReachableNodes.add(oppositeNode);
                    queue.add(oppositeNode);
                }
            }
        }
    }

    public void updateTargetReachableFrom(T node) {
        targetReachableNodes.add(node);
        Queue<T> queue = new LinkedList<>(Collections.singletonList(node));
        while (!queue.isEmpty()) {
            var currentNode = queue.remove();
            for (var multiFlowEdge : unitFlowNetwork.getOutEdges(currentNode)) {
                var oppositeNode = multiFlowEdge.getOppositeOf(currentNode);
                if (multiFlowEdge.canFlowTo(currentNode) && !targetReachableNodes.contains(oppositeNode)) {
                    targetReachableNodes.add(oppositeNode);
                    queue.add(oppositeNode);
                }
            }
        }
    }

    private void updateSourceReachable() {
        sourceReachableNodes.clear();
        sourceReachableNodes.addAll(unitFlowNetwork.getSourceNodes());
        Queue<T> queue = new LinkedList<>(sourceReachableNodes);
        while (!queue.isEmpty()) {
            var currentNode = queue.remove();
            for (var multiFlowEdge : unitFlowNetwork.getOutEdges(currentNode)) {
                var oppositeNode = multiFlowEdge.getOppositeOf(currentNode);
                if (multiFlowEdge.canFlowTo(oppositeNode) && !sourceReachableNodes.contains(oppositeNode)) {
                    sourceReachableNodes.add(oppositeNode);
                    queue.add(oppositeNode);
                }
            }
        }
    }

    // must be computed AFTER we have concluded the forward search i.e. have exhausted all augmenting paths
    private void updateTargetReachable() {
        targetReachableNodes.clear();
        targetReachableNodes.addAll(unitFlowNetwork.getTargetNodes());
        Queue<T> queue = new LinkedList<>(targetReachableNodes);
        while (!queue.isEmpty()) {
            var currentNode = queue.remove();
            for (var multiFlowEdge : unitFlowNetwork.getOutEdges(currentNode)) {
                var oppositeNode = multiFlowEdge.getOppositeOf(currentNode);
                if (multiFlowEdge.canFlowTo(currentNode) && !targetReachableNodes.contains(oppositeNode)) {
                    targetReachableNodes.add(oppositeNode);
                    queue.add(oppositeNode);
                }
            }
        }
    }

    public Set<T> getSourceReachableNodes() {
        return sourceReachableNodes;
    }

    public Set<T> getTargetReachableNodes() {
        return targetReachableNodes;
    }

    public int getMaxFlow() {
        return maxFlow;
    }
}