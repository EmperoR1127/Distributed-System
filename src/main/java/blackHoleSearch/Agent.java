package blackHoleSearch;

public class Agent {
    private AgentType type;
    private int avanCounter; // only valid for leader
    private int meetRetro; // only valid for leader
    private int retroCounter; // only valid for leader
    private int numOfMoves;

    public Agent(AgentType aType) {
        type = aType;
        if (aType == AgentType.LEADER) {
            avanCounter = 0;
            meetRetro = -1;
            retroCounter = 0;
        }
    }

    public AgentType getType() {
        return type;
    }

    /**
     * This method is used to get the total number of move made by this node.
     * @return int total number of moves made by this node.
     */
    public int getNumOfMoves() {
        return numOfMoves;
    }

    /**
     * This method is used to add one move to the total number of moves made by this node.
     * @return void
     */
    public void addNumOfMoves() {
        numOfMoves += 1;
    }

    public void addAvanCounter() {
        avanCounter += 1;
    }

    public void resetAvanCounter() {
        avanCounter = 0;
    }
    /**
    * This method is used to add one to the counter which keeps the number of time units
     * collapse since the leader and retroGuard meet.
     * @return void
     */
    public void addRetroCounter() {
        retroCounter += 1;
    }

    /**
     * This method is used to reset the counter which keeps the number of time units collapse
     * since the leader and retroGuard meet.
     * @return void
     */
    public void resetRetroCounter() {
        retroCounter = 0;
    }
    /**
     * This method is used to add one to the number of meetings between the leader
     * and the retroGuard.
     * @return void
     */
    public void addMeetRetro() {
        meetRetro += 1;
    }

    public int getMeetRetro() {
        return meetRetro;
    }

    public int getRetroCounter() { return retroCounter;}
    /**
     * This method is used to check whether the avanGuard fails to report.
     * @return Boolean true if the avanGuard fails to report; false otherwise.
     */
    public Boolean avanFailsToReport() {
        return avanCounter > 2;
    }

    public  Boolean retroFailsToReport(int i) {
        return 2 * (meetRetro + i + 1) < retroCounter;
    }

    public Boolean equals(Agent other) {
        if (this == other) {
            return true;
        }
        return getType() == other.getType();
    }
}
