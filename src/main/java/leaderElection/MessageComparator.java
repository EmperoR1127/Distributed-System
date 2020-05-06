package leaderElection;
import java.util.Comparator;
import io.jbotsim.core.Message;

public class MessageComparator implements Comparator<Message> {
    @Override
    public int compare(Message m1, Message m2) {
        MessageContent content1 = (MessageContent)m1.getContent();
        MessageContent content2 = (MessageContent)m2.getContent();
        if(content1.getStage() < content2.getStage()){
            return -1;
        }
        else if(content1.getStage() > content2.getStage()){
            return 1;
        }
        else{
            return 0;
        }
    }
}
