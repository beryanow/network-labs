import java.net.InetSocketAddress;

public class ResendMessage {
    private String message;
    private Type type;
    private InetSocketAddress socket;

    ResendMessage(String message, Type type, InetSocketAddress socket) {
        this.message = message;
        this.type = type;
        this.socket = socket;
    }

    Type getType() {
        return type;
    }

    String getMessage() {
        return message;
    }

    InetSocketAddress getSocket() {
        return socket;
    }
}
