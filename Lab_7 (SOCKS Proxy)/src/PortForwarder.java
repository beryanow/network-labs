import org.xbill.DNS.ARecord;
import org.xbill.DNS.Message;
import org.xbill.DNS.Record;
import org.xbill.DNS.ResolverConfig;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;

class PortForwarder {
    private MessageAssistance messageAssistance = new MessageAssistance();
    private Selector selector = null;
    static DatagramChannel dnsChannel;
    static HashMap<Integer, SelectionKey> dnsMap = new HashMap<>();
    private String dnsServer = ResolverConfig.getCurrentConfig().server();

    void start(int port) throws IOException {
        selector = Selector.open();
        ServerSocketChannel serverSocket;
        serverSocket = ServerSocketChannel.open();
        assert serverSocket != null;
        serverSocket.bind(new InetSocketAddress(port));
        serverSocket.configureBlocking(false);
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);

        dnsChannel = DatagramChannel.open();
        dnsChannel.configureBlocking(false);
        dnsChannel.connect(new InetSocketAddress(dnsServer, 53));
        SelectionKey DNSKey = dnsChannel.register(selector, SelectionKey.OP_READ);

        while (true) {
            selector.select();
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> iter = selectedKeys.iterator();
            while (iter.hasNext()) {
                SelectionKey key = iter.next();
                if (DNSKey == key) {
                    if (key.isReadable()) {
                        resolveDns();
                    }
                } else {
                    if (key.isValid() && key.isAcceptable()) {
                        accept(key);
                    }
                    if (key.isValid() && key.isConnectable()) {
                        connect(key);
                    }
                    if (key.isValid() && key.isReadable()) {
                        read(key);
                    }
                    if (key.isValid() && key.isWritable()) {
                        write(key);
                    }
                }
                iter.remove();
            }

        }
    }

    private void resolveDns() throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(1024);
        if (dnsChannel.read(buf) <= 0)
            return;
        Message message = new Message(buf.array());
        Record[] records = message.getSectionArray(1);
        for (Record record : records) {
            if (record instanceof ARecord) {
                ARecord aRecord = (ARecord) record;
                int id = message.getHeader().getID();
                SelectionKey key = dnsMap.get(id);
                if (key == null)
                    continue;
                Assistance assistance = (Assistance) key.attachment();
                assistance.setHost(aRecord.getAddress());
                messageAssistance.connectHost(assistance, assistance.getHost(), assistance.getPort(), selector);
                messageAssistance.answerOkayClient(assistance, assistance.getHost(), (byte) assistance.getPort());
                return;
            }
        }
    }

    private void accept(SelectionKey key) {
        SocketChannel clientChannel = null;
        try {
            clientChannel = ((ServerSocketChannel) key.channel()).accept();
            clientChannel.configureBlocking(false);
            Assistance client = new Assistance(clientChannel, selector);
            clientChannel.register(selector, SelectionKey.OP_READ, client);
        } catch (IOException e) {
            e.printStackTrace();
            try {
                assert clientChannel != null;
                clientChannel.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    private void connect(SelectionKey key) {
        SocketChannel channel = ((SocketChannel) key.channel());
        Assistance assistance = ((Assistance) key.attachment());
        try {
            channel.finishConnect();
            assistance.deleteOption(SelectionKey.OP_CONNECT);
            assistance.addOption(SelectionKey.OP_WRITE);
        } catch (IOException e) {
            e.printStackTrace();
            assistance.close();
        }
    }

    private void read(SelectionKey key) {
        Assistance assistance = (Assistance) key.attachment();
        try {
            int byteRead = assistance.getSocketChannel().read(assistance.getBuf());
            if (assistance.getOtherAssistance() == null) {
                if (assistance.isFirstMessage()) {
                    messageAssistance.helloMessage(assistance);
                } else {
                    messageAssistance.headersMessage(key, assistance, byteRead, selector);
                }
            } else {
                Assistance otherAssistance = assistance.getOtherAssistance();
                if (byteRead > 0 && otherAssistance.getSocketChannel().isConnected()) {
                    otherAssistance.addOption(SelectionKey.OP_WRITE);
                }
                if (assistance.getBuf().position() == 0) {
                    assistance.deleteOption(SelectionKey.OP_READ);
                    otherAssistance.setFinishWrite(true);
                    if (assistance.isFinishWrite() || otherAssistance.getBuf().position() == 0) {
                        assistance.close();
                        otherAssistance.close();
                    }
                }
                if (byteRead == -1) {
                    assistance.setFinishRead(true);
                }
            }
        } catch (IOException e) {
            assistance.close();
        }

    }

    private void write(SelectionKey key) {
        Assistance assistance = (Assistance) key.attachment();
        Assistance otherAssistance = assistance.getOtherAssistance();
        otherAssistance.getBuf().flip();
        try {
            int byteWrite = assistance.getSocketChannel().write(otherAssistance.getBuf());
            if (byteWrite > 0) {
                otherAssistance.getBuf().compact();
                otherAssistance.addOption(SelectionKey.OP_READ);
            }
            if (otherAssistance.getBuf().position() == 0) {
                assistance.deleteOption(SelectionKey.OP_WRITE);
                if (otherAssistance.isFinishRead()) {
                    assistance.getSocketChannel().shutdownOutput();
                    assistance.setFinishWrite(true);
                    if (otherAssistance.isFinishWrite()) {
                        assistance.close();
                        otherAssistance.close();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
