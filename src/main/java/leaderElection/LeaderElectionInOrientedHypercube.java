package leaderElection;
import io.jbotsim.core.*;
import io.jbotsim.ui.JViewer;
import java.util.*;

public class LeaderElectionInOrientedHypercube extends Node{
    Random random = new Random();
    boolean isInitiator;
    boolean isInitialized;
    boolean isDuelist;
    int stage;
    int value;
    int nextDuelist;
    ArrayList<Integer> source = new ArrayList<>();
    ArrayList<Integer> dest = new ArrayList<>();
    PriorityQueue<Message> queue;

    @Override
    public void onStart() {
        //System.out.println("Start");
        isInitiator = random.nextBoolean(); //becomes initiator randomly
        stage = 1;
        value = getID();
        source.add(stage);
        isInitialized = false;
        queue = new PriorityQueue(new MessageComparator());   //stores unread msg with respect to stage
        if(isInitiator){
            System.out.println("Node " + getID() + " is a initiator");
            setInitialized();
        }
    }

    @Override
    public void onMessage(Message message) {
        if(!isInitialized){
            setInitialized();
        }
        if(message.getFlag().equals("Match")){  //receives a Match msg
            MessageContent content = (MessageContent) message.getContent();
            if(isDuelist){      //a duelist node receives a Match msg
                if(content.getStage() == stage){    //the Match msg has the same stage
                    System.out.println("Node " + getID() + " receives the Match msg in stage " + stage + " with value " + content.getValue() +", the match begins");
                    processMessage(message);    //the match begin
                }
                else {      //the Match msg has different stage
                    queue.add(message); //put the msg in mailbox
                }
            }
            else{   //a defeated node receives a Match msg
                if(content.getDest().size() == 0){
                    dest.add(nextDuelist);
                }
                int first = dest.get(0);
                dest.remove(0);
                source.add(first);
                source = sortAndCompressPath(source);
                for(Link link : getLinks()){
                    if(link.getClass() == LabeledLink.class && ((LabeledLink) link).getLabel() == first){
                        //forward msg through the link with label "first"
                        sendThroughLink(link, new Message(new MessageContent(content.getValue(), content.getStage(), source, dest), "Match"));
                        System.out.println("Node " + getID() + " forward the Match msg sent by node " + content.getValue() + " through link " + ((LabeledLink) link).getLabel());
                        break;
                    }
                }
            }
        }
        else{   //receives a Notify msg, becomes follower
            System.out.println("Node " + getID() + " becomes follower");
            //the link label that connected to the sender
            int label = ((LabeledLink) getCommonLinkWith(message.getSender())).getLabel();
            for(Link link : getLinks()){
                //send Notify msg through link with larger label
                if(link.getClass() == LabeledLink.class && ((LabeledLink) link).getLabel() > label){
                    sendThroughLink(link, message);
                }
            }
            setColor(Color.GREEN);      //green for followers
        }
    }
    /*
    This method is called by a duelist to process the Match msg.
    If the duelist' id is smaller than the value in the msg, it wins the match and proceed to next stage;
    and if the stage is equal to the getLinks().size(), the node becomes leader and notifies the termination
    otherwise it becomes defeated and thus only responsible for forwarding msg.
    @param message
    the Match msg that about to process
    * */
    private void processMessage(Message message){
        MessageContent content = (MessageContent) message.getContent();
        if(content.getValue() > value){     //win the duel
            if(stage == getLinks().size()){     //becomes leader when wins the match at the last stage
                //sendAll(new Message(new MessageContent(0,0,null, null), "Notify"));
                sendAll(new Message("Termination", "Notify"));  //send notify msg
                System.out.println("Node " + getID() + " becomes the leader");
                setColor(Color.RED);
            }
            else {      //proceed to next stage
                System.out.println("Node " + getID() + " is the winner in stage " + stage);
                stage++;
                source.clear();
                source.add(stage);
                source = sortAndCompressPath(source);   //sort and compress the path
                //find the link with label equals to stage and send a Match msg through that link
                for(Link link : getLinks()){
                    if(link.getClass() == LabeledLink.class && ((LabeledLink) link).getLabel() == stage){
                        sendThroughLink(link, new Message(new MessageContent(value, stage, source, dest), "Match"));
                        System.out.println("New Match msg sent by node " + getID() + " in stage " + stage + " through link " + ((LabeledLink) link).getLabel());
                        break;
                    }
                }
                //check for mailbox to see whether msg with same stage exists
                check();
            }
        }
        else{   //becomes defeated
            nextDuelist = source.get(0);
            isDuelist = false;
            setColor(null);     //no color for defeated nodes
            System.out.println("Node " + getID() + " loses the match, become defeated");
            //check for mailbox and forward the msg
            checkAll();
        }
    }

