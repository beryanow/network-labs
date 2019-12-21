import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;

public class Node {
    private final ArrayList<InetSocketAddress> nodes;
    private InetSocketAddress nodeAddress;
    private ReceiveTool receiveTool;
    private SendTool sendTool;

    Node(String nodeIP, int nodePort) throws SocketException {
        nodes = new ArrayList<>();
        nodeAddress = new InetSocketAddress(nodeIP, nodePort);
        receiveTool = new ReceiveTool(nodeAddress, nodes);
        sendTool = new SendTool(nodes, nodeAddress, receiveTool.getSocket());
        receiveTool.setSendTool(sendTool);

        Thread receiveThread = new Thread(receiveTool);
        receiveThread.start();
    }

    Node(String nodeIP, int nodePort, String parentIP, int parentPort) throws SocketException {
        nodes = new ArrayList<>();
        nodeAddress = new InetSocketAddress(nodeIP, nodePort);
        InetSocketAddress parentAddress = new InetSocketAddress(parentIP, parentPort);
        nodes.add(parentAddress);
        receiveTool = new ReceiveTool(nodeAddress, nodes);
        sendTool = new SendTool(nodes, nodeAddress, receiveTool.getSocket());
        receiveTool.setSendTool(sendTool);

        Thread receiveThread = new Thread(receiveTool);
        receiveThread.start();
    }

    SendTool getSendTool() {
        return sendTool;
    }

    ReceiveTool getReceiveTool() {
        return receiveTool;
    }
}
