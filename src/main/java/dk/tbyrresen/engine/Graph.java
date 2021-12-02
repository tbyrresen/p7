package dk.tbyrresen.engine;

import java.util.Set;

public interface Graph<T> {
    Set<T> getNodes();
    Set<T> getAdjacentNodes(T node);
    void addEdge(Edge<T> edge);
    Set<Edge<T>> getEdges();
    boolean allowsSelfLoops();
    boolean allowsParallelEdges();
}
