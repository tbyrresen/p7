package dk.tbyrresen.engine;

import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class NestedDissectionTree<T> {
    private NestedDissectionTreeNode<T> root;
    private List<NestedDissectionTreeNode<T>> orderedDissections;
    private final double epsilon;

    public NestedDissectionTree(Graph<T> graph, double epsilon) {
        this.epsilon = epsilon;
        root = buildNestedDissectionTreeRoot(graph);
        orderedDissections = getOrderedDissectionNodes();
    }

    private NestedDissectionTreeNode<T> buildNestedDissectionTreeRoot(Graph<T> graph) {
        NestedDissectionTreeNode<T> treeRoot;
        if (GraphUtils.isClique(graph) || GraphUtils.isTree(graph)) {
            treeRoot = new NestedDissectionTreeNode<>(graph.getNodes(), graph.getEdges(), 0);
        } else {
            var graphSeparator = new GraphSeparator<>(graph, epsilon);
            var separator = graphSeparator.getSeparator();
            var separatorNodes = separator.getSeparatorNodes();
            var separatorEdges = GraphUtils.extractSubGraphEdges(graph, separatorNodes);
            var subGraphs = graphSeparator.separate();
            treeRoot = new NestedDissectionTreeNode<>(
                    separatorNodes,
                    separatorEdges,
                    separator.getSeparatingEdges(),
                    subGraphs.getLeft().stream().mapToInt(g -> g.getNodes().size()).sum(),
                    subGraphs.getRight().stream().mapToInt(g -> g.getNodes().size()).sum(),
                    0
            );
            for (var subGraph : subGraphs.left) {
                buildNestedDissectionTree(treeRoot, subGraph, SeparationSide.LEFT, 1);
            }
            for (var subGraph : subGraphs.right) {
                buildNestedDissectionTree(treeRoot, subGraph, SeparationSide.RIGHT, 1);
            }
        }
        return treeRoot;
    }

    private void buildNestedDissectionTree(NestedDissectionTreeNode<T> parent,
                                           Graph<T> graph,
                                           SeparationSide separationSide,
                                           int depth) {
        if (GraphUtils.isClique(graph) || GraphUtils.isTree(graph)) {
            var dissectionNode = new NestedDissectionTreeNode<>(
                    graph.getNodes(),
                    graph.getEdges(),
                    parent,
                    separationSide,
                    depth
            );
            parent.addChild(dissectionNode);
        } else {
            var graphSeparator = new GraphSeparator<>(graph, epsilon);
            var separator = graphSeparator.getSeparator();
            var separatorNodes = separator.getSeparatorNodes();
            var separatorEdges = GraphUtils.extractSubGraphEdges(graph, separatorNodes);
            var subGraphs = graphSeparator.separate();
            var dissectionNode = new NestedDissectionTreeNode<>(
                    separatorNodes,
                    separatorEdges,
                    parent,
                    separator.getSeparatingEdges(),
                    separationSide,
                    subGraphs.getLeft().stream().mapToInt(g -> g.getNodes().size()).sum(),
                    subGraphs.getRight().stream().mapToInt(g -> g.getNodes().size()).sum(),
                    depth
            );
            parent.addChild(dissectionNode);
            for (var subGraph : subGraphs.left) {
                buildNestedDissectionTree(dissectionNode, subGraph, SeparationSide.LEFT, depth + 1);
            }
            for (var subGraph : subGraphs.right) {
                buildNestedDissectionTree(dissectionNode, subGraph, SeparationSide.RIGHT, depth + 1);
            }
        }
    }

    // TODO make this able to compute from specific node instead of traversing entire tree each time
    // Collects all dissections in correct order by doing a post order traversal from the root node
    private List<NestedDissectionTreeNode<T>> getOrderedDissectionNodes() {
        List<NestedDissectionTreeNode<T>> orderedDissectionNodes = new ArrayList<>();
        collectOrderedDissectionNodes(root, orderedDissectionNodes);
        return orderedDissectionNodes;
    }

    private void collectOrderedDissectionNodes(NestedDissectionTreeNode<T> node,
                                               List<NestedDissectionTreeNode<T>> dissectionNodes) {
        for (var child : node.getChildren()) {
            collectOrderedDissectionNodes(child, dissectionNodes);
        }
        dissectionNodes.add(node);
    }

    public void addEdge(Edge<T> edge) {
        var sourceDissectionNode = findDissectionNodeByGraphNode(edge.getSource());
        var targetDissectionNode = findDissectionNodeByGraphNode(edge.getTarget());
        if (sourceDissectionNode.isEmpty() && targetDissectionNode.isEmpty()) {
            throw new IllegalArgumentException(String.format(
                    "At least one endpoint of edge %s must be a node in the graph", edge));
        } else if (sourceDissectionNode.isPresent() && targetDissectionNode.isEmpty()) {
            var source = sourceDissectionNode.get();
            source.addDissectionNode(edge.getTarget());
            incrementTreeSize(source.getParent(), source.getSeparationSide());
            source.addDissectionEdge(edge);
            var highestImbalancedNode = findHighestImbalancedAncestor(source);
            highestImbalancedNode.ifPresent(this::recomputeTreeFromDissectionNode);
        } else if (sourceDissectionNode.isEmpty()) {
            var target = targetDissectionNode.get();
            target.addDissectionNode(edge.getSource());
            incrementTreeSize(target.getParent(), target.getSeparationSide());
            target.addDissectionEdge(edge);
            var highestImbalancedNode = findHighestImbalancedAncestor(target);
            highestImbalancedNode.ifPresent(this::recomputeTreeFromDissectionNode);
        } else { // need to find the lowest common ancestor when both are present
            var source = sourceDissectionNode.get();
            var target = targetDissectionNode.get();
            var lowestCommonAncestor= findLowestCommonAncestor(source, target);
            if (lowestCommonAncestor.equals(source)) {  // source == target
                source.addDissectionEdge(edge);
            } else { // determine if the edge crosses a separator or if we simply need to add an edge to child
                if (crossesSeparator(sourceDissectionNode.get(), targetDissectionNode.get())) {
                    recomputeTreeFromDissectionNode(lowestCommonAncestor, edge);
                } else { // one of the two must be the parent of the other which never requires recomputation
                    if (source.getParent() != null && source.getParent().equals(target)) {
                        target.addEdgeToChildren(edge);
                    } else {
                        source.addEdgeToChildren(edge);
                    }
                }
            }
        }
    }

    private Optional<NestedDissectionTreeNode<T>> findDissectionNodeByGraphNode(T graphNode) {
        for (var dissectionNode : orderedDissections) {
            if (dissectionNode.getDissectionNodes().contains(graphNode)) {
                return Optional.of(dissectionNode);
            }
        }
        return Optional.empty();
    }

    // Increments the tree size of the given size by one and propagates this change to all ancestor nodes
    private void incrementTreeSize(@Nullable NestedDissectionTreeNode<T> node, @Nullable SeparationSide separationSide) {
        if (node != null) {
            node.incrementTreeSize(separationSide);
            incrementTreeSize(node.getParent(), node.getSeparationSide());
        }
    }

    private Optional<NestedDissectionTreeNode<T>> findHighestImbalancedAncestor(NestedDissectionTreeNode<T> node) {
        Optional<NestedDissectionTreeNode<T>> highestImbalancedNode = Optional.empty();
        var currentNode = node;
        while (currentNode != null) {
            if (!currentNode.isBalanced()) {
                highestImbalancedNode = Optional.of(currentNode);
            }
            currentNode = currentNode.getParent();
        }
        return highestImbalancedNode;
    }

    private NestedDissectionTreeNode<T> findLowestCommonAncestor(NestedDissectionTreeNode<T> source,
                                                                 NestedDissectionTreeNode<T> target) {
        if (root.equals(source) || root.equals(target)) {
            return root;
        } else if (source.equals(target)) {
            return source;
        }
        var sourceParent = source.getParent();
        var targetParent = target.getParent();
        if (source.getDepth() > target.getDepth() && sourceParent != null) {
            return findLowestCommonAncestor(sourceParent, target);
        } else if (source.getDepth() < target.getDepth() && targetParent != null) {
            return findLowestCommonAncestor(source, targetParent);
        } else if (sourceParent != null && targetParent != null) {
            return findLowestCommonAncestor(sourceParent, targetParent);
        }
        throw new IllegalStateException(String.format(
                "Can't find common ancestor of dissection nodes %s and %s", source, target));
    }

    // Returns true if a separator needs to be crossed to traverse from source to target or vice versa
    // This is true iff the two nodes are not the same and if neither of the two nodes is the parent
    // of the other
    private boolean crossesSeparator(NestedDissectionTreeNode<T> source, NestedDissectionTreeNode<T> target) {
        var sourceParent = source.getParent();
        var targetParent = target.getParent();
        return !source.equals(target)
               && (sourceParent == null || !sourceParent.equals(target))
               && (targetParent == null || !targetParent.equals(source));
    }

    private void recomputeTreeFromDissectionNode(NestedDissectionTreeNode<T> node) {
        var graph = buildGraphFromDissectionNode(node);
        if (root.equals(node)) {
            root = buildNestedDissectionTreeRoot(graph);
        } else {
            var parentNode = node.getParent();
            if (parentNode == null || node.getSeparationSide() == null) {
                throw new IllegalStateException(String.format(
                        "No parent or separation side available for node %s", node));
            }
            parentNode.removeChild(node);
            buildNestedDissectionTree(parentNode, graph, node.getSeparationSide(), node.getDepth());
        }
        orderedDissections = getOrderedDissectionNodes();
    }

    // TODO fix duplicated code
    // We use this in the case where an edge crosses a separator and we need to recompute. If the
    // edge crosses a separator we have no nested dissection node to which we can add the edge
    // and we can't add it as a children edge either, so we pass it this way.
    private void recomputeTreeFromDissectionNode(NestedDissectionTreeNode<T> node, Edge<T> edge) {
        var graph = buildGraphFromDissectionNode(node);
        graph.addEdge(edge);
        if (root.equals(node)) {
            root = buildNestedDissectionTreeRoot(graph);
        } else {
            var parentNode = node.getParent();
            if (parentNode == null || node.getSeparationSide() == null) {
                throw new IllegalStateException(String.format(
                        "No parent or separation side available for node %s", node));
            }
            parentNode.removeChild(node);
            buildNestedDissectionTree(parentNode, graph, node.getSeparationSide(), node.getDepth());
        }
        orderedDissections = getOrderedDissectionNodes();
    }

    private Graph<T> buildGraphFromDissectionNode(NestedDissectionTreeNode<T> node) {
        var collectedNodes = new HashSet<>(node.getDissectionNodes());
        var collectedEdges = new HashSet<>(node.getDissectionEdges());
        collectedEdges.addAll(node.getEdgesToChildren());
        for (var child : node.getChildren()) {
            collectedNodes.addAll(collectNodesFromDissectionNode(child));
            collectedEdges.addAll(collectEdgesFromDissectionNode(child));
        }
        return new StandardGraph<>(collectedNodes, collectedEdges);
    }

    // TODO FOR TESTING
    public NestedDissectionTreeNode<T> getRoot() {
        return root;
    }

    private Set<T> collectNodesFromDissectionNode(NestedDissectionTreeNode<T> node) {
        var collectedNodes = new HashSet<>(node.getDissectionNodes());
        for (var child : node.getChildren()) {
            collectedNodes.addAll(collectNodesFromDissectionNode(child));
        }
        return collectedNodes;
    }

    private Set<Edge<T>> collectEdgesFromDissectionNode(NestedDissectionTreeNode<T> node) {
        var collectedEdges = new HashSet<>(node.getDissectionEdges());
        collectedEdges.addAll(node.getEdgesToChildren());
        for (var child : node.getChildren()) {
            collectedEdges.addAll(collectEdgesFromDissectionNode(child));
        }
        return collectedEdges;
    }

    public List<NestedDissectionTreeNode<T>> getOrderedDissections() {
        return orderedDissections;
    }

    // TODO this is straightforward but could be more efficient
    public int getNumDirtyNodes() {
        return orderedDissections
                .stream()
                .mapToInt(NestedDissectionTreeNode::getNumDirtyNodes)
                .sum();
    }
}
