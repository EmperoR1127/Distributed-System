package leaderElection;

import java.util.ArrayList;

public class MessageContent {
    private int value;
    private int stage;
    private ArrayList<Integer> source;
    private ArrayList<Integer> dest;

    MessageContent(int value, int stage, ArrayList<Integer> source, ArrayList<Integer> dest){
        this.value = value;
        this.stage = stage;
        this.source = source;
        this.dest = dest;
    }

    public int getValue(){
        return value;
    }

    public int getStage(){
        return stage;
    }

    public ArrayList<Integer> getSource(){
        return source;
    }

    public ArrayList<Integer> getDest(){
        return dest;
    }
}
