package leaderElection;
import io.jbotsim.core.Link;
import io.jbotsim.core.Node;

public class LabeledLink extends Link {
    private int label;

    LabeledLink(Node a, Node b, int label){
        super(a, b);
        this.label = label;
    }

    public int getLabel(){
        return label;
    }

    public String toString(){
        return super.toString() + " Label: " + label;
    }
}
