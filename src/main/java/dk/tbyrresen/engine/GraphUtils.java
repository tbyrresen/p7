package dk.tbyrresen.engine;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.SetUtils;
import org.springframework.lang.Nullable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

// TODO we work with undirected graphs for now, but these methods should be corrected to handle both undirected and directed graphs
// TODO also ensure correctness if provided with selfloops and/or parallel edges
public class GraphUtils {
    private GraphUtils() {
    }

    public static<T> Set<Graph<T>> findConnectedComponents(Graph<T> graph) {
        Set<Graph<T>> connectedComponents = new HashSet<>();
        Set<T> visited = new HashSet<>();
        Set<T> currentConnectedNodes = new HashSet<>();
        for (var node : graph.getNodes()) {
            if (!visited.contains(node)) {
                fillConnectedComponent(node, visited, currentConnectedNodes, graph);
                connectedComponents.add(extractSubGraph(graph, currentConnectedNodes));
                currentConnectedNodes = new HashSet<>();
            }
        }
        return connectedComponents;
    }

    // Uses DFS to fill the current connected component
    private static<T> void fillConnectedComponent(T node,
                                                  Set<T> visited,
                                                  Set<T> connectedNodes,
                                                  Graph<T> graph) {
        visited.add(node);
        connectedNodes.add(node);
        for (var neighbor : graph.getAdjacentNodes(node)) {
            if (!visited.contains(neighbor)) {
                fillConnectedComponent(neighbor, visited, connectedNodes, graph);
            }
        }
    }

    public static<T> boolean isClique(Graph<T> graph) {
        var numNodes = graph.getNodes().size();
        var numEdges = graph.getEdges().size();
        if (!graph.allowsSelfLoops() && !graph.allowsParallelEdges()) {
            return numEdges == (numNodes * (numNodes - 1)) / 2;
        } else if (graph.allowsSelfLoops() && !graph.allowsParallelEdges()) {
            var numSelfLoops = findSelfLoops(graph).size();
            return (numEdges - numSelfLoops) == (numNodes * (numNodes - 1)) / 2;
        }
        return checkIfClique(graph);
    }

    private static<T> Set<Edge<T>> findSelfLoops(Graph<T> graph) {
        return graph.getEdges()
                .stream()
                .filter(e -> e.getSource().equals(e.getTarget()))
                .collect(Collectors.toSet());
    }

    private static<T> boolean checkIfClique(Graph<T> graph) {
        for (var node : graph.getNodes()) {
            if (!CollectionUtils.isEqualCollection(SetUtils.difference(graph.getNodes(), Collections.singleton(node)),
                                                                       graph.getAdjacentNodes(node))) {
                return false;
            }
        }
        return true;
    }

    public static<T> boolean isTree(Graph<T> graph) {
        return isConnected(graph) && graph.getEdges().size() == graph.getNodes().size() - 1;
        //return isConnected(graph) && !isCyclic(graph);
    }

    public static<T> boolean isConnected(Graph<T> graph) {
        var source = graph.getNodes().stream().findAny();
        if (source.isEmpty()) {
            return true;
        }
        Set<T> visited = new HashSet<>();
        findReachableNodes(source.get(), visited, graph);
        return visited.size() == graph.getNodes().size();
    }

    // Uses DFS to find all nodes reachable from the provided source node
    private static<T> void findReachableNodes(T source, Set<T> visited, Graph<T> graph) {
        visited.add(source);
        for (var neighbor : graph.getAdjacentNodes(source)) {
            if (!visited.contains(neighbor)) {
                findReachableNodes(neighbor, visited, graph);
            }
        }
    }

    public static<T> boolean isCyclic(Graph<T> graph) {
        if (graph.allowsSelfLoops() && !findSelfLoops(graph).isEmpty()) {
            return true;
        }
        var visited = new HashSet<T>();
        for (var node : graph.getNodes()) {
            if (!visited.contains(node) && checkIfCyclic(null, node, visited, graph)) {
                return true;
            }
        }
        return false;
    }

