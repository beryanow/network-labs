import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;

class MulticastPublisher {
    void send (String message, Type type, int port, InetAddress group, MulticastSocket socket) throws IOException {
        GameMessage newGameMessage = new GameMessage(type, GameMessage.getUniqueSeq());
        GameMessage.incUniqueSeq();

        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        String gameMessageJson = gson.toJson(newGameMessage);

        byte[] buf = (gameMessageJson + "-" + message).getBytes(StandardCharsets.UTF_8);
        DatagramPacket packet = new DatagramPacket(buf, buf.length, group, port);
        socket.send(packet);
    }
}