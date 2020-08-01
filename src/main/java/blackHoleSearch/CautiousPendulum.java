package blackHoleSearch;
import io.jbotsim.core.*;
import java.util.*;

public class CautiousPendulum extends Node {
    private Boolean isBlackHole;
    private Boolean isExplored;
    private Boolean isTerminated;
    private ArrayList<Move> begin; // moves at the beginning of each round
    private int size;
    private Move leaderMove = null;
    private Move avanGuardMove = null;
    private Move retroGuardMove = null;

    public CautiousPendulum(int s) {
        super();
        isBlackHole = false;
        isExplored = false;
        isTerminated = false;
        begin = new ArrayList<>();
        size = s;
    }

    @Override
    public void onStart() {
        // initialize three agents at the home base
        if (getID() == 0) {
            begin.add(new Move(new Agent(AgentType.LEADER), null));
            begin.add(new Move(new Agent(AgentType.AVANGUARD), null));
            begin.add(new Move(new Agent(AgentType.RETROGUARD), null));
            setIsExplored(true); // set the home base explored
        }
    }

    @Override
    public void onPreClock() {
        // check which agent resides on the node
        for (Move move: begin) {
            if (move.getAgent().getType() == AgentType.AVANGUARD) { // avanGuard resides at the node
                avanGuardMove = move;
                System.out.println("The avanGuard resides on node " + getID());
            }
            else if (move.getAgent().getType() == AgentType.RETROGUARD) { // retroGuard resides at the node
                retroGuardMove = move;
                System.out.println("The retroGuard resides on node " + getID());
            }
            else { // leader resides at the node
                leaderMove = move;
                System.out.println("The leader resides on node " + getID());
                System.out.println("Meet retroGuard " + leaderMove.getAgent().getMeetRetro() + " times");
                System.out.println("retroCounter is " + leaderMove.getAgent().getRetroCounter());
            }
        }
    }

    public void onClock() {
        // the node is the black hole or there's no agent resides on this node
        if (getIsBlackHole() || (leaderMove == null && avanGuardMove == null && retroGuardMove == null)) {
            // do nothing
        }
        // avanGuard resides on the node alone
        else if (leaderMove == null && avanGuardMove != null && retroGuardMove == null) {
            setIsExplored(true);
            sendThroughLink(avanGuardMove.getAgent(), Orientation.COUNTERCLOCKWISE);
        }
        // retroGuard resides on the node alone
        else if (leaderMove == null && avanGuardMove == null && retroGuardMove != null) {
            if (!getIsExplored()) { // an unexplored node
                setIsExplored(true);
                sendThroughLink(retroGuardMove.getAgent(), Orientation.CLOCKWISE);

            }
            else { // an explored node, do not need to change orientation
                sendThroughLink(retroGuardMove.getAgent(), retroGuardMove.getOrientation());
            }
        }
        // leader resides on the node alone
        else if (leaderMove != null && avanGuardMove == null && retroGuardMove == null) {
            if (!isLinkMissing(Orientation.CLOCKWISE)) { // clockwise link is not missing
                leaderMove.getAgent().addAvanCounter();
            }
            else { // clockwise link is missing
                leaderMove.getAgent().addRetroCounter();
            }
            reportBlackHole(leaderMove.getAgent());
        }
        // leader and avanGuard reside on the node together
        else if (leaderMove != null && avanGuardMove != null && retroGuardMove == null) {
            Agent leader = leaderMove.getAgent();
            Agent avanGuard = avanGuardMove.getAgent();
            leader.resetAvanCounter();
            if (!isLinkMissing(Orientation.CLOCKWISE)) { // clockwise link is not missing
                leader.addAvanCounter();
                sendThroughLink(avanGuard, Orientation.CLOCKWISE);
                Link clockwise = checkLink(Orientation.CLOCKWISE);
                assert(clockwise != null);
                Node clowckwiseNeighbor = clockwise.getOtherEndpoint(this);
                // send the leader to the clockwise neighbor if it is explored
                if (clowckwiseNeighbor.getClass() == CautiousPendulum.class) {
                    if (((CautiousPendulum) clowckwiseNeighbor).getIsExplored()) {
                        sendThroughLink(leader, Orientation.CLOCKWISE);
                    }
                }
            }
            else { // clockwise link is missing
                leader.addRetroCounter();
                reportBlackHole(leader);
            }
        }
        // leader and retroGuard reside on the node together
        else if (leaderMove != null && avanGuardMove == null && retroGuardMove != null) {
            Agent leader = leaderMove.getAgent();
            Agent retroGuard = retroGuardMove.getAgent();
            leader.addMeetRetro();
            leader.resetRetroCounter();
            if (!isLinkMissing(Orientation.CLOCKWISE)) { // clockwise link is not missing
                leader.addAvanCounter();
                reportBlackHole(leader);
            }
            if (!isLinkMissing(Orientation.COUNTERCLOCKWISE)) {// counter-clockwise link is not missing
                leader.addRetroCounter();
                sendThroughLink(retroGuard, Orientation.COUNTERCLOCKWISE);
            }
        }
        else { // all the three agents reside on the node together
            assert(leaderMove != null);
            assert(avanGuardMove != null);
            assert(retroGuardMove != null);
            Agent leader = leaderMove.getAgent();
            Agent avanGuard = avanGuardMove.getAgent();
            Agent retroGuard = retroGuardMove.getAgent();
            leader.addAvanCounter();
            leader.resetAvanCounter();
            leader.addMeetRetro();
            leader.resetRetroCounter();
            if (!isLinkMissing(Orientation.CLOCKWISE)) { // clockwise link is not missing
                sendThroughLink(avanGuard, Orientation.CLOCKWISE);
                Link clockwise = checkLink(Orientation.CLOCKWISE);
                assert(clockwise != null);
                Node clowckwiseNeighbor = clockwise.getOtherEndpoint(this);
                // send the leader to the clockwise neighbor if it is explored
                if (clowckwiseNeighbor.getClass() == CautiousPendulum.class) {
                    if (((CautiousPendulum) clowckwiseNeighbor).getIsExplored()) {
                        sendThroughLink(leader, Orientation.CLOCKWISE);
                    }
                }
            }
            else { // clockwise link is missing
                leader.addRetroCounter();
            }
            sendThroughLink(retroGuard, Orientation.COUNTERCLOCKWISE);
        }
    }

