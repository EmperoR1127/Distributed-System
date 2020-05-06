import io.jbotsim.core.*;
import io.jbotsim.ui.JViewer;
import java.util.ArrayList;

public class DepthFirstSearch extends Node{
    boolean visited;
    ArrayList<Node> neighbors;
    Node nodeToSend;
    Node parent;

    @Override
    public void onStart() {
        // JBotSim executes this method on each node upon initialization
        visited = false;
        setColor(null);
    }

    @Override
    public void onSelection() {
        // JBotSim executes this method on a selected node
        visited = true;
        neighbors = new ArrayList(getNeighbors());  //find all the neighbors
        setColor(Color.RED);
        if (neighbors.size() != 0){
            nodeToSend = neighbors.remove(0);
            send(nodeToSend, new Message("Token"));
        }
    }

    @Override
    public void onMessage(Message message) {
        // JBotSim executes this method on a node every time it receives a message
        if(message.getContent() == "Token"){
            if(visited == false){ // receive the token for the first time
                visited = true;
                neighbors = new ArrayList(getNeighbors());
                setColor(Color.RED);
                parent = message.getSender();
                Link treeLink = getInLinkFrom(parent);  //find the tree link
                System.out.println(treeLink.toString());
                treeLink.setColor(Color.GREEN);
                if(neighbors.size() != 0){
                    neighbors.remove(parent);
                }
                sendNext(message);
            }
            else{
                send(message.getSender(), new Message("Return Token")); //already visited, send back the token
            }
        }
        else{
            sendNext(new Message("Token")); //find next node to send
        }

    }

    private void sendNext(Message message){
        if (neighbors.size() != 0) {    //there is at least one unvisited node
            nodeToSend = neighbors.remove(0);
            send(nodeToSend, message);
        }
        else{
            send(parent, new Message("Return Token"));  //send the token back to parent
        }
    }

    public static void main(String[] args) {
        Topology tp = new Topology();
        tp.setDefaultNodeModel(DepthFirstSearch.class);
        tp.setTimeUnit(500);
        new JViewer(tp);
        tp.start();
    }
}
