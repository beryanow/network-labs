import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

class MulticastReceiver {
    ArrayList<String> receive(MulticastSocket socket) throws IOException {
        byte[] formatMessage = new byte[1024];
        DatagramPacket packet = new DatagramPacket(formatMessage, formatMessage.length);
        try {
            socket.receive(packet);
        } catch (SocketTimeoutException e) {
            return null;
        }

        ArrayList<String> pack = new ArrayList<>();
        pack.add(packet.getAddress().toString());
        pack.add(new String(packet.getData(), packet.getOffset(), packet.getLength()));
        return pack;
    }
}

