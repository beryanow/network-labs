import org.xbill.DNS.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

class MessageAssistance {
    void headersMessage(SelectionKey key, Assistance assistance, int byteRead, Selector selector) throws IOException {
        byte[] array = assistance.getBuf().array();
        if (checkIPv4Request(array)) {
            InetAddress host = getAddress(array);
            int port = getPort(array, byteRead);
            connectHost(assistance, host, port, selector);
            assert host != null;
            answerOkayClient(assistance, host, (byte) port);
        }
        if (checkDomainRequest(array)) {
            int length = array[4];
            StringBuilder domain = new StringBuilder();
            int i;
            for (i = 5; i < 5 + length; i++) {
                domain.append((char) array[i]);
            }
            int port = (((0xFF & array[i]) << 8) + (0xFF & array[i + 1]));
            assistance.setPort(port);
            Name name = org.xbill.DNS.Name.fromString(domain.toString(), Name.root);
            Record rec = Record.newRecord(name, Type.A, DClass.IN);
            Message msg = Message.newQuery(rec);
            PortForwarder.dnsChannel.write(ByteBuffer.wrap(msg.toWire()));
            PortForwarder.dnsMap.put(msg.getHeader().getID(), key);
        }
    }

    void answerOkayClient(Assistance assistance, InetAddress host, byte port) throws IOException {
        byte[] answer = {0x05, // version
                0x00, // success
                0x00,
                0x01, // IPv4
                host.getAddress()[0],
                host.getAddress()[1],
                host.getAddress()[2],
                host.getAddress()[3],
                0x00,
                port};
        assistance.getSocketChannel().write(ByteBuffer.wrap(answer));
        assistance.getBuf().clear();
    }

    void connectHost(Assistance assistance, InetAddress host, int port, Selector selector) throws IOException {
        SocketChannel newHostChannel;
        newHostChannel = SocketChannel.open();
        newHostChannel.configureBlocking(false);
        newHostChannel.connect(new InetSocketAddress(host, port));
        Assistance newHost = new Assistance(newHostChannel, selector);
        assistance.setOtherAssistance(newHost);
        newHost.setOtherAssistance(assistance);
        assistance.getBuf().clear();
        assistance.getOtherAssistance().getBuf().clear();
        newHostChannel.register(selector, SelectionKey.OP_CONNECT | SelectionKey.OP_READ, newHost);
    }

    void helloMessage(Assistance assistance) throws IOException {
        if (checkHello(assistance.getBuf().array())) {
            byte[] answer = {0x05, 0x00}; // version , no auth required
            assistance.getSocketChannel().write(ByteBuffer.wrap(answer));
            assistance.getBuf().clear();
            assistance.setFirstMessage(false);
        } else {
            byte[] answer = {0x05, (byte) 0xFF}; // version, no methods
            assistance.getSocketChannel().write(ByteBuffer.wrap(answer));
            assistance.close();
        }
    }

    private boolean checkIPv4Request(byte[] buf) {
        return buf[0] == 0x05 // version
                && buf[1] == 0x01 // connect
                && buf[3] == 0x01; // IPv4
    }

    private boolean checkDomainRequest(byte[] buf) {
        return buf[0] == 0x05 // version
                && buf[1] == 0x01 // connect
                && buf[3] == 0x03; // domain
    }

    private InetAddress getAddress(byte[] buf) throws UnknownHostException {
        if (buf[3] == 0x01) { // IPv4
            byte[] addr = new byte[]{buf[4], buf[5], buf[6], buf[7]};
            return InetAddress.getByAddress(addr);
        }
        return null;
    }

    private int getPort(byte[] buf, int lenBuf) {
        return (((0xFF & buf[lenBuf - 2]) << 8) + (0xFF & buf[lenBuf - 1]));
    }

    private boolean checkHello(byte[] buf) {
        return buf[0] == 0x05 // version
                && buf[1] != 0
                && buf[2] == 0x00; // no auth required
    }
}
