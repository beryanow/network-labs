import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SendTool {
    private ArrayList<InetSocketAddress> nodes;
    private final ArrayBlockingQueue<Message> messagesQueue;
    InetSocketAddress nodeReceiveAddress;
    DatagramSocket socket;

    SendTool(ArrayBlockingQueue<Message> messagesQueue, ArrayList<InetSocketAddress> nodes, InetSocketAddress nodeReceiveAddress, DatagramSocket socket) {
        this.messagesQueue = messagesQueue;
        this.nodes = nodes;
        this.nodeReceiveAddress = nodeReceiveAddress;
        this.socket = socket;
    }

    void sendMessage(DatagramSocket socket, Message message) throws IOException {
        if (message.getReceiver().equals(nodeReceiveAddress)) return;
        String packData = message.getType() + ":" + nodeReceiveAddress.getPort() + ":" + message.getData() + ":" + message.getGuid();
        byte[] buf = packData.getBytes(StandardCharsets.UTF_8);
        DatagramPacket pack = new DatagramPacket(buf, 0, buf.length, message.getReceiver());
        socket.send(pack);
    }

    void sendMoreMessages(DatagramSocket socket, Message message, InetSocketAddress sendAddress) throws IOException {
        if (message.getReceiver().equals(sendAddress)) return;
        String packData = message.getType() + ":" + nodeReceiveAddress.getPort() + ":" + message.getData() + ":" + message.getGuid();
        byte[] buf = packData.getBytes(StandardCharsets.UTF_8);
        DatagramPacket pack = new DatagramPacket(buf, 0, buf.length, message.getReceiver());
        socket.send(pack);
    }

    void makeMoreMessages(String messageData, String messageType, InetSocketAddress receiveAddress) throws IOException {
        synchronized (nodes) {
            for (InetSocketAddress node : nodes) {
                if (!node.getAddress().equals(receiveAddress.getAddress())) {
                    UUID guid = UUID.randomUUID();
                    Message message = new Message(messageData, messageType, nodeReceiveAddress, node, guid);
                    synchronized (messagesQueue) {
                        messagesQueue.add(message);
                    }
                    sendMoreMessages(socket, message, receiveAddress);
                }
            }
        }

    }
}
