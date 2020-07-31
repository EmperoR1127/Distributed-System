package blackHoleSearch;

import io.jbotsim.core.Link;
import io.jbotsim.core.Node;

public class DynamicLink extends Link {
    private Boolean isMissing;

    public DynamicLink(Node a, Node b) {
        super(a, b);
        isMissing = false;
    }

    public void setIsMissing(Boolean missing) {
        isMissing = missing;
    }

    public Boolean getIsMissing() {
        return isMissing;
    }
}
