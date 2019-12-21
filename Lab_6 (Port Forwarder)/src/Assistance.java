import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

public class Assistance {
    private int lPort;
    private int rPort;
    private String rHost;

    private Map<SocketChannel, SocketChannel> channelsConnections;
    private Map<SocketChannel, ByteBuffer> channelsBuffers;

    Assistance(int lPort, String rHost, int rPort) {
        this.lPort = lPort;
        this.rPort = rPort;
        this.rHost = rHost;
        channelsConnections = new HashMap<>();
        channelsBuffers = new HashMap<>();
    }

    Map<SocketChannel, SocketChannel> getChannelsConnections() {
        return channelsConnections;
    }

    Map<SocketChannel, ByteBuffer> getChannelsBuffers() {
        return channelsBuffers;
    }

    int getlPort() {
        return lPort;
    }

    void removeSocketChannels(SocketChannel firstSocketChannel, SocketChannel secondSocketChannel) {
        channelsConnections.remove(firstSocketChannel);
        channelsConnections.remove(secondSocketChannel);
        channelsBuffers.remove(firstSocketChannel);
        channelsBuffers.remove(secondSocketChannel);
    }

    InetSocketAddress resolveAddress() throws UnknownHostException {
        InetAddress inetAddress = InetAddress.getByName(rHost);
        return new InetSocketAddress(inetAddress, rPort);
    }
}
