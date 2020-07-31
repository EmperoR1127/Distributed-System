package blackHoleSearch;
import io.jbotsim.core.*;
import io.jbotsim.ui.JViewer;
import java.util.*;

public class CautiousPendulum<agentPool> extends Node {
    private Random random = new Random();
    private Boolean isBlackHole;
    private Boolean isExplored;
    private Boolean isTerminated;
    private ArrayList<Move> moves;
    private final int SIZE = 8;

    public void onStart() {
        moves = new ArrayList<>();
        // initialize three agents at the home base
        if (getID() == 0) {
            moves.add(new Move(new Agent(AgentType.LEADER), null));
            moves.add(new Move(new Agent(AgentType.AVANGUARD), null));
            moves.add(new Move(new Agent(AgentType.RETROGUARD), null));
        }
    }

    public void onClock() {
        // if the node is the black hole or
        // there's nothing in its mailbox, do nothing
        if (getIsBlackHole() || (moves.size() == 0)) {
            return;
        }
        else if (moves.size() == 1) { // only one agent
            Move move = moves.get(0);
            Agent agent = move.getAgent();
            if (agent.getType() == AgentType.AVANGUARD) { // avanGuard resides at the node
                // the node is unvisited if the avanGuard resides at this node alone
                setIsExplored(true);
                sendThroughLink(agent, Orientation.COUNTERCLOCKWISE);
            }
            else if (agent.getType() == AgentType.RETROGUARD) { // retroGuard resides at the node
                if (!getIsExplored()) { // an unexplored node
                    setIsExplored(true);
                    sendThroughLink(agent, Orientation.CLOCKWISE);
                }
                else { // an explored node, do not need to change orientation
                    sendThroughLink(agent, move.getOrientation());
                }
            }
            else { // leader resides at the node
                if (!isLinkMissing(Orientation.CLOCKWISE)) { // clockwise link is not missing
                    agent.addAvanCounter();
                    if (agent.avanFailsToReport()) { // avanGuard fails to report
                        isTerminated = true; // terminates the algorithm
                    }
                }
                else { // clockwise link is missing
                    agent.addRetroCounter();
                    // TODO check retroGuard
                }
            }
        }
        else if (moves.size() == 3) { // three agents meet at this node
            Agent leader = null;
            Agent avanGuard = null;
            Agent retroGuard = null;
            for (Move m: moves) {
                Agent a = m.getAgent();
                if (a.getType() == AgentType.LEADER) {
                    leader = a;
                }
                else if (a.getType() == AgentType.AVANGUARD) {
                    avanGuard = a;
                }
                else {
                    retroGuard = a;
                }
            }
            // do not need to check the availability of the clockwise link
            // since sendThroughLink has no effect when the link is missing
            leader.resetAvanCounter();
            leader.addAvanCounter();
            sendThroughLink(avanGuard, Orientation.CLOCKWISE);
            leader.addMeetRetro();
            sendThroughLink(retroGuard, Orientation.COUNTERCLOCKWISE);
        }
        else { // two agents meet at this node

        }
    }

    public void setIsExplored(Boolean explored) {
        isExplored = explored;
    }

    public Boolean getIsExplored() {
        return isExplored;
    }

    public void setBlackHole(Boolean blackHole) {
        isBlackHole = blackHole;
    }

    public Boolean getIsBlackHole() {
        return isBlackHole;
    }

    public Boolean getIsTerminated() {
        return isTerminated;
    }

    public ArrayList<Move> getMoves() {
        return moves;
    }

    private Boolean isLinkMissing(Orientation orientation) {
        return checkLink(orientation) == null;
    }

    private Link checkLink(Orientation orientation) {
        ArrayList<Link> links = (ArrayList<Link>) getLinks();
        Link res = null;
        if (orientation == Orientation.COUNTERCLOCKWISE) { // send message left
            for (Link link: links) {
                // the link is not missing
                if ((link.getClass() == DynamicLink.class) && (!((DynamicLink) link).getIsMissing())
                        && (link.getOtherEndpoint(this).getID() == (getID() - 1) % SIZE)) {
                    res = link;
                    break;
                }
            }
        }
        else { // send message right
            for (Link link: links) {
                // the link is not missing
                if ((link.getClass() == DynamicLink.class) && (!((DynamicLink) link).getIsMissing())
                        && (link.getOtherEndpoint(this).getID() == (getID() + 1) % SIZE)) {
                    res = link;
                    break;
                }
            }
        }
        return res;
    }

    private void sendThroughLink(Agent agent, Orientation orientation) {
        Link link = checkLink(orientation);
        if (link == null) { // the link is missing
            return;
        }
        Node node = link.getOtherEndpoint(this);
        // ready to send the agent
        agent.addNumOfMoves();
        for (Move m: moves) { // delete this agent in this node
            if (m.getAgent() == agent) {
                moves.remove(m);
                break;
            }
        }
        // add a move to the destination
        if (node.getClass() == CautiousPendulum.class) {
            ((CautiousPendulum) node).getMoves().add(new Move(agent, orientation));
        }
    }
}
