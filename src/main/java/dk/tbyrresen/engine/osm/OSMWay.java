package dk.tbyrresen.engine.osm;

import java.util.Objects;
import java.util.Queue;

public class OSMWay {
    private final Queue<Long> nodeRefs;

    public OSMWay(Queue<Long> nodeRefs) {
        this.nodeRefs = nodeRefs;
    }

    public Queue<Long> getNodeRefs() {
        return nodeRefs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OSMWay osmWay = (OSMWay) o;
        return Objects.equals(nodeRefs, osmWay.nodeRefs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeRefs);
    }
}
