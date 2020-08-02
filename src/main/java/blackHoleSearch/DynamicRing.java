package blackHoleSearch;

import io.jbotsim.core.Color;
import io.jbotsim.core.Link;
import io.jbotsim.core.Node;
import io.jbotsim.core.Topology;
import io.jbotsim.ui.JViewer;

import java.util.List;
import java.util.Random;

public class DynamicRing extends Topology {
    private final int BASEX = 500;
    private final int BASEY = 500;
    private final int PARAMETER = 200;
    private Random random;
    private int ringSize;
    private int prevMissing;
    private int round;
    private Agent leader;
    private Agent avanGuard;
    private Agent retroGuard;

    public DynamicRing(int size) {
        super();
        random = new Random();
        ringSize = size;
        leader = new Agent(AgentType.LEADER);
        avanGuard = new Agent(AgentType.AVANGUARD);
        retroGuard = new Agent(AgentType.RETROGUARD);
        // add nodes to the topology
        for (int i = 0; i < ringSize; i++) {
            CautiousPendulum node = new CautiousPendulum(ringSize);
            double angle = 90 - (2 * Math.PI * i) / ringSize;
            node.setID(i);
            if (i == 0) { // initialize three agents at the home base
                node.getBegin().add(new Move(leader, null));
                node.getBegin().add(new Move(avanGuard, null));
                node.getBegin().add(new Move(retroGuard, null));
                node.setIsExplored(true);
            }
            else { // set unexplored nodes yellow
                node.setColor(Color.YELLOW);
            }
            // y-axis in java is different as in real world
            addNode(BASEX + PARAMETER * Math.cos(angle), BASEY - PARAMETER * Math.sin(angle), node);
        }
        // add links to the topology
        for (int i = 0; i < ringSize; i++) {
            addLink(new DynamicLink(getNodes().get(i), getNodes().get((i + 1) % ringSize)));
        }

        // set the black hole randomly
        Node blackHole = getNodes().get(random.nextInt(ringSize - 1) + 1);
        if (blackHole.getClass() == CautiousPendulum.class) {
            ((CautiousPendulum) blackHole).setBlackHole(true);
        }
    }

    public void onClock() {
        // check whether the black hole has been found
        for (Node node: getNodes()) {
            if (node.getClass() == CautiousPendulum.class) {
                if (((CautiousPendulum) node).getIsTerminated()) {
                    System.out.println(report());
                    pause(); // find the black hole
                    return;
                }
            }
        }
        // set a link missing
        setLinkMissing();
    }

    /**
     * This method is used to randomly choose a link and set it missing, after set
     * the link in the previous round appear. It's possible to operate the same
     * link in two consecutive rounds.
     */
    private void setLinkMissing() {
        // reset the previous link
        Link prev = getLinks().get(prevMissing);
        if (prev.getClass() == DynamicLink.class) {
            ((DynamicLink) prev).setIsMissing(false);
        }
        prev.setColor(Color.BLACK);
        // begin a round
        addRound();
        System.out.println("Round " + getRound() + " begins");
        // select a link randomly and set it missing
        int missing = random.nextInt(ringSize);
        Link link = getLinks().get(missing);
        if (link.getClass() == DynamicLink.class) {
            ((DynamicLink) link).setIsMissing(true);
        }
        link.setColor(Color.RED);
        // report the missing link
        List<Node> nodes = link.endpoints();
        System.out.println("Link between node " + nodes.get(0).getID() + " and node "
                           + nodes.get(1).getID() + " is missing.");
        prevMissing = missing;
    }

    private void addRound() {
        round += 1;
    }

    private int getRound() {
        return round;
    }

    private int getNumOfMoves() {
        return leader.getNumOfMoves() + avanGuard.getNumOfMoves() + retroGuard.getNumOfMoves();
    }

    private String report() {
        int r = getRound();
        String end = " rounds";
        if (r == 1) {
            end = " round";
        }
        return "The algorithm terminates in " + r + end + " with " + getNumOfMoves() + " moves.";
    }

    public static void main(String[] args) {
        DynamicRing ring = new DynamicRing(8);
        ring.setScheduler(new DynamicScheduler());
        ring.setTimeUnit(2000);
        new JViewer(ring);
        ring.step();
    }
}
