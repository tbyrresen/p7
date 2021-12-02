package dk.tbyrresen.engine;

import java.util.Objects;

// TODO make clear that this is really an undirected edge
public class StandardEdge<T> implements Edge<T> {
    private final T source;
    private final T target;

    public StandardEdge(T source, T target) {
        this.source = source;
        this.target = target;
    }

    @Override
    public T getSource() {
        return source;
    }

    @Override
    public T getTarget() {
        return target;
    }

    @Override
    public T getOppositeOf(T node) {
        if (!source.equals(node) && !target.equals(node)) {
            throw new IllegalArgumentException(String.format(
                    "Node %s must be either source or target of edge", node));
        }
        return source.equals(node) ? target : source;
    }

    @Override
    public String toString() {
        return "StandardEdge{" +
                "source=" + source +
                ", target=" + target +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        StandardEdge<?> that = (StandardEdge<?>) o;
        return source.equals(that.source) && target.equals(that.target);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, target);
    }
}
