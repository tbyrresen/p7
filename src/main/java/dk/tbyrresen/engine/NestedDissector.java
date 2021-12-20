package dk.tbyrresen.engine;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

// Used for testing only to compare ND-trees vs generic nested dissection
public class NestedDissector {
    private NestedDissector() {
    }

    public static<T> List<Set<T>> dissect(Graph<T> graph, double epsilon, int numFlowCutterRuns) {
        Deque<Set<T>> dissections = new ArrayDeque<>();
        Queue<Graph<T>> queue = new LinkedList<>(Collections.singletonList(graph));
        while (!queue.isEmpty()) {
            var currentGraph = queue.remove();
            if (GraphUtils.isClique(currentGraph) || GraphUtils.isTree(currentGraph)) {
                dissections.addFirst(currentGraph.getNodes());
            } else {
                var graphSeparator = new GraphSeparator<>(currentGraph, epsilon, numFlowCutterRuns);
                var separator = graphSeparator.getSeparator();
                dissections.addFirst(separator.getSeparatorNodes());
                var subgraphs = graphSeparator.separate();
                queue.addAll(subgraphs.left);
                queue.addAll(subgraphs.right);
            }
        }
        return new ArrayList<>(dissections);
    }
}
