import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Scanner;

class Node {
    private InetSocketAddress nodeAddress;
    private ConnectionTool connectionTool;

    Node(String nodeIP, int lostPercentage, int nodePort) throws SocketException {
        nodeAddress = new InetSocketAddress(nodeIP, nodePort);
        connectionTool = new ConnectionTool(nodeAddress, lostPercentage);
        Thread connectorThread = new Thread(connectionTool);
        connectorThread.start();
    }

    Node(String nodeIP, int lostPercentage, int nodePort, String parentIP, int parentPort) throws IOException {
        nodeAddress = new InetSocketAddress(nodeIP, nodePort);
        InetSocketAddress parentAddress = new InetSocketAddress(parentIP, parentPort);
        connectionTool = new ConnectionTool(nodeAddress, lostPercentage);
        connectionTool.addNode(parentAddress);
        connectionTool.makeConnection("Connect!", "CONNECT", parentAddress);
        Thread connectionThread = new Thread(connectionTool);
        connectionThread.start();
    }

    void enterMessages() throws IOException {
        Scanner textScanner = new Scanner(System.in);
        while (true) {
            String word = textScanner.next();
            connectionTool.makeMessage(word, "MESSAGE");
        }
    }
}