    public void onPostClock() {
        leaderMove = null;
        avanGuardMove = null;
        retroGuardMove = null;
    }

    public void setIsExplored(Boolean explored) {
        isExplored = explored;
        if (isExplored) {
            setColor(Color.GREEN);
        }
    }

    public Boolean getIsExplored() {
        return isExplored;
    }

    public void setBlackHole(Boolean blackHole) {
        isBlackHole = blackHole;
        if (isBlackHole) {
            setColor(Color.BLACK);
        }
    }

    public Boolean getIsBlackHole() {
        return isBlackHole;
    }

    public Boolean getIsTerminated() {
        return isTerminated;
    }

    public ArrayList<Move> getBegin() {
        return begin;
    }

    /**
     * This methods is used to check whether a clockwise or counter-clockwise link is missing.
     * @param orientation the link orientation
     * @return true if the desired link is missing; false otherwise
     */
    private Boolean isLinkMissing(Orientation orientation) {
        return checkLink(orientation) == null;
    }

    /**
     * This method is used to get the clockwise or counter-clockwise link.
     * @param orientation clockwise or counter-clockwise
     * @return the clockwise or counter-clockwise link; null if the desired
     * link is missing.
     */
    private Link checkLink(Orientation orientation) {
        ArrayList<Link> links = (ArrayList<Link>) getLinks();
        Link res = null;
        if (orientation == Orientation.COUNTERCLOCKWISE) { // check counter-clockwise link
            for (Link link: links) {
                // the link is not missing
                if ((link.getClass() == DynamicLink.class) && (!((DynamicLink) link).getIsMissing())
                        && (link.getOtherEndpoint(this).getID() == (getID() + size - 1) % size)) {
                    res = link;
                    break;
                }
            }
        }
        else { // check clockwise link
            for (Link link: links) {
                // the link is not missing
                if ((link.getClass() == DynamicLink.class) && (!((DynamicLink) link).getIsMissing())
                        && (link.getOtherEndpoint(this).getID() == (getID() + 1) % size)) {
                    res = link;
                    break;
                }
            }
        }
        return res;
    }

    /**
     * This method is used to send an agent to one of its neighbors.
     * If the link is missing, the agent won't be sent.
     * @param agent The agent about to be sent
     * @param orientation clockwise or counter-clockwise direction.
     */
    private void sendThroughLink(Agent agent, Orientation orientation) {
        Link link = checkLink(orientation);
        if (link == null) { // the link is missing
            return;
        }
        Node node = link.getOtherEndpoint(this);
        // ready to send the agent
        agent.addNumOfMoves();
        for (Move m: begin) { // delete this agent in this node
            if (m.getAgent().equals(agent)) {
                begin.remove(m);
                break;
            }
        }
        // add a move to the destination
        if (node.getClass() == CautiousPendulum.class) {
            ((CautiousPendulum) node).getBegin().add(new Move(agent, orientation));
        }
    }

    private void reportBlackHole(int i) {
        System.out.println("The Black Hole resides on node " + i);
    }

    /**
     * This method is used to check whether the black hole is found.
     * @param agent an agent with AgentType.LEADER
     */
    private void reportBlackHole(Agent agent) {
        assert(agent.getType() == AgentType.LEADER);
        if (agent.avanFailsToReport()) { // avanGuard fails to report
            reportBlackHole(getID() + 1); // the black hole resides on the clockwise neighbor
            isTerminated = true; // terminates the algorithm
        }
        if (agent.retroFailsToReport(getID())) { // retroGuard fails to report
            reportBlackHole(size - agent.getMeetRetro() - 1);
            isTerminated = true;
        }
    }
}
