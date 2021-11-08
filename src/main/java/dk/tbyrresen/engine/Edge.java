package dk.tbyrresen.engine;

public interface Edge<T> {
    T getSource();
    T getTarget();
    T getOppositeOf(T node);
}
