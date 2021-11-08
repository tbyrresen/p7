package dk.tbyrresen.engine;

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
}
