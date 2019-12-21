import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class SendTool {
    private final ArrayList<InetSocketAddress> nodes;
    private InetSocketAddress nodeReceiveAddress;
    private DatagramSocket socket;
    private boolean sendPing = false;

    boolean getSendPing() {
        return sendPing;
    }

    void setSendPingTrue() {
        sendPing = true;
    }

    void setSendPingFalse() {
        sendPing = false;
    }

    SendTool(ArrayList<InetSocketAddress> nodes, InetSocketAddress nodeReceiveAddress, DatagramSocket socket) {
        this.nodes = nodes;
        this.nodeReceiveAddress = nodeReceiveAddress;
        this.socket = socket;
    }

    void sendMessage(String message, Type type) throws IOException {
        GameMessage newGameMessage = new GameMessage(type, GameMessage.getUniqueSeq());
        GameMessage.incUniqueSeq();

        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        String gameMessageJson = gson.toJson(newGameMessage);

        byte[] buf = (gameMessageJson + "-" + message).getBytes(StandardCharsets.UTF_8);
        DatagramPacket pack = new DatagramPacket(buf, 0, buf.length, nodes.get(0));

        socket.send(pack);
    }

    void sendDefiniteMessage(String message, Type type, InetSocketAddress node) throws IOException {
        GameMessage newGameMessage = new GameMessage(type, GameMessage.getUniqueSeq());
        GameMessage.incUniqueSeq();

        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        String gameMessageJson = gson.toJson(newGameMessage);

        byte[] buf = (gameMessageJson + "-" + message).getBytes(StandardCharsets.UTF_8);
        DatagramPacket pack = new DatagramPacket(buf, 0, buf.length, node);
        //System.out.println(pack);
        socket.send(pack);
    }

    void sendRecentMessage(String message, Type type) throws IOException {
        GameMessage newGameMessage = new GameMessage(type, GameMessage.getUniqueSeq());
        GameMessage.incUniqueSeq();

        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        String gameMessageJson = gson.toJson(newGameMessage);

        byte[] buf = (gameMessageJson + "-" + message).getBytes(StandardCharsets.UTF_8);
        if (nodes.size() != 0) {
            DatagramPacket pack = new DatagramPacket(buf, 0, buf.length, nodes.get(nodes.size() - 1));
            socket.send(pack);
        }
    }

    void sendEverybodyMessage(String message, Type type) throws IOException {
        GameMessage newGameMessage = new GameMessage(type, GameMessage.getUniqueSeq());
        GameMessage.incUniqueSeq();

        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        String gameMessageJson = gson.toJson(newGameMessage);

        byte[] buf = (gameMessageJson + "-" + message).getBytes(StandardCharsets.UTF_8);
        for (int i = 0; i < nodes.size(); i++) {
            DatagramPacket pack = new DatagramPacket(buf, 0, buf.length, nodes.get(i));
            //System.out.println(pack);
            socket.send(pack);
        }
    }
}