    /*
    This method is called by a duelist
    If the message queue is not empty and the stage of msg at the head of the queue is equal to the self.stage,
    it is the Match msg the sent by the opponent of the duelist in that stage,
    the method then poll the msg out of the queue and process it.
    Note that the msg is stored in the priority queue with respect to stage, so the msg at the head of queue will
    have the minimum stage.
    * */
    private void check(){
        if(queue.size() != 0){
            //the message in the head of the queue has identical stage
            if(stage == ((MessageContent)queue.peek().getContent()).getStage()){
                Message nextMessage = queue.poll();     //retrieve message in the head of the queue
                processMessage(nextMessage);    //begin the match
            }
        }
    }
    /*
    This method is called by a defeated node
    forward the Match msg in the queue
    * */
    private void checkAll(){
        while(queue.size() != 0){
            Message nextMessage = queue.poll();
            MessageContent nextMessageContent = (MessageContent)nextMessage.getContent();
            if(nextMessageContent.getDest().size() == 0){
                dest.add(nextDuelist);
            }
            int first = dest.get(0);
            dest.remove(0);
            source.add(first);
            source = sortAndCompressPath(source);   //sort and compress the path
            for(Link link : getLinks()){
                if(link.getClass() == LabeledLink.class && ((LabeledLink) link).getLabel() == first){
                    //forward msg through the link with label "first"
                    sendThroughLink(link, new Message(new MessageContent(nextMessageContent.getValue(), nextMessageContent.getStage(), source, dest), "Match"));
                    System.out.println("Node " + getID() + " forward the Match msg sent by node " + nextMessageContent.getValue() + " through link " + ((LabeledLink) link).getLabel());
                    break;
                }
            }
        }
    }

    /*
    Node initialization steps, run only once for each node
    * */
    private void setInitialized(){
        MessageContent messageContent = new MessageContent(value, stage, source, dest);
        Message message = new Message(messageContent, "Match");
        //send msg to duelist in stage 1
        for(Link link : getLinks()){
            if(link.getClass() == LabeledLink.class && ((LabeledLink) link).getLabel() == 1){
                sendThroughLink(link, message);
                break;
            }
        }
        isInitialized = true;
        isDuelist = true;
        setColor(Color.BLUE);   //blue for duelists
    }

    /*
    Send a message through a given link
    @param link, the link through which the msg is send
    @param message, a message that need to send
    * */
    private void sendThroughLink(Link link, Message message){
        if(link == null){
            return;
        }
        Node node = nodeOnTheOtherSideOf(link);     //find node on the other side of the link
        send(node, message);
    }

    /*
    Return the node on the other side of a given link
    @param link, a link which need to be considered
    @return node, the other side of the given link
    * */
    private Node nodeOnTheOtherSideOf(Link link){
        if(link == null){
            return null;
        }
        Node node;
        if(getID() == link.endpoint(0).getID()){
            node = link.endpoint(1);
        }
        else{
            node = link.endpoint(0);
        }
        return node;
    }

