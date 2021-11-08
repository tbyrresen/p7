package dk.tbyrresen.engine;

public class UnitFlowEdge<T> implements Edge<T> {
    private enum Direction {
        NONE,
        SOURCE,
        TARGET
    }

    private final T source;
    private final T target;
    private Direction flowDirection = Direction.NONE;

    public UnitFlowEdge(T source, T target) {
        if (source.equals(target)) {
            throw new IllegalArgumentException("Source and target must be distinct");
        }
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

    public T getOppositeOf(T node) {
        requireConnectedNode(node);
        return source.equals(node) ? target : source;
    }

    public boolean canFlowTo(T node) {
        requireConnectedNode(node);
        return source.equals(node) ? flowDirection == Direction.TARGET : flowDirection == Direction.NONE;
    }

    public void flowTo(T node) {
        requireConnectedNode(node);
        if (!canFlowTo(node)) {
            throw new IllegalArgumentException(String.format(
                    "Flow to %s conflicts with current flow of %s", node, flowDirection));
        }
        if (source.equals(node)) {
            flowDirection = Direction.SOURCE;
        } else {
            flowDirection = Direction.TARGET;
        }
    }

    public void resetFlow() {
        flowDirection = Direction.NONE;
    }

    private void requireConnectedNode(T node) {
        if (!source.equals(node) && !target.equals(node)) {
            throw new IllegalArgumentException(String.format(
                    "Node %s must be either source or target of edge", node));
        }
    }

    @Override
    public String toString() {
        return "UnitFlowEdge{" +
                "source=" + source +
                ", target=" + target +
                ", flowDirection=" + flowDirection +
                '}';
    }
}