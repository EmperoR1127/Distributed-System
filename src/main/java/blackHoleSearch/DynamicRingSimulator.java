package blackHoleSearch;

import io.jbotsim.ui.JViewer;

public class DynamicRingSimulator {
    private int numOfSimulation;
    private int sizeOfRing;

    public DynamicRingSimulator(int num, int size) {
        numOfSimulation = num;
        sizeOfRing = size;
    }

    public void simulate() {
        int totalNumOfMoves = 0;
        int totalNumOfRounds = 0;
        for (int i = 0; i < numOfSimulation; i++) {
            DynamicRing ring = new DynamicRing(sizeOfRing);
            ring.setScheduler(new DynamicScheduler());
            ring.start();
            totalNumOfMoves += ring.getNumOfMoves();
            totalNumOfRounds += ring.getRound();
        }
        System.out.println(reportResult(totalNumOfMoves, totalNumOfRounds));
    }

    private String reportResult(int i, int j) {
        return "Run " + numOfSimulation + " time(s) simulation with a dynamic ring of size " + sizeOfRing + ". "
                + "The average number of moves made by the agents are " + i / numOfSimulation + ". "
                + "The average number of rounds in each simulation are " + j / numOfSimulation + ".";
    }

    public static void main(String[] args) {
        DynamicRingSimulator simulator = new DynamicRingSimulator(20, 16);
        simulator.simulate();
    }
}
