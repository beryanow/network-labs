import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class ResendTool implements Runnable {
    private final ArrayBlockingQueue<Message> messagesQueue;
    private long lastTimerStart;
    private SendTool sendTool;
    private DatagramSocket socket;
    private final ConcurrentHashMap<InetSocketAddress, Long> nodesInfo;
    private final ArrayList<InetSocketAddress> nodes;

    ResendTool(ArrayBlockingQueue<Message> messagesQueue, SendTool sendTool, DatagramSocket socket, ConcurrentHashMap<InetSocketAddress, Long> nodesInfo, ArrayList<InetSocketAddress> nodes) {
        this.messagesQueue = messagesQueue;
        lastTimerStart = System.currentTimeMillis();
        this.sendTool = sendTool;
        this.socket = socket;
        this.nodesInfo = nodesInfo;
        this.nodes = nodes;
    }

    @Override
    public void run() {
        while (true) {
            if (System.currentTimeMillis() - lastTimerStart > 5000) {
                for (Message messageInQueue : messagesQueue) {
                    System.out.println("MESSAGE IN QUEUE: " + messageInQueue.getData() + " " + (messageInQueue.getTimeCreation()) + " GUID: " + messageInQueue.getGuid());
                    if (System.currentTimeMillis() - messageInQueue.getTimeCreation() > 5000) {
                        System.out.println("\nRESENDING MESSAGE: " + messageInQueue.getSender() + " " + messageInQueue.getData() + " " + messageInQueue.getType() + " to " + messageInQueue.getReceiver());
                        try {
                            sendTool.sendMessage(socket, messageInQueue);
                        } catch (IOException e) {
                            System.err.println(e.getMessage());
                        }
                    }
                }

                System.out.println("\nCURRENT NODES: ");
                synchronized (nodes) {
                    if (nodes.isEmpty()) {
                        System.out.println("None");
                    }
                }
                synchronized (nodes) {
                    nodes.forEach((node) -> System.out.println(node.getHostString()));
                }
                System.out.println();

                nodesInfo.forEach(1, (node, time) -> {
                    if (System.currentTimeMillis() - time > 10000) {
                        System.out.println("\nDISCONNECTED NODE -> " + node.getHostString());
                        synchronized (nodes) {
                            nodes.remove(node);
                        }
                        for (Message messageInQueue : messagesQueue) {
                            if (messageInQueue.getReceiver().getHostString().equals(node.getHostString())) {
                                messagesQueue.remove(messageInQueue);
                            }
                        }

                        nodesInfo.remove(node);
                    }
                });

                lastTimerStart = System.currentTimeMillis();
            }
        }
    }
}
