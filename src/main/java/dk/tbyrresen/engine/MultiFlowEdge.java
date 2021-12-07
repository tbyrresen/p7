package dk.tbyrresen.engine;

import org.springframework.lang.Nullable;

// Simple wrapper class to enable arbitrary source and target nodes by keeping flow edges
// in both directions. Note that if we are currently not using any flow edge, we are always
// free to pick the one that makes a flow possible
public class MultiFlowEdge<T> {
    @Nullable private UnitFlowEdge<T> used;
    private final UnitFlowEdge<T> first;
    private final UnitFlowEdge<T> second;

    public MultiFlowEdge(T source, T target) {
        first = new UnitFlowEdge<>(source, target);
        second = new UnitFlowEdge<>(target, source);
    }

    public boolean canFlowTo(T node) {
        if (used != null) {
            return used.canFlowTo(node);
        }
        return first.canFlowTo(node) || second.canFlowTo(node); // always true?
    }

    public void flowTo(T node) {
        if (used != null) {
            used.flowTo(node);
        } else {
            if (first.canFlowTo(node)) {
                first.flowTo(node);
                used = first;
            } else if (second.canFlowTo(node)) {
                second.flowTo(node);
                used = second;
            } else {
                throw new IllegalStateException(String.format(
                        "MultiFlowEdge %s can't flow to node %s", this, node));
            }
        }
    }

    public T getOppositeOf(T node) {
        return first.getOppositeOf(node);
    }

    public UnitFlowEdge<T> getFirst() {
        return first;
    }

    public UnitFlowEdge<T> getSecond() {
        return second;
    }

    @Override
    public String toString() {
        return "MultiFlowEdge{" +
               "used=" + used +
               ", first=" + first +
               ", second=" + second +
               '}';
    }
}
