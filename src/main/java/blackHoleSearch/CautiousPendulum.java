package blackHoleSearch;
import io.jbotsim.core.*;
import io.jbotsim.ui.JViewer;
import java.util.*;

public class CautiousPendulum extends Node {
    Random random = new Random();
    Boolean isBlackHole;
    Boolean isExplored;
    final int SIZE = 8;

    public void onStart() {
        // initialize three agents at the home base
        if (getID() == 0) {
            Message leader = new Message(Agent.LEADER);
            Message avanGuard = new Message(Agent.AVANGUARD);
            Message retroGuard = new Message(Agent.RETROGUARD);
            // send avanGuard to the right
            sendThroughLink(Orientation.CLOCKWISE, avanGuard);
            // send retroGuard to the left
            sendThroughLink(Orientation.COUNTERCLOCKWISE, retroGuard);
        }
    }

    public void onClock() {

    }

    public void setBlackHole(Boolean blackHole) {
        isBlackHole = blackHole;
    }

    private void sendThroughLink(Orientation orientation, Message message) {
        ArrayList<Link> neighbors = (ArrayList<Link>) getLinks();
        if (orientation == Orientation.COUNTERCLOCKWISE) { // send message left
            for (Link link: neighbors) {
                // the link is not missing
                if ((link.getClass() == DynamicLink.class) && (!((DynamicLink) link).getIsMissing())) {
                    Node node = link.getOtherEndpoint(this);
                    if (node.getID() == (getID() - 1) % SIZE) {
                        send(node, message);
                        break;
                    }
                }
            }
        }
        else {
            for (Link link: neighbors) {
                // the link is not missing
                if ((link.getClass() == DynamicLink.class) && (!((DynamicLink) link).getIsMissing())) {
                    Node node = link.getOtherEndpoint(this);
                    if (node.getID() == (getID() + 1) % SIZE) {
                        send(node, message);
                        break;
                    }
                }
            }
        }
    }
}