    /*
    Sort and compress the path between the duelist and its opponent
    sort the integers first,
    if an integer appears even times, remove those identical integers
    if an integer appears odd times, the integer can appear only once in compressed path
    @param path, the sequence of integers
    @return compressed path
    * */
    private ArrayList<Integer> sortAndCompressPath(ArrayList<Integer> path){
        if(path == null || path.size() == 1){
            return path;
        }
        ArrayList<Integer> newPath;
        HashSet<Integer> pathSet = new HashSet<Integer>();
        path.sort(null);
        for(int i : path){
            if(pathSet.contains(i)){
                pathSet.remove(i);
            }
            else{
                pathSet.add(i);
            }
        }
        newPath = new ArrayList(pathSet);
        newPath.sort(null);
        return newPath;
    }

    public static void main(String[] args) {
        Topology tp = new Topology();
        tp.setDefaultNodeModel(LeaderElectionInOrientedHypercube.class);
        LeaderElectionInOrientedHypercube node_1 = new LeaderElectionInOrientedHypercube();
        LeaderElectionInOrientedHypercube node_2 = new LeaderElectionInOrientedHypercube();
        LeaderElectionInOrientedHypercube node_3 = new LeaderElectionInOrientedHypercube();
        LeaderElectionInOrientedHypercube node_4 = new LeaderElectionInOrientedHypercube();
        LeaderElectionInOrientedHypercube node_5 = new LeaderElectionInOrientedHypercube();
        LeaderElectionInOrientedHypercube node_6 = new LeaderElectionInOrientedHypercube();
        LeaderElectionInOrientedHypercube node_7 = new LeaderElectionInOrientedHypercube();
        LeaderElectionInOrientedHypercube node_8 = new LeaderElectionInOrientedHypercube();
        node_1.setID(1);
        node_2.setID(2);
        node_3.setID(3);
        node_4.setID(4);
        node_5.setID(5);
        node_6.setID(6);
        node_7.setID(7);
        node_8.setID(8);
        Link link_1_1 = new LabeledLink(node_3, node_4, 1);
        Link link_1_2 = new LabeledLink(node_7, node_2, 1);
        Link link_1_3 = new LabeledLink(node_1, node_8, 1);
        Link link_1_4 = new LabeledLink(node_5, node_6, 1);
        link_1_1.setColor(Color.BLUE);
        link_1_2.setColor(Color.BLUE);
        link_1_3.setColor(Color.BLUE);
        link_1_4.setColor(Color.BLUE);
        Link link_2_1 = new LabeledLink(node_3, node_1, 2);
        Link link_2_2 = new LabeledLink(node_4, node_8, 2);
        Link link_2_3 = new LabeledLink(node_7, node_5, 2);
        Link link_2_4 = new LabeledLink(node_2, node_6, 2);
        link_2_1.setColor(Color.ORANGE);
        link_2_2.setColor(Color.ORANGE);
        link_2_3.setColor(Color.ORANGE);
        link_2_4.setColor(Color.ORANGE);
        Link link_3_1 = new LabeledLink(node_3, node_7, 3);
        Link link_3_2 = new LabeledLink(node_4, node_2, 3);
        Link link_3_3 = new LabeledLink(node_1, node_5, 3);
        Link link_3_4 = new LabeledLink(node_8, node_6, 3);
        link_3_1.setColor(Color.GREEN);
        link_3_2.setColor(Color.GREEN);
        link_3_3.setColor(Color.GREEN);
        link_3_4.setColor(Color.GREEN);
        tp.addNode(200, 200, node_3);
        tp.addNode(500, 200, node_4);
        tp.addNode(90, 300, node_7);
        tp.addNode(390, 300, node_2);
        tp.addNode(200, 500, node_1);
        tp.addNode(500, 500, node_8);
        tp.addNode(90, 600, node_5);
        tp.addNode(390, 600, node_6);
        tp.addLink(link_1_1);
        tp.addLink(link_1_2);
        tp.addLink(link_1_3);
        tp.addLink(link_1_4);
        tp.addLink(link_2_1);
        tp.addLink(link_2_2);
        tp.addLink(link_2_3);
        tp.addLink(link_2_4);
        tp.addLink(link_3_1);
        tp.addLink(link_3_2);
        tp.addLink(link_3_3);
        tp.addLink(link_3_4);
        tp.setTimeUnit(2000);
        new JViewer(tp);
        tp.step();
    }
}
