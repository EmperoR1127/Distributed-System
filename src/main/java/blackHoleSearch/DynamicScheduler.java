package blackHoleSearch;

import io.jbotsim.core.*;
import io.jbotsim.core.event.*;
import java.util.List;

public class DynamicScheduler extends Scheduler {
    public void onClock(Topology tp, List<ClockListener> expiredListeners) {
        // Delivers messages first
        tp.getMessageEngine().onClock();
        // Then to the topology itself
        tp.onClock();
        // Then give the hand to the nodes
        for (Node node : tp.getNodes())
            node.onPreClock();
        for (Node node : tp.getNodes())
            node.onClock();
        for (Node node : tp.getNodes())
            node.onPostClock();
        // And finally the other listeners
        for (ClockListener cl : expiredListeners)
            cl.onClock();
    }
}
