package dk.tbyrresen.engine;

// Simple class to contain information regarding recomputations in an ND-tree. Used for evaluation
public class NestedDissectionTreeRecomputation {
    private final int depth;
    private final long recomputationTimeMs;

    public NestedDissectionTreeRecomputation(int depth, long recomputationTimeMs) {
        this.depth = depth;
        this.recomputationTimeMs = recomputationTimeMs;
    }

    public int getDepth() {
        return depth;
    }

    public long getRecomputationTimeMs() {
        return recomputationTimeMs;
    }
}
