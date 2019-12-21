import javax.xml.crypto.Data;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ReceiveTool implements Runnable {
    private InetSocketAddress nodeReceiveAddress;
    private final ArrayList<InetSocketAddress> nodes;
    private final ConcurrentLinkedQueue<Message> messages;
    private final ArrayBlockingQueue<Message> messagesQueue;
    private final ConcurrentHashMap<InetSocketAddress, Long> nodesInfo;
    private int lossPercentage;
    DatagramSocket socket;
    DatagramSocket additionalSocket;

    ReceiveTool(InetSocketAddress address, int lossPercentage, ArrayList<InetSocketAddress> nodes, DatagramSocket additionalSocket, ConcurrentLinkedQueue<Message> messages, ArrayBlockingQueue<Message> messagesQueue, ConcurrentHashMap<InetSocketAddress, Long> nodesInfo) throws SocketException {
        nodeReceiveAddress = address;
        socket = new DatagramSocket(nodeReceiveAddress);
        this.lossPercentage = lossPercentage;
        this.nodes = nodes;
        this.messagesQueue = messagesQueue;
        this.additionalSocket = additionalSocket;
        this.messages = messages;
        this.nodesInfo = nodesInfo;
    }

    @Override
    public void run() {
        byte[] buf = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);

        Random randomPercentage = new Random(System.currentTimeMillis());
        try {
            while (true) {
                try {
                    socket.receive(packet);
                } catch (IOException e) {
                    System.err.println(e.getMessage());
                }

                String data = new String(buf, 0, packet.getLength(), StandardCharsets.UTF_8);
                String[] messageParts = data.split(":", 4);

                String messageType = messageParts[0];
                int messagePort = Integer.parseInt(messageParts[1]);

                InetSocketAddress senderAddress = new InetSocketAddress(packet.getAddress(), messagePort);
                synchronized (nodes) {
                    if ((!nodes.contains(senderAddress)) && (!senderAddress.equals(nodeReceiveAddress))) {
                        nodes.add(senderAddress);
                        nodesInfo.put(senderAddress, System.currentTimeMillis());
                    } else if (nodes.contains(senderAddress)) {
                        nodesInfo.put(senderAddress, System.currentTimeMillis());
                    }
                }

                String messageData = messageParts[2];
                UUID messageGuid = UUID.fromString(messageParts[3]);

                Message message = new Message(messageData, messageType, senderAddress, nodeReceiveAddress, messageGuid);

                if ((randomPercentage.nextInt(100) < lossPercentage) && (!messageType.equals("CONNECT"))) {
                    continue;
                }

                messages.add(message);

                if ((!messageType.equals("ACK")) && (!messageType.equals("CONNECT") && (!messageType.equals("ACKSEND")))) {
                    Message ackMessageSend = new Message(" ", "ACKSEND", nodeReceiveAddress, senderAddress, messageGuid);
                    messages.add(ackMessageSend);

                    if (!message.getReceiver().equals(nodeReceiveAddress)) {
                        messagesQueue.add(message);
                    }
                }

                System.out.println("RECEIVED MESSAGE -> " + message.getData() + " -> " + message.getSender().getAddress().toString().substring(1) + " -> " + message.getType());
            }
        } finally {
            socket.close();
            additionalSocket.close();
        }
    }
}
