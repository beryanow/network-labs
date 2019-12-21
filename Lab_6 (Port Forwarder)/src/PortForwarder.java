import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;

public class PortForwarder {
    private InetSocketAddress inetSocketAddress;
    private ServerSocketChannel serverSocketChannel;
    private Selector selector;
    private Assistance assistance;

    PortForwarder(Assistance assistance) {
        this.assistance = assistance;
    }

    private void connectClient(SelectionKey selectionKey) throws IOException {
        SocketChannel connectionSocketChannel = (SocketChannel) selectionKey.channel();
        SocketChannel clientSocketChannel = assistance.getChannelsConnections().get(connectionSocketChannel);

        if (!connectionSocketChannel.finishConnect()) {
            assistance.removeSocketChannels(connectionSocketChannel, clientSocketChannel);
            return;
        }

        clientSocketChannel.register(selector, SelectionKey.OP_READ);
        connectionSocketChannel.register(selector, SelectionKey.OP_READ);
        System.out.println("CONNECTED -> " + connectionSocketChannel.getRemoteAddress());
    }

    private void acceptClient() throws IOException {
        SocketChannel clientSocketChannel = serverSocketChannel.accept();
        clientSocketChannel.configureBlocking(false);
        System.out.println("\nNEW CONNECTION -> LOCAL: " + clientSocketChannel.getLocalAddress() + " -> REMOTE: " + clientSocketChannel.getRemoteAddress());

        SocketChannel connectionSocketChannel = SocketChannel.open();
        connectionSocketChannel.configureBlocking(false);

        if (!connectionSocketChannel.connect(inetSocketAddress)) {
            connectionSocketChannel.register(selector, SelectionKey.OP_CONNECT);
        } else {
            clientSocketChannel.register(selector, SelectionKey.OP_READ);
            connectionSocketChannel.register(selector, SelectionKey.OP_READ);
        }

        assistance.getChannelsConnections().put(clientSocketChannel, connectionSocketChannel);
        assistance.getChannelsConnections().put(connectionSocketChannel, clientSocketChannel);
        assistance.getChannelsBuffers().put(clientSocketChannel, ByteBuffer.allocate(16384));
        assistance.getChannelsBuffers().put(connectionSocketChannel, ByteBuffer.allocate(16384));
    }

    private void readChannel(SelectionKey key) throws IOException {
        try {
            SocketChannel socketChannel = (SocketChannel) key.channel();
            SocketChannel anotherSocketChannel = assistance.getChannelsConnections().get(socketChannel);
            Set<SelectionKey> keys = selector.keys();
            ByteBuffer buf = assistance.getChannelsBuffers().get(socketChannel);

            int bytesRead = socketChannel.read(buf);
            if (bytesRead == -1) {
                key.interestOps(key.interestOps() & ~SelectionKey.OP_READ);
                anotherSocketChannel.shutdownOutput();
                throw new ClosedChannelException();
            } else {
                System.out.println(bytesRead + " BYTES READ -> " + socketChannel.getRemoteAddress());
            }

            for (SelectionKey selectionKey : keys) {
                if (selectionKey.channel().equals(anotherSocketChannel)) {
                    selectionKey.interestOps(selectionKey.interestOps() | SelectionKey.OP_WRITE);
                }
            }

            key.interestOps(key.interestOps() & ~SelectionKey.OP_READ);
        } catch (ClosedChannelException e) {
            closeChannels(key);
        }
    }

    private void sendChannel(SelectionKey key) throws IOException {
        try {
            SocketChannel socketChannel = (SocketChannel) key.channel();
            SocketChannel anotherSocketChannel = assistance.getChannelsConnections().get(socketChannel);
            Set<SelectionKey> keys = selector.keys();
            ByteBuffer buf = assistance.getChannelsBuffers().get(anotherSocketChannel);

            buf.flip();
            int bytesWritten = socketChannel.write(buf);
            System.out.println(bytesWritten + " BYTES WRITTEN -> " + socketChannel.getRemoteAddress());
            if (!buf.hasRemaining()) {
                key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
            }
            buf.compact();

            for (SelectionKey selectionKey : keys) {
                if (selectionKey.channel().equals(anotherSocketChannel)) {
                    selectionKey.interestOps(selectionKey.interestOps() | SelectionKey.OP_READ);
                }
            }
        } catch (ClosedChannelException e) {
            closeChannels(key);
        }
    }

    void initiate() {
        try {
            inetSocketAddress = assistance.resolveAddress();

            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(assistance.getlPort()));
            serverSocketChannel.configureBlocking(false);

            selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("WAITING ON PORT -> " + assistance.getlPort());

            while (true) {
                int count = selector.select();
                if (count == 0) {
                    continue;
                }
                Set keys = selector.selectedKeys();
                Iterator iterator = keys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = (SelectionKey) iterator.next();
                    iterator.remove();

                    if (key.isAcceptable()) {
                        acceptClient();
                    } else if (key.isReadable()) {
                        readChannel(key);
                    } else if (key.isWritable()) {
                        sendChannel(key);
                    } else if (key.isConnectable()) {
                        connectClient(key);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    private void closeChannels(SelectionKey key) throws IOException {
        SocketChannel firstChannel = (SocketChannel) key.channel();
        SocketChannel secondChannel = assistance.getChannelsConnections().get(firstChannel);

        System.out.println("CLOSING CHANNELS -> " + firstChannel.getLocalAddress() + " + " + secondChannel.getRemoteAddress());
        firstChannel.close();
        secondChannel.close();
        assistance.removeSocketChannels(firstChannel, secondChannel);
    }
}