    // Uses DFS to detect cycles in the provided graph
    private static<T> boolean checkIfCyclic(@Nullable T parent, T source, Set<T> visited, Graph<T> graph) {
        visited.add(source);
        for (var neighbor : graph.getAdjacentNodes(source)) {
            if (!visited.contains(neighbor)) {
                if (checkIfCyclic(source, neighbor, visited, graph)) {
                    return true;
                }
            } else if (!neighbor.equals(parent)) {
                return true;
            }
        }
        return false;
    }

    public static<T> Graph<T> extractSubGraph(Graph<T> graph, Set<T> subGraphNodes) {
        if (!graph.getNodes().containsAll(subGraphNodes)) {
            throw new IllegalArgumentException(String.format(
                    "Cannot extract subgraph for nodes %s which are not all contained in graph %s", subGraphNodes, graph));
        }
        var subGraphEdges = graph.getEdges()
                .stream()
                .filter(e -> isEdgeInSubGraph(e, subGraphNodes))
                .collect(Collectors.toSet());

        return new StandardGraph<>(subGraphNodes, subGraphEdges);
    }

    private static <T>boolean isEdgeInSubGraph(Edge<T> edge, Set<T> nodesOfSubGraph) {
        return nodesOfSubGraph.contains(edge.getSource()) && nodesOfSubGraph.contains(edge.getTarget());
    }

    // TODO WIP preprocessing for road graphs
//    public static<T> void findBiConnectedComponents(Graph<T> graph) {
//        var stack = new LinkedList<Edge<T>>();
//
//        var depth = new HashMap<T, Integer>();
//        var low = new HashMap<T, Integer>();
//        var parent = new HashMap<T, T>();
//        for (var node : graph.getNodes()) {
//            depth.put(node, -1);
//            low.put(node, -1);
//            parent.put(node, null);
//        }
//
//        var time = 0;
//        for (var node : graph.getNodes()) {
//            if (depth.get(node) == -1) {
//                buildBiConnectedComponent(node, graph, stack, depth, low, parent, time);
//            }
//        }
//
//        while (!stack.isEmpty()) {
//            System.out.print(stack.removeLast() + " ");
//        }
//    }
//
//    private static<T> void buildBiConnectedComponent(T node,
//                                                     Graph<T> graph,
//                                                     LinkedList<Edge<T>> stack,
//                                                     HashMap<T, Integer> depth,
//                                                     HashMap<T, Integer> low,
//                                                     HashMap<T, T> parent,
//                                                     int time) {
//        time++;
//        depth.put(node, time);
//        low.put(node, time);
//        int numChildren = 0;
//        for (var neighbor : graph.getAdjacentNodes(node)) {
//            if (depth.get(neighbor) == -1) {
//                numChildren++;
//                parent.put(neighbor, node);
//                stack.add(new StandardEdge<>(node, neighbor));
//                buildBiConnectedComponent(neighbor, graph, stack, depth, low, parent, time);
//                if (low.get(node) > low.get(neighbor)) {
//                    low.put(node, low.get(neighbor));
//                }
//                if ((depth.get(node) == 1 && numChildren > 1)
//                        || (depth.get(node) > 1 && low.get(neighbor) >= depth.get(node))) {
//                    while (!stack.getLast().getSource().equals(node) || !stack.getLast().getTarget().equals(neighbor)) {
//                        System.out.println("HEHE");
//                        System.out.println(stack.getLast().getTarget().equals(node) + "HEJ");
//                        System.out.print(stack.removeLast() + " ");
//                    }
//                    System.out.print(stack.removeLast());
//                    System.out.println();
//                }
//            } else if (!neighbor.equals(parent.get(node)) && depth.get(neighbor) < depth.get(node)) {
//                if (low.get(node) > depth.get(neighbor)) {
//                    low.put(node, low.get(neighbor));
//                }
//                stack.add(new StandardEdge<>(node, neighbor));
//            }
//        }
//    }
}
