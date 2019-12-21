import java.io.IOException;
import java.net.*;

class MulticastPublisher {
    void send(String message, int port, InetAddress group, MulticastSocket socket) throws IOException {
        byte[] formatMessage = message.getBytes();
        DatagramPacket packet = new DatagramPacket(formatMessage, formatMessage.length, group, port);
        socket.send(packet);
    }
}