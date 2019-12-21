import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.text.SimpleDateFormat;
import java.util.Date;

class MulticastStructure implements AutoCloseable {
    private MulticastSocket socket;
    private InetAddress group;

    private int port = 6789;

    private Long recentSendTimeLong;
    private String recentReceiveTime;

    MulticastStructure(String addr) throws IOException {
        group = InetAddress.getByName(addr);
        socket = new MulticastSocket(port);
        socket.setSoTimeout(1000);
        socket.joinGroup(group);
    }

    void setRecentReceiveTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        recentReceiveTime = "date: " + dateFormat.format(new Date());
    }

    void setRecentSendTimeLong(Long time) {
        recentSendTimeLong = time;
    }

    Long getRecentSendTimeLong() {
        return recentSendTimeLong;
    }

    String getRecentReceiveTime() {
        return recentReceiveTime;
    }

    int getPort() {
        return port;
    }

    InetAddress getGroup() {
        return group;
    }

    MulticastSocket getSocket() {
        return socket;
    }

    @Override
    public void close() throws IOException {
        socket.close();
        socket.leaveGroup(group);
    }
}
