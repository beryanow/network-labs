import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

class Assistance {
    private Selector selector;
    private Assistance otherAssistance;
    private int bufferSize = 4096;
    private ByteBuffer buf = ByteBuffer.allocate(bufferSize);
    private SocketChannel socketChannel;
    private boolean firstMessage = true;
    private int port;
    private boolean finishWrite = false;
    private boolean isFinishRead = false;

    Assistance getOtherAssistance() {
        return otherAssistance;
    }

    ByteBuffer getBuf() {
        return buf;
    }

    SocketChannel getSocketChannel() {
        return socketChannel;
    }

    boolean isFinishRead() {
        return isFinishRead;
    }

    void setOtherAssistance(Assistance otherAssistance) {
        this.otherAssistance = otherAssistance;
    }


    void setFinishRead(boolean finishRead) {
        isFinishRead = finishRead;
    }


    void setFinishWrite(boolean outputShutdown) {
        this.finishWrite = outputShutdown;
    }

    InetAddress getHost() {
        return host;
    }

    void setHost(InetAddress host) {
        this.host = host;
    }

    private InetAddress host;

    boolean isFirstMessage() {
        return firstMessage;
    }

    void setFirstMessage(boolean firstMessage) {
        this.firstMessage = firstMessage;
    }

    boolean isFinishWrite() {
        return finishWrite;
    }

    Assistance(SocketChannel socketChannel, Selector selector) {
        this.socketChannel = socketChannel;
        this.selector = selector;
    }

    void close() {
        try {
            socketChannel.close();
            socketChannel.keyFor(selector).cancel();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void addOption(int option) {
        SelectionKey currentOption = socketChannel.keyFor(selector);
        currentOption.interestOps(currentOption.interestOps() | option);
    }

    void deleteOption(int option) {
        SelectionKey currentOption = socketChannel.keyFor(selector);
        currentOption.interestOps(currentOption.interestOps() & ~option);
    }

    int getPort() {
        return port;
    }

    void setPort(int port) {
        this.port = port;
    }
}





