package dk.tbyrresen.engine;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class NestedDissector {
    private NestedDissector() {
    }

    // TODO return immutable?
    // TODO return graph or nodes? Nodes would be cheaper and should be sufficient
    // TODO parallelize method this since we are dealing with disjoint graphs in each recursive step
    // TODO consider finding the largest biconnected component and do computation on them in parallel? See special preprocessing in papers

    // TODO should probably start checking connected components
    public static<T> List<Set<T>> dissect(Graph<T> graph, double epsilon) {
        Deque<Set<T>> dissections = new ArrayDeque<>();
        Queue<Graph<T>> queue = new LinkedList<>(Collections.singletonList(graph));
        while (!queue.isEmpty()) {
            var currentGraph = queue.remove();
            if (GraphUtils.isClique(currentGraph) || GraphUtils.isTree(currentGraph)) {
                dissections.addFirst(currentGraph.getNodes());
            } else {
                var separator = new GraphSeparator<>(currentGraph, epsilon);
                var subGraphs = separator.separate();
                dissections.addFirst(separator.getSeparator());
                queue.addAll(subGraphs);
            }
        }
        return new ArrayList<>(dissections);
    }
}
