import java.net.InetSocketAddress;
import java.util.UUID;

public class Message {
    private String data;
    private String type;
    private InetSocketAddress sender;
    private InetSocketAddress receiver;
    private UUID guid;
    private int ackAmount = 0;
    private long timeCreation;

    Message(String data, String type, InetSocketAddress sender, InetSocketAddress receiver, UUID guid) {
        this.data = data;
        this.type = type;
        this.sender = sender;
        this.receiver = receiver;
        this.guid = guid;
        timeCreation = System.currentTimeMillis();
    }

    long getTimeCreation() {
        return timeCreation;
    }

    InetSocketAddress getReceiver() {
        return receiver;
    }

    String getType() {
        return type;
    }

    String getData() {
        return data;
    }

    InetSocketAddress getSender() {
        return sender;
    }

    UUID getGuid() {
        return guid;
    }

    void setType(String type) {
        this.type = type;
    }

    void incAckAmount() {
        ackAmount++;
    }

    int getAckAmount() {
        return ackAmount;
    }
}
