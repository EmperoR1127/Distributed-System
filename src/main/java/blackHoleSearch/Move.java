package blackHoleSearch;

public class Move {
    private Agent agent;
    private Orientation orientation;

    public Move(Agent a, Orientation o) {
        agent = a;
        orientation = o;
    }

    public Agent getAgent() {
        return agent;
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public void setOrientation(Orientation orientation) {
        this.orientation = orientation;
    }
}
