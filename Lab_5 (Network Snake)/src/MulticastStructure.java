import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;

class MulticastStructure implements AutoCloseable {
    private MulticastSocket socket;
    private InetAddress group;
    private int port = 6789;

    MulticastSocket getSocket() {
        return socket;
    }

    InetAddress getGroup() {
        return group;
    }

    int getPort() {
        return port;
    }

    MulticastStructure(String addr) throws IOException {
        group = InetAddress.getByName(addr);
        socket = new MulticastSocket(port);
        socket.joinGroup(group);
    }

    @Override
    public void close() throws IOException {
        socket.close();
        socket.leaveGroup(group);
    }
}
