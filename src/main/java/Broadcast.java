import io.jbotsim.core.Message;
import io.jbotsim.core.Color;
import io.jbotsim.core.Node;
import io.jbotsim.core.Topology;
import io.jbotsim.ui.JViewer;

public class Broadcast extends Node {

    boolean informed;

    @Override
    public void onStart() {
        informed = false;
        setColor(null);
    }

    @Override
    public void onSelection() {
        informed = true;
        setColor(Color.RED);
        sendAll(new Message("My message"));
    }

    @Override
    public void onMessage(Message message) {
        if (! informed) {
            informed = true;
            setColor(Color.RED);
            sendAll(new Message(message.getContent()));
        }
    }

    public static void main(String[] args){
        Topology tp = new Topology();
        tp.setDefaultNodeModel(Broadcast.class);
        tp.setTimeUnit(500);
        new JViewer(tp);
        tp.start();
    }
}