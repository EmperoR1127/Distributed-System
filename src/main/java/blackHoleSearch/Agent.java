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

    public int getNumOfMoves() {
        return numOfMoves;
    }

    public void addNumOfMoves() {
        this.numOfMoves += 1;
    }

    public void addAvanCounter() {
        this.avanCounter += 1;
    }

    public void resetAvanCounter() {
        this.avanCounter = 0;
    }

    public void addRetroCounter() {
        this.retroCounter += 1;
    }

    public void resetRetroCounter() {
        this.retroCounter = 0;
    }

    public void addMeetRetro() {
        this.meetRetro += 1;
    }

    public Boolean avanFailsToReport() {
        return this.avanCounter == 2;
    }
}
