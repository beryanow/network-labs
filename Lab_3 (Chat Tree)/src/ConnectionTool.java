import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ConnectionTool implements Runnable {
    private final ArrayList<InetSocketAddress> nodes;
    private final ConcurrentLinkedQueue<Message> messages;
    private final ArrayBlockingQueue<Message> messagesQueue;
    private final ConcurrentHashMap<InetSocketAddress, Long> nodesInfo;
    InetSocketAddress nodeReceiveAddress;
    InetSocketAddress nodeSendAddress;
    int lossPercentage;
    DatagramSocket socket;
    ReceiveTool receiveTool;
    SendTool sendTool;
    ResendTool resendTool;

    ConnectionTool(InetSocketAddress address, int lossPercentage) throws SocketException {
        nodesInfo = new ConcurrentHashMap<>();
        messagesQueue = new ArrayBlockingQueue<>(100);
        messages = new ConcurrentLinkedQueue<>();
        nodes = new ArrayList<>();
        nodeReceiveAddress = address;
        nodeSendAddress = new InetSocketAddress(nodeReceiveAddress.getHostString(), nodeReceiveAddress.getPort() + 1);
        socket = new DatagramSocket(nodeSendAddress);
        this.lossPercentage = lossPercentage;
        receiveTool = new ReceiveTool(nodeReceiveAddress, lossPercentage, nodes, socket, messages, messagesQueue, nodesInfo);
        sendTool = new SendTool(messagesQueue, nodes, nodeReceiveAddress, socket);
        resendTool = new ResendTool(messagesQueue, sendTool, socket, nodesInfo, nodes);
    }

    void addNode(InetSocketAddress node) {
        synchronized (nodes) {
            nodes.add(node);
        }
    }

    void makeConnection(String data, String type, InetSocketAddress parentAddress) throws IOException {
        UUID guid = UUID.randomUUID();
        Message message = new Message(data, type, nodeReceiveAddress, parentAddress, guid);
        sendTool.sendMessage(socket, message);
    }

    void makeMessage(String word, String type) throws IOException {
        synchronized (nodes) {
            for (InetSocketAddress node : nodes) {
                UUID guid = UUID.randomUUID();
                Message message = new Message(word, type, nodeReceiveAddress, node, guid);
                messagesQueue.add(message);
                sendTool.sendMessage(socket, message);
            }
        }
    }

    @Override
    public void run() {
        Thread receiveThread = new Thread(receiveTool);
        receiveThread.start();

        Thread resendThread = new Thread(resendTool);
        resendThread.start();

        while (true) {
            Iterator<Message> it;
            it = messages.iterator();
            while (it.hasNext()) {
                Message message = it.next();

                switch (message.getType()) {
                    case "CONNECT":
                        if (!nodeReceiveAddress.equals(message.getSender().getAddress().toString().substring(1))) {
                            System.out.println("CONNECTED NODE -> " + message.getSender().getAddress().toString().substring(1));
                            it.remove();
                        }
                        break;
                    case "ACK":
                        System.out.println("RECEIVED ACK -> " + message.getSender().getAddress().toString().substring(1) + " " + message.getReceiver().getAddress().toString().substring(1) + "GUID: " + message.getGuid());
                        UUID messageGuid = message.getGuid();
                        for (Message messageInQueue : messagesQueue) {
                            if (messageInQueue.getGuid().equals(messageGuid)) {
                                System.out.println("MESSAGE REMOVED" + message.getData() + " " + message.getSender() + " " + message.getReceiver());
                                messagesQueue.remove(messageInQueue);
                                break;
                            }
                        }
                        it.remove();
                        break;
                    case "ACKSEND":
                        message.setType("ACK");
                        System.out.println("REQUEST FOR ACK -> " + message.getSender().getAddress().toString().substring(1) + " " + message.getReceiver().getAddress().toString().substring(1) + message.getGuid());
                        try {
                            sendTool.sendMessage(socket, message);
                        } catch (IOException e) {
                            System.err.println(e.getMessage());
                        }
                        it.remove();
                        break;
                    case "MESSAGE":
                        try {
                            sendTool.makeMoreMessages(message.getData(), message.getType(), message.getSender());
                            it.remove();
                        } catch (IOException e) {
                            System.err.println(e.getMessage());
                        }
                }
            }
        }
    }
}
